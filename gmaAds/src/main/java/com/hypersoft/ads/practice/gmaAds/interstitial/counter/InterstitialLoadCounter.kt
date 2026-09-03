package com.hypersoft.ads.practice.gmaAds.interstitial.counter

import android.util.Log
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAdConfig
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAdKey

/**
 * Frequency gate for interstitial loads.
 *
 * Non-load ticks ([CounterDecision.Skip]) advance immediately.
 * Load ticks ([CounterDecision.Arm]) commit only via [commit] when the controller
 * starts a real SDK request — so SkipFallback / AlreadyLoaded / AlreadyLoading
 * do not burn a capped slot.
 */
internal class InterstitialLoadCounter(
    private val sharedPrefManager: SharedPrefManager,
) {

    private val currentCounters = mutableMapOf<InterstitialAdKey, Int>()

    fun evaluate(key: InterstitialAdKey): CounterDecision {
        val placement = InterstitialAdConfig[key]
        val loadOnStart = placement.loadOnStart ?: return CounterDecision.Allow
        val remoteCounter = placement.remoteCounter?.invoke(sharedPrefManager) ?: return CounterDecision.Allow
        if (remoteCounter <= 0) {
            Log.d(TAG_ADS, "${key.value} -> interstitial -> load: Skipped, counter disabled")
            return CounterDecision.Skip
        }
        if (remoteCounter == 1) {
            Log.d(TAG_ADS, "${key.value} -> interstitial -> load: counter=1/$remoteCounter load=true")
            return CounterDecision.Allow
        }

        var current = currentCounters[key] ?: UNINITIALIZED
        if (current < 0 && loadOnStart) {
            Log.d(TAG_ADS, "${key.value} -> interstitial -> load: counter=$remoteCounter/$remoteCounter load=true (pending)")
            return CounterDecision.Arm(nextCount = remoteCounter)
        }

        if (current < 0) current = 0
        current += 1
        if (current > remoteCounter) current = 1
        val shouldLoad = current == remoteCounter - 1
        if (shouldLoad) {
            Log.d(TAG_ADS, "${key.value} -> interstitial -> load: counter=$current/$remoteCounter load=true (pending)")
            return CounterDecision.Arm(nextCount = current)
        }

        currentCounters[key] = current
        Log.d(TAG_ADS, "${key.value} -> interstitial -> load: counter=$current/$remoteCounter load=false")
        return CounterDecision.Skip
    }

    fun commit(key: InterstitialAdKey, nextCount: Int) {
        currentCounters[key] = nextCount
        Log.d(TAG_ADS, "${key.value} -> interstitial -> load: counter committed=$nextCount")
    }

    private companion object {
        const val UNINITIALIZED = -1
    }
}

internal sealed interface CounterDecision {
    /** No frequency cap — always request when validation passes. */
    data object Allow : CounterDecision

    /** This call is not a load tick (or cap disabled) — do not request. */
    data object Skip : CounterDecision

    /** Load tick — persist [nextCount] only after a real SDK request starts. */
    data class Arm(val nextCount: Int) : CounterDecision
}