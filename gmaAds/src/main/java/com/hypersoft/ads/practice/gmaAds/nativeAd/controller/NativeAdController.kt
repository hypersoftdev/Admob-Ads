package com.hypersoft.ads.practice.gmaAds.nativeAd.controller

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdConfig
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey
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

internal class NativeAdController {

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val loadedAds = mutableMapOf<NativeAdKey, LoadedNative>()
    private val loadingKeys = mutableSetOf<NativeAdKey>()
    private val loadingDeferreds = mutableMapOf<NativeAdKey, CompletableDeferred<Boolean>>()
    private val fallbackOrder = mutableListOf<NativeAdKey>()
    private val pendingOwnLoadKeys = mutableSetOf<NativeAdKey>()

    fun load(
        key: NativeAdKey,
        context: Context,
        adUnitId: String,
    ): Flow<AdLoadResult> = flow {
        while (true) {
            when (val decision = mutex.withLock { decideLoadLocked(key) }) {
                LoadDecision.AlreadyLoaded -> {
                    val cacheLabel = if (NativeAdConfig[key].cache) " (cache)" else ""
                    Log.i(TAG_ADS, "${key.value} -> native -> load: Already loaded$cacheLabel")
                    emit(AdLoadResult.Loaded)
                    return@flow
                }

                LoadDecision.AlreadyLoading -> {
                    Log.d(TAG_ADS, "${key.value} -> native -> load: Waiting for in-flight load")
                    val deferred = mutex.withLock { loadingDeferreds[key]!! }
                    if (deferred.await()) {
                        emit(AdLoadResult.Loaded)
                        return@flow
                    }
                }

                LoadDecision.SkipFallback -> {
                    Log.d(TAG_ADS, "${key.value} -> native -> load: Skipped, fallback available")
                    emit(AdLoadResult.SkippedFallback)
                    return@flow
                }

                is LoadDecision.WaitFallback -> {
                    Log.d(TAG_ADS, "${key.value} -> native -> load: Waiting for in-flight fallback")
                    val fallbackLoaded = coroutineScope {
                        decision.deferreds
                            .map { deferred -> async { deferred.await() } }
                            .awaitAll()
                            .any { it }
                    }
                    val hasEligibleFallback = mutex.withLock {
                        !pendingOwnLoadKeys.contains(key) && hasEligibleFallbackLocked(key)
                    }
                    if (fallbackLoaded && hasEligibleFallback) {
                        Log.d(TAG_ADS, "${key.value} -> native -> load: Skipped, fallback available")
                        emit(AdLoadResult.SkippedFallback)
                        return@flow
                    }
                }

                LoadDecision.Request -> {
                    emitAll(requestSdkLoad(key, context, adUnitId))
                    return@flow
                }
            }
        }
    }

    suspend fun takeForShow(
        key: NativeAdKey,
        canUseAvailableFallback: Boolean,
    ): ShowTarget? = mutex.withLock {
        evictExpiredLocked()
        val sourceKey = resolveSourceKeyLocked(key, canUseAvailableFallback) ?: return@withLock null

        if (sourceKey == key) {
            val retainInCache = NativeAdConfig[sourceKey].cache
            if (retainInCache) {
                val loaded = loadedAds[sourceKey] ?: return@withLock null
                val isReshow = loaded.shown
                loadedAds[sourceKey] = loaded.copy(shown = true)
                ShowTarget(sourceKey, loaded.ad, isReshow = isReshow)
            } else {
                val ad = dequeueLocked(sourceKey) ?: return@withLock null
                ShowTarget(sourceKey, ad, isReshow = false)
            }
        } else {
            // Fallback always transfers the ad away from the source owner so it reloads fresh.
            val ad = dequeueLocked(sourceKey) ?: return@withLock null
            pendingOwnLoadKeys.add(key)
            ShowTarget(sourceKey, ad, isReshow = false)
        }
    }

    fun destroy(key: NativeAdKey, destroyIfImpressionReceived: Boolean = false) {
        scope.launch {
            mutex.withLock { destroyLocked(key, destroyIfImpressionReceived) }
        }
    }

    fun destroyAll(destroyIfImpressionReceived: Boolean = false) {
        scope.launch {
            mutex.withLock {
                if (destroyIfImpressionReceived) {
                    loadedAds.keys.toList().forEach { key ->
                        destroyLocked(key, destroyIfImpressionReceived = true)
                    }
                    return@withLock
                }
                loadingKeys.toList().forEach { finishLoadingLocked(it, success = false) }
                loadedAds.values.forEach { it.ad.destroy() }
                loadedAds.clear()
                fallbackOrder.clear()
                pendingOwnLoadKeys.clear()
            }
        }
    }

    private fun requestSdkLoad(
        key: NativeAdKey,
        context: Context,
        adUnitId: String,
    ): Flow<AdLoadResult> = callbackFlow {
        Log.d(TAG_ADS, "${key.value} -> native -> load: Requesting ad...")
        val nativeAdOptions = NativeAdOptions.Builder()
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .build()

        val adLoader = AdLoader.Builder(context.applicationContext, adUnitId)
            .forNativeAd { nativeAd ->
                Log.i(TAG_ADS, "${key.value} -> native -> load: Loaded")
                scope.launch {
                    mutex.withLock {
                        finishLoadingLocked(key, success = true)
                        loadedAds[key]?.ad?.destroy()
                        loadedAds[key] = LoadedNative(nativeAd, SystemClock.elapsedRealtime())
                        pendingOwnLoadKeys.remove(key)
                        enqueueFallbackLocked(key)
                    }
                    trySend(AdLoadResult.Loaded)
                    close()
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG_ADS, "${key.value} -> native -> load: Failed: ${error.message}")
                    scope.launch {
                        mutex.withLock { finishLoadingLocked(key, success = false) }
                        trySend(AdLoadResult.Failed(AdFailureReason.Sdk(error.code, error.message)))
                        close()
                    }
                }

                override fun onAdImpression() {
                    Log.v(TAG_ADS, "${key.value} -> native -> show: Impression")
                    scope.launch {
                        mutex.withLock { markImpressionLocked(key) }
                    }
                }
            })
            .withNativeAdOptions(nativeAdOptions)
            .build()

        var requested = false
        try {
            withContext(Dispatchers.Main.immediate) {
                adLoader.loadAd(AdRequest.Builder().build())
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

    private fun decideLoadLocked(key: NativeAdKey): LoadDecision {
        evictExpiredLocked()
        val placement = NativeAdConfig[key]
        if (loadedAds[key] != null) return LoadDecision.AlreadyLoaded
        if (loadingKeys.contains(key)) return LoadDecision.AlreadyLoading
        if (
            placement.canUseAvailableFallback &&
            !pendingOwnLoadKeys.contains(key) &&
            hasEligibleFallbackLocked(key)
        ) {
            return LoadDecision.SkipFallback
        }
        if (placement.canUseAvailableFallback) {
            val deferreds = inFlightFallbackDeferredsLocked(key)
            if (deferreds.isNotEmpty()) return LoadDecision.WaitFallback(deferreds)
        }
        markLoadingLocked(key)
        return LoadDecision.Request
    }

    private fun resolveSourceKeyLocked(key: NativeAdKey, canUseAvailableFallback: Boolean): NativeAdKey? {
        if (loadedAds[key] != null) return key
        if (!canUseAvailableFallback) return null
        return fallbackOrder.firstOrNull { isEligibleFallbackLocked(it, key) }
    }

    private fun isEligibleFallbackLocked(candidate: NativeAdKey, except: NativeAdKey): Boolean {
        if (candidate == except) return false
        if (!NativeAdConfig[candidate].canBeUsedAsFallback) return false
        val loaded = loadedAds[candidate] ?: return false
        return !loaded.impressionReceived
    }

    private fun hasEligibleFallbackLocked(except: NativeAdKey): Boolean {
        return loadedAds.keys.any { isEligibleFallbackLocked(it, except) }
    }

    private fun inFlightFallbackDeferredsLocked(except: NativeAdKey): List<CompletableDeferred<Boolean>> {
        return loadingKeys
            .filter { it != except && NativeAdConfig[it].canBeUsedAsFallback }
            .mapNotNull { loadingDeferreds[it] }
    }

    private fun markImpressionLocked(key: NativeAdKey) {
        val loaded = loadedAds[key] ?: return
        if (loaded.impressionReceived) return
        loadedAds[key] = loaded.copy(impressionReceived = true)
        Log.v(TAG_ADS, "${key.value} -> native -> show: Impression")
    }

    private fun markLoadingLocked(key: NativeAdKey) {
        loadingKeys.add(key)
        loadingDeferreds.getOrPut(key) { CompletableDeferred() }
    }

    private fun finishLoadingLocked(key: NativeAdKey, success: Boolean) {
        loadingKeys.remove(key)
        loadingDeferreds.remove(key)?.complete(success)
    }

    private fun enqueueFallbackLocked(key: NativeAdKey) {
        if (!NativeAdConfig[key].canBeUsedAsFallback) return
        if (fallbackOrder.contains(key)) return
        fallbackOrder.add(key)
    }

    private fun dequeueLocked(key: NativeAdKey): NativeAd? {
        fallbackOrder.remove(key)
        return loadedAds.remove(key)?.ad
    }

    private fun destroyLocked(key: NativeAdKey, destroyIfImpressionReceived: Boolean) {
        val loaded = loadedAds[key]
        if (destroyIfImpressionReceived) {
            if (loaded == null || !loaded.impressionReceived) {
                Log.d(TAG_ADS, "${key.value} -> native -> destroy: Skipped, impression not received")
                return
            }
        }
        finishLoadingLocked(key, success = false)
        loadedAds.remove(key)?.ad?.destroy()
        fallbackOrder.remove(key)
        pendingOwnLoadKeys.remove(key)
        Log.e(TAG_ADS, "${key.value} -> native -> destroy: Destroyed")
    }

    private fun evictExpiredLocked() {
        val now = SystemClock.elapsedRealtime()
        val expiredKeys = loadedAds
            .filter { now - it.value.loadedAtMs >= AD_TTL_MS }
            .keys
            .toList()
        expiredKeys.forEach { key ->
            Log.e(TAG_ADS, "${key.value} -> native -> evict: Expired")
            loadedAds.remove(key)?.ad?.destroy()
            fallbackOrder.remove(key)
        }
    }

    internal data class ShowTarget(
        val sourceKey: NativeAdKey,
        val ad: NativeAd,
        val isReshow: Boolean,
    )

    private data class LoadedNative(
        val ad: NativeAd,
        val loadedAtMs: Long,
        val shown: Boolean = false,
        val impressionReceived: Boolean = false,
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