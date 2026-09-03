package com.hypersoft.ads.practice.gmaAds.appOpen.controller

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAdConfig
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAdKey
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
import com.hypersoft.ads.practice.gmaAds.common.AdShowResult
import com.hypersoft.ads.practice.gmaAds.common.FullscreenAdGate
import com.hypersoft.ads.practice.gmaAds.common.extensions.isSafeForAd
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

internal class AppOpenAdController(
    private val fullscreenAdGate: FullscreenAdGate,
) {

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val loadedAds = mutableMapOf<AppOpenAdKey, LoadedAppOpen>()
    private val loadingKeys = mutableSetOf<AppOpenAdKey>()
    private val loadingDeferreds = mutableMapOf<AppOpenAdKey, CompletableDeferred<Boolean>>()
    private val fallbackOrder = mutableListOf<AppOpenAdKey>()

    fun load(
        key: AppOpenAdKey,
        context: Context,
        adUnitId: String,
        onSdkRequest: () -> Unit = {},
    ): Flow<AdLoadResult> = flow {
        while (true) {
            when (val decision = mutex.withLock { decideLoadLocked(key) }) {
                LoadDecision.AlreadyLoaded -> {
                    Log.i(TAG_ADS, "${key.value} -> appOpen -> load: Already loaded")
                    emit(AdLoadResult.Loaded)
                    return@flow
                }

                LoadDecision.AlreadyLoading -> {
                    Log.d(TAG_ADS, "${key.value} -> appOpen -> load: Already loading")
                    emit(AdLoadResult.AlreadyLoading)
                    return@flow
                }

                LoadDecision.SkipFallback -> {
                    Log.d(TAG_ADS, "${key.value} -> appOpen -> load: Skipped, fallback available")
                    emit(AdLoadResult.SkippedFallback)
                    return@flow
                }

                is LoadDecision.WaitFallback -> {
                    Log.d(TAG_ADS, "${key.value} -> appOpen -> load: Waiting for in-flight fallback")
                    val fallbackLoaded = coroutineScope {
                        decision.deferreds
                            .map { deferred -> async { deferred.await() } }
                            .awaitAll()
                            .any { it }
                    }
                    if (fallbackLoaded) {
                        Log.d(TAG_ADS, "${key.value} -> appOpen -> load: Skipped, fallback available")
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
        key: AppOpenAdKey,
        activity: Activity,
        canUseAvailableFallback: Boolean,
    ): Flow<AdShowResult> = callbackFlow {
        if (!activity.isSafeForAd()) {
            Log.e(TAG_ADS, "${key.value} -> appOpen -> show: Failed: activity finishing/destroyed")
            trySend(AdShowResult.Failed(AdFailureReason.InvalidActivity))
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
            Log.e(TAG_ADS, "${key.value} -> appOpen -> show: Not loaded")
            trySend(AdShowResult.NotAvailable)
            close()
            awaitClose { }
            return@callbackFlow
        }

        val ad = target.ad
        suspend fun restoreAndFailInvalidActivity() {
            ad.fullScreenContentCallback = null
            mutex.withLock { restoreLocked(target.sourceKey, ad, target.loadedAtMs) }
            Log.e(TAG_ADS, "${key.value} -> appOpen -> show: Failed: activity finishing/destroyed")
            trySend(AdShowResult.Failed(AdFailureReason.InvalidActivity))
            close()
        }

        if (!activity.isSafeForAd()) {
            restoreAndFailInvalidActivity()
            awaitClose { }
            return@callbackFlow
        }

        if (target.sourceKey == key) {
            Log.d(TAG_ADS, "${key.value} -> appOpen -> show: Showing")
        } else {
            Log.d(TAG_ADS, "${key.value} -> appOpen -> show: Fallback from ${target.sourceKey.value}")
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                Log.v(TAG_ADS, "${key.value} -> appOpen -> show: Impression")
                trySend(AdShowResult.Impression)
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG_ADS, "${key.value} -> appOpen -> show: Dismissed")
                ad.fullScreenContentCallback = null
                fullscreenAdGate.dismissed()
                trySend(AdShowResult.Dismissed)
                close()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG_ADS, "${key.value} -> appOpen -> show: Failed: ${error.message}")
                ad.fullScreenContentCallback = null
                fullscreenAdGate.dismissed()
                trySend(AdShowResult.Failed(AdFailureReason.Sdk(error.code, error.message)))
                close()
            }
        }
        withContext(Dispatchers.Main.immediate) {
            if (!activity.isSafeForAd()) {
                restoreAndFailInvalidActivity()
                return@withContext
            }
            fullscreenAdGate.showing()
            ad.show(activity)
        }
        awaitClose { }
    }

    fun destroy(key: AppOpenAdKey) {
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
        key: AppOpenAdKey,
        context: Context,
        adUnitId: String,
    ): Flow<AdLoadResult> = callbackFlow {
        Log.d(TAG_ADS, "${key.value} -> appOpen -> load: Requesting ad...")
        val callback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                Log.i(TAG_ADS, "${key.value} -> appOpen -> load: Loaded")
                scope.launch {
                    mutex.withLock {
                        finishLoadingLocked(key, success = true)
                        loadedAds[key] = LoadedAppOpen(ad, SystemClock.elapsedRealtime())
                        enqueueFallbackLocked(key)
                    }
                    trySend(AdLoadResult.Loaded)
                    close()
                }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG_ADS, "${key.value} -> appOpen -> load: Failed: ${error.message}")
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
                AppOpenAd.load(
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

    private fun decideLoadLocked(key: AppOpenAdKey): LoadDecision {
        evictExpiredLocked()
        val placement = AppOpenAdConfig[key]
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

    private fun resolveSourceKeyLocked(key: AppOpenAdKey, canUseAvailableFallback: Boolean): AppOpenAdKey? {
        if (loadedAds[key] != null) return key
        if (!canUseAvailableFallback) return null
        return fallbackOrder.firstOrNull { it != key && loadedAds[it] != null }
    }

    private fun hasLoadedFallbackLocked(except: AppOpenAdKey): Boolean {
        return loadedAds.keys.any { it != except && AppOpenAdConfig[it].canBeUsedAsFallback }
    }

    private fun inFlightFallbackDeferredsLocked(except: AppOpenAdKey): List<CompletableDeferred<Boolean>> {
        return loadingKeys
            .filter { it != except && AppOpenAdConfig[it].canBeUsedAsFallback }
            .mapNotNull { loadingDeferreds[it] }
    }

    private fun markLoadingLocked(key: AppOpenAdKey) {
        loadingKeys.add(key)
        loadingDeferreds.getOrPut(key) { CompletableDeferred() }
    }

    private fun finishLoadingLocked(key: AppOpenAdKey, success: Boolean) {
        loadingKeys.remove(key)
        loadingDeferreds.remove(key)?.complete(success)
    }

    private fun enqueueFallbackLocked(key: AppOpenAdKey) {
        if (!AppOpenAdConfig[key].canBeUsedAsFallback) return
        if (fallbackOrder.contains(key)) return
        fallbackOrder.add(key)
    }

    private fun dequeueLocked(key: AppOpenAdKey): AppOpenAd? {
        fallbackOrder.remove(key)
        return loadedAds.remove(key)?.ad
    }

    private fun restoreLocked(key: AppOpenAdKey, ad: AppOpenAd, loadedAtMs: Long) {
        loadedAds[key] = LoadedAppOpen(ad, loadedAtMs)
        enqueueFallbackLocked(key)
    }

    private fun destroyLocked(key: AppOpenAdKey) {
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
            Log.d(TAG_ADS, "${key.value} -> appOpen -> evict: Expired")
            loadedAds.remove(key)?.ad?.fullScreenContentCallback = null
            fallbackOrder.remove(key)
        }
    }

    private data class LoadedAppOpen(
        val ad: AppOpenAd,
        val loadedAtMs: Long,
    )

    private data class ShowTarget(
        val sourceKey: AppOpenAdKey,
        val ad: AppOpenAd,
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
        const val AD_TTL_MS = 4 * 60 * 60 * 1000L
    }
}