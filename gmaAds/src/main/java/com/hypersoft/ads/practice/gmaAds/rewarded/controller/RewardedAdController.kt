package com.hypersoft.ads.practice.gmaAds.rewarded.controller

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
import com.hypersoft.ads.practice.gmaAds.common.FullscreenAdGate
import com.hypersoft.ads.practice.gmaAds.common.RewardedShowResult
import com.hypersoft.ads.practice.gmaAds.common.extensions.isSafeForAd
import com.hypersoft.ads.practice.gmaAds.rewarded.RewardedAdConfig
import com.hypersoft.ads.practice.gmaAds.rewarded.RewardedAdKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class RewardedAdController(
    private val fullscreenAdGate: FullscreenAdGate,
) {

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val loadedAds = mutableMapOf<RewardedAdKey, LoadedRewarded>()
    private val loadingKeys = mutableSetOf<RewardedAdKey>()
    private val loadingDeferreds = mutableMapOf<RewardedAdKey, CompletableDeferred<Boolean>>()
    private val fallbackOrder = mutableListOf<RewardedAdKey>()

    fun load(
        key: RewardedAdKey,
        context: Context,
        adUnitId: String,
        onSdkRequest: () -> Unit = {},
    ): Flow<AdLoadResult> = flow {
        while (true) {
            when (val decision = mutex.withLock { decideLoadLocked(key) }) {
                LoadDecision.AlreadyLoaded -> {
                    Log.i(TAG_ADS, "${key.value} -> rewarded -> load: Already loaded")
                    emit(AdLoadResult.Loaded)
                    return@flow
                }

                LoadDecision.AlreadyLoading -> {
                    Log.d(TAG_ADS, "${key.value} -> rewarded -> load: Already loading")
                    emit(AdLoadResult.AlreadyLoading)
                    return@flow
                }

                LoadDecision.SkipFallback -> {
                    Log.d(TAG_ADS, "${key.value} -> rewarded -> load: Skipped, fallback available")
                    emit(AdLoadResult.SkippedFallback)
                    return@flow
                }

                is LoadDecision.WaitFallback -> {
                    Log.d(TAG_ADS, "${key.value} -> rewarded -> load: Waiting for in-flight fallback")
                    val fallbackLoaded = coroutineScope {
                        decision.deferreds
                            .map { deferred -> async { deferred.await() } }
                            .awaitAll()
                            .any { it }
                    }
                    if (fallbackLoaded) {
                        Log.d(TAG_ADS, "${key.value} -> rewarded -> load: Skipped, fallback available")
                        emit(AdLoadResult.SkippedFallback)
                        return@flow
                    }
                }

                LoadDecision.Request -> {
                    onSdkRequest()
                    emitAll(requestSdkLoad(key, context, adUnitId))
                    return@flow
                }
            }
        }
    }

    fun show(
        key: RewardedAdKey,
        activity: Activity,
        canUseAvailableFallback: Boolean,
    ): Flow<RewardedShowResult> = callbackFlow {
        if (!activity.isSafeForAd()) {
            Log.e(TAG_ADS, "${key.value} -> rewarded -> show: Failed: activity finishing/destroyed")
            trySend(RewardedShowResult.Failed(AdFailureReason.InvalidActivity))
            close()
            awaitClose { }
            return@callbackFlow
        }

        val target = mutex.withLock {
            evictExpiredLocked()
            val sourceKey = resolveSourceKeyLocked(key, canUseAvailableFallback)
            val loaded = sourceKey?.let { loadedAds[it] }
            val ad = sourceKey?.let { dequeueLocked(it) }
            if (sourceKey == null || loaded == null || ad == null) null
            else ShowTarget(sourceKey, ad, loaded.loadedAtMs)
        }
        if (target == null) {
            Log.e(TAG_ADS, "${key.value} -> rewarded -> show: Not loaded")
            trySend(RewardedShowResult.NotAvailable)
            close()
            awaitClose { }
            return@callbackFlow
        }

        val ad = target.ad
        suspend fun restoreAndFailInvalidActivity() {
            ad.fullScreenContentCallback = null
            mutex.withLock { restoreLocked(target.sourceKey, ad, target.loadedAtMs) }
            Log.e(TAG_ADS, "${key.value} -> rewarded -> show: Failed: activity finishing/destroyed")
            trySend(RewardedShowResult.Failed(AdFailureReason.InvalidActivity))
            close()
        }

        if (!activity.isSafeForAd()) {
            restoreAndFailInvalidActivity()
            awaitClose { }
            return@callbackFlow
        }

        if (target.sourceKey == key) {
            Log.d(TAG_ADS, "${key.value} -> rewarded -> show: Showing")
        } else {
            Log.d(TAG_ADS, "${key.value} -> rewarded -> show: Fallback from ${target.sourceKey.value}")
        }

        var rewardGranted = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                Log.v(TAG_ADS, "${key.value} -> rewarded -> show: Impression")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG_ADS, "${key.value} -> rewarded -> show: Dismissed rewardGranted=$rewardGranted")
                ad.fullScreenContentCallback = null
                fullscreenAdGate.dismissed()
                trySend(RewardedShowResult.Closed(rewardGranted))
                close()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG_ADS, "${key.value} -> rewarded -> show: Failed: ${error.message}")
                ad.fullScreenContentCallback = null
                fullscreenAdGate.dismissed()
                trySend(RewardedShowResult.Failed(AdFailureReason.Sdk(error.code, error.message)))
                close()
            }
        }
        withContext(Dispatchers.Main.immediate) {
            if (!activity.isSafeForAd()) {
                restoreAndFailInvalidActivity()
                return@withContext
            }
            fullscreenAdGate.showing()
            ad.show(activity) {
                rewardGranted = true
                Log.d(TAG_ADS, "${key.value} -> rewarded -> show: Reward granted")
            }
        }
        awaitClose { }
    }

    fun destroy(key: RewardedAdKey) {
        scope.launch {
            mutex.withLock { destroyLocked(key) }
        }
    }

    fun destroyAll() {
        scope.launch {
            mutex.withLock {
                loadingKeys.toList().forEach { finishLoadingLocked(it, success = false) }
                loadedAds.values.forEach { it.ad.fullScreenContentCallback = null }
                loadedAds.clear()
                fallbackOrder.clear()
            }
        }
    }

    private fun requestSdkLoad(
        key: RewardedAdKey,
        context: Context,
        adUnitId: String,
    ): Flow<AdLoadResult> = callbackFlow {
        Log.d(TAG_ADS, "${key.value} -> rewarded -> load: Requesting ad...")
        val callback = object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                Log.i(TAG_ADS, "${key.value} -> rewarded -> load: Loaded")
                scope.launch {
                    mutex.withLock {
                        finishLoadingLocked(key, success = true)
                        loadedAds[key] = LoadedRewarded(ad, SystemClock.elapsedRealtime())
                        enqueueFallbackLocked(key)
                    }
                    trySend(AdLoadResult.Loaded)
                    close()
                }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG_ADS, "${key.value} -> rewarded -> load: Failed: ${error.message}")
                scope.launch {
                    mutex.withLock {
                        finishLoadingLocked(key, success = false)
                        dequeueLocked(key)
                    }
                    trySend(AdLoadResult.Failed(AdFailureReason.Sdk(error.code, error.message)))
                    close()
                }
            }
        }
        var requested = false
        try {
            withContext(Dispatchers.Main.immediate) {
                RewardedAd.load(
                    context.applicationContext,
                    adUnitId,
                    AdRequest.Builder().build(),
                    callback,
                )
                requested = true
            }
        } catch (e: CancellationException) {
            if (!requested) {
                withContext(NonCancellable) {
                    mutex.withLock { finishLoadingLocked(key, success = false) }
                }
            }
            throw e
        }
        awaitClose { }
    }

    private fun decideLoadLocked(key: RewardedAdKey): LoadDecision {
        evictExpiredLocked()
        val placement = RewardedAdConfig[key]
        if (loadedAds[key] != null) return LoadDecision.AlreadyLoaded
        if (loadingKeys.contains(key)) return LoadDecision.AlreadyLoading
        if (placement.canUseAvailableFallback && hasLoadedFallbackLocked(key)) {
            return LoadDecision.SkipFallback
        }
        if (placement.canUseAvailableFallback) {
            val deferreds = inFlightFallbackDeferredsLocked(key)
            if (deferreds.isNotEmpty()) return LoadDecision.WaitFallback(deferreds)
        }
        markLoadingLocked(key)
        return LoadDecision.Request
    }

    private fun resolveSourceKeyLocked(key: RewardedAdKey, canUseAvailableFallback: Boolean): RewardedAdKey? {
        if (loadedAds[key] != null) return key
        if (!canUseAvailableFallback) return null
        return fallbackOrder.firstOrNull { it != key && loadedAds[it] != null }
    }

    private fun hasLoadedFallbackLocked(except: RewardedAdKey): Boolean {
        return loadedAds.keys.any { it != except && RewardedAdConfig[it].canBeUsedAsFallback }
    }

    private fun inFlightFallbackDeferredsLocked(except: RewardedAdKey): List<CompletableDeferred<Boolean>> {
        return loadingKeys
            .filter { it != except && RewardedAdConfig[it].canBeUsedAsFallback }
            .mapNotNull { loadingDeferreds[it] }
    }

    private fun markLoadingLocked(key: RewardedAdKey) {
        loadingKeys.add(key)
        loadingDeferreds.getOrPut(key) { CompletableDeferred() }
    }

    private fun finishLoadingLocked(key: RewardedAdKey, success: Boolean) {
        loadingKeys.remove(key)
        loadingDeferreds.remove(key)?.complete(success)
    }

    private fun enqueueFallbackLocked(key: RewardedAdKey) {
        if (!RewardedAdConfig[key].canBeUsedAsFallback) return
        if (fallbackOrder.contains(key)) return
        fallbackOrder.add(key)
    }

    private fun dequeueLocked(key: RewardedAdKey): RewardedAd? {
        fallbackOrder.remove(key)
        return loadedAds.remove(key)?.ad
    }

    private fun restoreLocked(key: RewardedAdKey, ad: RewardedAd, loadedAtMs: Long) {
        loadedAds[key] = LoadedRewarded(ad, loadedAtMs)
        enqueueFallbackLocked(key)
    }

    private fun destroyLocked(key: RewardedAdKey) {
        finishLoadingLocked(key, success = false)
        loadedAds.remove(key)?.ad?.fullScreenContentCallback = null
        fallbackOrder.remove(key)
    }

    private fun evictExpiredLocked() {
        val now = SystemClock.elapsedRealtime()
        val expiredKeys = loadedAds
            .filter { now - it.value.loadedAtMs >= AD_TTL_MS }
            .keys
            .toList()
        expiredKeys.forEach { key ->
            Log.d(TAG_ADS, "${key.value} -> rewarded -> evict: Expired")
            loadedAds.remove(key)?.ad?.fullScreenContentCallback = null
            fallbackOrder.remove(key)
        }
    }

    private data class LoadedRewarded(
        val ad: RewardedAd,
        val loadedAtMs: Long,
    )

    private data class ShowTarget(
        val sourceKey: RewardedAdKey,
        val ad: RewardedAd,
        val loadedAtMs: Long,
    )

    private sealed interface LoadDecision {
        data object AlreadyLoaded : LoadDecision
        data object AlreadyLoading : LoadDecision
        data object SkipFallback : LoadDecision
        data class WaitFallback(val deferreds: List<CompletableDeferred<Boolean>>) : LoadDecision
        data object Request : LoadDecision
    }

    private companion object {
        const val AD_TTL_MS = 60 * 60 * 1000L
    }
}