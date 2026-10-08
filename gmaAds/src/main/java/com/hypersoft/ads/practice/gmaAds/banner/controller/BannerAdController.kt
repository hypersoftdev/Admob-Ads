package com.hypersoft.ads.practice.gmaAds.banner.controller

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.banner.BannerAdConfig
import com.hypersoft.ads.practice.gmaAds.banner.BannerAdKey
import com.hypersoft.ads.practice.gmaAds.banner.model.BannerFormat
import com.hypersoft.ads.practice.gmaAds.banner.view.destroySafely
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
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

internal class BannerAdController {

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val loadedAds = mutableMapOf<BannerAdKey, LoadedBanner>()
    private val shownViews = mutableMapOf<BannerAdKey, AdView>()
    private val impressedViews = mutableSetOf<AdView>()
    private val loadingKeys = mutableSetOf<BannerAdKey>()
    private val loadingDeferreds = mutableMapOf<BannerAdKey, CompletableDeferred<Boolean>>()
    private val fallbackOrder = mutableListOf<BannerAdKey>()
    private val pendingOwnLoadKeys = mutableSetOf<BannerAdKey>()

    fun load(
        key: BannerAdKey,
        activity: Activity,
        adUnitId: String,
        adWidthDp: Int,
        format: BannerFormat,
        maxHeightDp: Int,
    ): Flow<AdLoadResult> = flow {
        while (true) {
            when (val decision = mutex.withLock { decideLoadLocked(key) }) {
                LoadDecision.AlreadyLoaded -> {
                    val cacheLabel = if (BannerAdConfig[key].cache) " (cache)" else ""
                    Log.i(TAG_ADS, "${key.value} -> banner -> load: Already loaded$cacheLabel")
                    emit(AdLoadResult.Loaded)
                    return@flow
                }

                LoadDecision.AlreadyLoading -> {
                    Log.d(TAG_ADS, "${key.value} -> banner -> load: Waiting for in-flight load")
                    val deferred = mutex.withLock { loadingDeferreds[key]!! }
                    if (deferred.await()) {
                        emit(AdLoadResult.Loaded)
                        return@flow
                    }
                }

                LoadDecision.SkipFallback -> {
                    Log.d(TAG_ADS, "${key.value} -> banner -> load: Skipped, fallback available")
                    emit(AdLoadResult.SkippedFallback)
                    return@flow
                }

                is LoadDecision.WaitFallback -> {
                    Log.d(TAG_ADS, "${key.value} -> banner -> load: Waiting for in-flight fallback")
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
                        Log.d(TAG_ADS, "${key.value} -> banner -> load: Skipped, fallback available")
                        emit(AdLoadResult.SkippedFallback)
                        return@flow
                    }
                }

                LoadDecision.Request -> {
                    emitAll(requestSdkLoad(key, activity, adUnitId, adWidthDp, format, maxHeightDp))
                    return@flow
                }
            }
        }
    }

    suspend fun takeForShow(
        key: BannerAdKey,
        canUseAvailableFallback: Boolean,
    ): ShowTarget? = mutex.withLock {
        evictExpiredLocked()

        val own = loadedAds[key]
        if (own != null) {
            replaceShownLocked(key, own.adView)
            val isReshow = own.shown
            if (BannerAdConfig[key].cache) {
                loadedAds[key] = own.copy(shown = true)
                shownViews[key] = own.adView
                return@withLock ShowTarget(key, own.adView, isReshow = isReshow)
            }
            val adView = dequeueLocked(key) ?: return@withLock null
            shownViews[key] = adView
            return@withLock ShowTarget(key, adView, isReshow = false)
        }

        shownViews[key]?.let { adView ->
            return@withLock ShowTarget(key, adView, isReshow = true)
        }

        if (!canUseAvailableFallback) return@withLock null
        val sourceKey = fallbackOrder.firstOrNull { isEligibleFallbackLocked(it, key) } ?: return@withLock null
        shownViews.remove(sourceKey)
        val adView = dequeueLocked(sourceKey) ?: return@withLock null
        pendingOwnLoadKeys.add(key)
        replaceShownLocked(key, adView)
        shownViews[key] = adView
        ShowTarget(sourceKey, adView, isReshow = false)
    }

    fun pause(key: BannerAdKey) {
        scope.launch {
            mutex.withLock { shownViews[key]?.pause() }
        }
    }

    fun resume(key: BannerAdKey) {
        scope.launch {
            mutex.withLock { shownViews[key]?.resume() }
        }
    }

    fun destroy(key: BannerAdKey, destroyIfImpressionReceived: Boolean = false) {
        scope.launch {
            mutex.withLock { destroyLocked(key, destroyIfImpressionReceived) }
        }
    }

    fun destroyAll(destroyIfImpressionReceived: Boolean = false) {
        scope.launch {
            mutex.withLock {
                if (destroyIfImpressionReceived) {
                    val keys = (loadedAds.keys + shownViews.keys).toSet()
                    keys.forEach { key -> destroyLocked(key, destroyIfImpressionReceived = true) }
                    return@withLock
                }
                loadingKeys.toList().forEach { finishLoadingLocked(it, success = false) }
                shownViews.values.forEach { adView ->
                    if (loadedAds.values.none { it.adView === adView }) {
                        adView.destroySafely()
                    }
                }
                loadedAds.values.forEach { it.adView.destroySafely() }
                loadedAds.clear()
                shownViews.clear()
                impressedViews.clear()
                fallbackOrder.clear()
                pendingOwnLoadKeys.clear()
            }
        }
    }

    private fun requestSdkLoad(
        key: BannerAdKey,
        activity: Activity,
        adUnitId: String,
        adWidthDp: Int,
        format: BannerFormat,
        maxHeightDp: Int,
    ): Flow<AdLoadResult> = callbackFlow {
        Log.d(TAG_ADS, "${key.value} -> banner -> load: Requesting ad (${format.name})...")
        val adView = AdView(activity)
        adView.adUnitId = adUnitId
        adView.setAdSize(resolveAdSize(activity, adWidthDp, format, maxHeightDp))
        lateinit var listener: AdListener
        listener = object : AdListener() {
            private var hasLoaded = false

            override fun onAdLoaded() {
                val isRefresh = hasLoaded
                hasLoaded = true
                Log.i(
                    TAG_ADS,
                    if (isRefresh) {
                        "${key.value} -> banner -> load: Refreshed"
                    } else {
                        "${key.value} -> banner -> load: Loaded"
                    },
                )
                scope.launch {
                    val discarded = mutex.withLock {
                        if (adView.adListener !== listener) return@withLock true
                        finishLoadingLocked(key, success = true)
                        val previous = loadedAds[key]
                        if (
                            previous != null &&
                            previous.adView !== adView &&
                            shownViews[key] !== previous.adView
                        ) {
                            previous.adView.destroySafely()
                        }
                        val stillShown = previous != null && previous.adView === adView && previous.shown
                        loadedAds[key] = LoadedBanner(
                            adView = adView,
                            loadedAtMs = SystemClock.elapsedRealtime(),
                            shown = stillShown,
                        )
                        pendingOwnLoadKeys.remove(key)
                        enqueueFallbackLocked(key)
                        false
                    }
                    if (discarded) {
                        if (!isRefresh) {
                            trySend(
                                AdLoadResult.Failed(
                                    AdFailureReason.Sdk(0, "Banner destroyed before load completed"),
                                ),
                            )
                            close()
                        }
                        return@launch
                    }
                    if (isRefresh) return@launch
                    trySend(AdLoadResult.Loaded)
                    close()
                }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                // AdMob reuses this listener for automatic refresh. A no-fill
                // must leave the creative that is already on screen in place.
                // Destroying it blanks the slot and keeps that dead AdView in
                // cache, so the next visit reshows nothing and never reloads.
                if (hasLoaded) {
                    Log.w(
                        TAG_ADS,
                        "${key.value} -> banner -> load: Refresh failed, keeping current ad: ${error.message}",
                    )
                    return
                }
                Log.e(TAG_ADS, "${key.value} -> banner -> load: Failed: ${error.message}")
                adView.destroySafely()
                scope.launch {
                    mutex.withLock { finishLoadingLocked(key, success = false) }
                    trySend(AdLoadResult.Failed(AdFailureReason.Sdk(error.code, error.message)))
                    close()
                }
            }

            override fun onAdImpression() {
                scope.launch {
                    mutex.withLock {
                        if (adView.adListener !== listener) return@withLock
                        markImpressionLocked(adView, key)
                    }
                }
            }
        }
        adView.adListener = listener

        var requested = false
        try {
            withContext(Dispatchers.Main.immediate) {
                adView.loadAd(buildAdRequest(format))
                requested = true
            }
        } catch (e: CancellationException) {
            if (!requested) {
                adView.destroySafely()
                withContext(NonCancellable) {
                    mutex.withLock { finishLoadingLocked(key, success = false) }
                }
            }
            throw e
        }
        awaitClose { }
    }

    private fun decideLoadLocked(key: BannerAdKey): LoadDecision {
        evictExpiredLocked()
        val placement = BannerAdConfig[key]
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

    private fun isEligibleFallbackLocked(candidate: BannerAdKey, except: BannerAdKey): Boolean {
        if (candidate == except) return false
        if (!BannerAdConfig[candidate].canBeUsedAsFallback) return false
        if (BannerAdConfig[candidate].slot != BannerAdConfig[except].slot) return false
        val loaded = loadedAds[candidate] ?: return false
        return loaded.adView !in impressedViews
    }

    private fun hasEligibleFallbackLocked(except: BannerAdKey): Boolean {
        return loadedAds.keys.any { isEligibleFallbackLocked(it, except) }
    }

    private fun inFlightFallbackDeferredsLocked(except: BannerAdKey): List<CompletableDeferred<Boolean>> {
        return loadingKeys
            .filter {
                it != except &&
                        BannerAdConfig[it].canBeUsedAsFallback &&
                        BannerAdConfig[it].slot == BannerAdConfig[except].slot
            }
            .mapNotNull { loadingDeferreds[it] }
    }

    private fun markImpressionLocked(adView: AdView, key: BannerAdKey) {
        if (!impressedViews.add(adView)) return
        Log.v(TAG_ADS, "${key.value} -> banner -> show: Impression")
    }

    private fun markLoadingLocked(key: BannerAdKey) {
        loadingKeys.add(key)
        loadingDeferreds.getOrPut(key) { CompletableDeferred() }
    }

    private fun finishLoadingLocked(key: BannerAdKey, success: Boolean) {
        loadingKeys.remove(key)
        loadingDeferreds.remove(key)?.complete(success)
    }

    private fun enqueueFallbackLocked(key: BannerAdKey) {
        if (!BannerAdConfig[key].canBeUsedAsFallback) return
        if (fallbackOrder.contains(key)) return
        fallbackOrder.add(key)
    }

    private fun dequeueLocked(key: BannerAdKey): AdView? {
        fallbackOrder.remove(key)
        val adView = loadedAds.remove(key)?.adView
        if (adView != null) {
            shownViews.entries.removeAll { it.value === adView && it.key != key }
        }
        return adView
    }

    private fun replaceShownLocked(key: BannerAdKey, incoming: AdView) {
        val previous = shownViews[key] ?: return
        if (previous === incoming) return
        impressedViews.remove(previous)
        previous.destroySafely()
    }

    private fun destroyLocked(key: BannerAdKey, destroyIfImpressionReceived: Boolean) {
        val loaded = loadedAds[key]
        val shown = shownViews[key]
        val adView = shown ?: loaded?.adView
        if (destroyIfImpressionReceived) {
            if (adView == null || adView !in impressedViews) {
                Log.d(TAG_ADS, "${key.value} -> banner -> destroy: Skipped, impression not received")
                return
            }
        }
        finishLoadingLocked(key, success = false)
        shownViews.remove(key)
        loadedAds.remove(key)
        fallbackOrder.remove(key)
        pendingOwnLoadKeys.remove(key)
        if (shown != null && shown !== loaded?.adView) {
            impressedViews.remove(shown)
            shown.destroySafely()
        }
        loaded?.adView?.let { view ->
            impressedViews.remove(view)
            view.destroySafely()
        }
        Log.e(TAG_ADS, "${key.value} -> banner -> destroy: Destroyed")
    }

    private fun evictExpiredLocked() {
        val now = SystemClock.elapsedRealtime()
        val expiredKeys = loadedAds
            .filter { now - it.value.loadedAtMs >= AD_TTL_MS }
            .keys
            .toList()
        expiredKeys.forEach { key ->
            val loaded = loadedAds[key] ?: return@forEach
            if (shownViews[key] === loaded.adView) return@forEach
            Log.e(TAG_ADS, "${key.value} -> banner -> evict: Expired")
            loadedAds.remove(key)
            fallbackOrder.remove(key)
            impressedViews.remove(loaded.adView)
            loaded.adView.destroySafely()
        }
    }

    internal data class ShowTarget(
        val sourceKey: BannerAdKey,
        val adView: AdView,
        val isReshow: Boolean,
    )

    private data class LoadedBanner(
        val adView: AdView,
        val loadedAtMs: Long,
        val shown: Boolean = false,
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
        const val MIN_INLINE_HEIGHT_DP = 32

        fun resolveAdSize(
            activity: Activity,
            adWidthDp: Int,
            format: BannerFormat,
            maxHeightDp: Int,
        ): AdSize {
            val widthDp = adWidthDp.coerceAtLeast(1)
            return when (format) {
                BannerFormat.ADAPTIVE,
                BannerFormat.COLLAPSIBLE_TOP,
                BannerFormat.COLLAPSIBLE_BOTTOM,
                    -> AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp)

                BannerFormat.INLINE_ADAPTIVE_MAX_HEIGHT -> AdSize.getInlineAdaptiveBannerAdSize(
                    widthDp,
                    maxHeightDp.coerceAtLeast(MIN_INLINE_HEIGHT_DP),
                )

                BannerFormat.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
            }
        }

        fun buildAdRequest(format: BannerFormat): AdRequest {
            val builder = AdRequest.Builder()
            val collapsePosition = when (format) {
                BannerFormat.COLLAPSIBLE_TOP -> "top"
                BannerFormat.COLLAPSIBLE_BOTTOM -> "bottom"
                else -> null
            }
            if (collapsePosition != null) {
                val extras = Bundle()
                extras.putString("collapsible", collapsePosition)
                builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            }
            return builder.build()
        }
    }
}
