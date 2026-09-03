package com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.controller

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
import com.hypersoft.ads.practice.gmaAds.common.FullscreenAdGate
import com.hypersoft.ads.practice.gmaAds.common.RewardedShowResult
import com.hypersoft.ads.practice.gmaAds.common.extensions.isSafeForAd
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.RewardedInterstitialAdConfig
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.RewardedInterstitialAdKey
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

internal class RewardedInterstitialAdController(
    private val fullscreenAdGate: FullscreenAdGate,
) {

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val loadedAds = mutableMapOf<RewardedInterstitialAdKey, LoadedRewardedInterstitial>()
    private val loadingKeys = mutableSetOf<RewardedInterstitialAdKey>()
    private val loadingDeferreds = mutableMapOf<RewardedInterstitialAdKey, CompletableDeferred<Boolean>>()
    private val fallbackOrder = mutableListOf<RewardedInterstitialAdKey>()

    fun load(
        key: RewardedInterstitialAdKey,
        context: Context,
        adUnitId: String,
        onSdkRequest: () -> Unit = {},
    ): Flow<AdLoadResult> = flow {
        while (true) {
            when (val decision = mutex.withLock { decideLoadLocked(key) }) {
                LoadDecision.AlreadyLoaded -> {
                    Log.i(TAG_ADS, "${key.value} -> rewardedInterstitial -> load: Already loaded")
                    emit(AdLoadResult.Loaded)
                    return@flow
                }

                LoadDecision.AlreadyLoading -> {
                    Log.d(TAG_ADS, "${key.value} -> rewardedInterstitial -> load: Already loading")
                    emit(AdLoadResult.AlreadyLoading)
                    return@flow
                }

                LoadDecision.SkipFallback -> {
                    Log.d(TAG_ADS, "${key.value} -> rewardedInterstitial -> load: Skipped, fallback available")
                    emit(AdLoadResult.SkippedFallback)
                    return@flow
                }

                is LoadDecision.WaitFallback -> {
                    Log.d(TAG_ADS, "${key.value} -> rewardedInterstitial -> load: Waiting for in-flight fallback")
                    val fallbackLoaded = coroutineScope {
                        decision.deferreds
                            .map { deferred -> async { deferred.await() } }
                            .awaitAll()
                            .any { it }
                    }
                    if (fallbackLoaded) {
                        Log.d(TAG_ADS, "${key.value} -> rewardedInterstitial -> load: Skipped, fallback available")
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
        key: RewardedInterstitialAdKey,
        activity: Activity,
        canUseAvailableFallback: Boolean,
    ): Flow<RewardedShowResult> = callbackFlow {
        if (!activity.isSafeForAd()) {
            Log.e(TAG_ADS, "${key.value} -> rewardedInterstitial -> show: Failed: activity finishing/destroyed")
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
            Log.e(TAG_ADS, "${key.value} -> rewardedInterstitial -> show: Not loaded")
            trySend(RewardedShowResult.NotAvailable)
            close()
            awaitClose { }
            return@callbackFlow
        }

        val ad = target.ad
        suspend fun restoreAndFailInvalidActivity() {
            ad.fullScreenContentCallback = null
            mutex.withLock { restoreLocked(target.sourceKey, ad, target.loadedAtMs) }
            Log.e(TAG_ADS, "${key.value} -> rewardedInterstitial -> show: Failed: activity finishing/destroyed")
            trySend(RewardedShowResult.Failed(AdFailureReason.InvalidActivity))
            close()
        }

        if (!activity.isSafeForAd()) {
            restoreAndFailInvalidActivity()
            awaitClose { }
            return@callbackFlow
        }

        if (target.sourceKey == key) {
            Log.d(TAG_ADS, "${key.value} -> rewardedInterstitial -> show: Showing")
        } else {
            Log.d(TAG_ADS, "${key.value} -> rewardedInterstitial -> show: Fallback from ${target.sourceKey.value}")
        }

        var rewardGranted = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                Log.v(TAG_ADS, "${key.value} -> rewardedInterstitial -> show: Impression")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG_ADS, "${key.value} -> rewardedInterstitial -> show: Dismissed rewardGranted=$rewardGranted")
                ad.fullScreenContentCallback = null
                fullscreenAdGate.dismissed()
                trySend(RewardedShowResult.Closed(rewardGranted))
                close()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG_ADS, "${key.value} -> rewardedInterstitial -> show: Failed: ${error.message}")
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
                Log.d(TAG_ADS, "${key.value} -> rewardedInterstitial -> show: Reward granted")
            }
        }
        awaitClose { }
    }

    fun destroy(key: RewardedInterstitialAdKey) {
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
        key: RewardedInterstitialAdKey,
        context: Context,
        adUnitId: String,
    ): Flow<AdLoadResult> = callbackFlow {
        Log.d(TAG_ADS, "${key.value} -> rewardedInterstitial -> load: Requesting ad...")
        val callback = object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                Log.i(TAG_ADS, "${key.value} -> rewardedInterstitial -> load: Loaded")
                scope.launch {
                    mutex.withLock {
                        finishLoadingLocked(key, success = true)
                        loadedAds[key] = LoadedRewardedInterstitial(ad, SystemClock.elapsedRealtime())
                        enqueueFallbackLocked(key)
                    }
                    trySend(AdLoadResult.Loaded)
                    close()
                }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG_ADS, "${key.value} -> rewardedInterstitial -> load: Failed: ${error.message}")
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
                RewardedInterstitialAd.load(
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

    private fun decideLoadLocked(key: RewardedInterstitialAdKey): LoadDecision {
        evictExpiredLocked()
        val placement = RewardedInterstitialAdConfig[key]
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

    private fun resolveSourceKeyLocked(key: RewardedInterstitialAdKey, canUseAvailableFallback: Boolean): RewardedInterstitialAdKey? {
        if (loadedAds[key] != null) return key
        if (!canUseAvailableFallback) return null
        return fallbackOrder.firstOrNull { it != key && loadedAds[it] != null }
    }

    private fun hasLoadedFallbackLocked(except: RewardedInterstitialAdKey): Boolean {
        return loadedAds.keys.any { it != except && RewardedInterstitialAdConfig[it].canBeUsedAsFallback }
    }

    private fun inFlightFallbackDeferredsLocked(except: RewardedInterstitialAdKey): List<CompletableDeferred<Boolean>> {
        return loadingKeys
            .filter { it != except && RewardedInterstitialAdConfig[it].canBeUsedAsFallback }
            .mapNotNull { loadingDeferreds[it] }
    }

    private fun markLoadingLocked(key: RewardedInterstitialAdKey) {
        loadingKeys.add(key)
        loadingDeferreds.getOrPut(key) { CompletableDeferred() }
    }

    private fun finishLoadingLocked(key: RewardedInterstitialAdKey, success: Boolean) {
        loadingKeys.remove(key)
        loadingDeferreds.remove(key)?.complete(success)
    }

    private fun enqueueFallbackLocked(key: RewardedInterstitialAdKey) {
        if (!RewardedInterstitialAdConfig[key].canBeUsedAsFallback) return
        if (fallbackOrder.contains(key)) return
        fallbackOrder.add(key)
    }

    private fun dequeueLocked(key: RewardedInterstitialAdKey): RewardedInterstitialAd? {
        fallbackOrder.remove(key)
        return loadedAds.remove(key)?.ad
    }

    private fun restoreLocked(key: RewardedInterstitialAdKey, ad: RewardedInterstitialAd, loadedAtMs: Long) {
        loadedAds[key] = LoadedRewardedInterstitial(ad, loadedAtMs)
        enqueueFallbackLocked(key)
    }

    private fun destroyLocked(key: RewardedInterstitialAdKey) {
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
            Log.d(TAG_ADS, "${key.value} -> rewardedInterstitial -> evict: Expired")
            loadedAds.remove(key)?.ad?.fullScreenContentCallback = null
            fallbackOrder.remove(key)
        }
    }

    private data class LoadedRewardedInterstitial(
        val ad: RewardedInterstitialAd,
        val loadedAtMs: Long,
    )

    private data class ShowTarget(
        val sourceKey: RewardedInterstitialAdKey,
        val ad: RewardedInterstitialAd,
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