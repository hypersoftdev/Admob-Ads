package com.hypersoft.ads.practice.gmaAds.interstitial

import android.app.Activity
import android.content.Context
import com.hypersoft.ads.practice.core.platform.InternetManager
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
import com.hypersoft.ads.practice.gmaAds.common.AdShowResult
import com.hypersoft.ads.practice.gmaAds.common.AdsSdk
import com.hypersoft.ads.practice.gmaAds.interstitial.controller.InterstitialAdController
import com.hypersoft.ads.practice.gmaAds.interstitial.counter.CounterDecision
import com.hypersoft.ads.practice.gmaAds.interstitial.counter.InterstitialLoadCounter
import com.hypersoft.ads.practice.gmaAds.interstitial.request.InterstitialLoadRequest
import com.hypersoft.ads.practice.gmaAds.interstitial.request.InterstitialShowRequest
import com.hypersoft.ads.practice.gmaAds.interstitial.validation.InterstitialAdValidator
import com.hypersoft.ads.practice.gmaAds.interstitial.validation.InterstitialLoadValidation
import com.hypersoft.ads.practice.gmaAds.interstitial.validation.InterstitialShowValidation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Public interstitial API: `adsManager.interstitial.load / show / destroy`.
 *
 * Placement policy lives in [InterstitialAdConfig]. This class only runs the pipeline.
 */
class InterstitialAds internal constructor(
    context: Context,
    private val adsSdk: AdsSdk,
    private val sharedPrefManager: SharedPrefManager,
    private val internetManager: InternetManager,
    private val validator: InterstitialAdValidator,
    private val counter: InterstitialLoadCounter,
    private val controller: InterstitialAdController,
) {

    private val context: Context = context.applicationContext

    /**
     * Request an ad for [key].
     *
     * Order: read [InterstitialAdConfig] → validate
     * (premium / RC / internet / ad unit) → frequency counter (if configured) → init GMA SDK → controller load.
     * Frequency-cap load ticks commit only when the controller starts a real SDK request.
     * Controller may skip when a fallback is already loaded or still in flight.
     */
    fun load(key: InterstitialAdKey): Flow<AdLoadResult> = flow {
        val placement = InterstitialAdConfig[key]
        val request = InterstitialLoadRequest(
            key = key,
            context = context,
            adUnitId = context.getString(placement.adUnitResId),
            isEnabledByRemote = placement.isEnabled(sharedPrefManager),
            isInternetConnected = internetManager.isInternetConnected,
            isAppPurchased = sharedPrefManager.isAppPurchased,
        )
        when (val validation = validator.validateLoad(request)) {
            is InterstitialLoadValidation.Invalid -> emit(AdLoadResult.Failed(validation.reason))
            is InterstitialLoadValidation.Valid -> when (val decision = counter.evaluate(key)) {
                CounterDecision.Skip -> emit(AdLoadResult.SkippedCounter)
                CounterDecision.Allow -> {
                    val sdkFailure = adsSdk.initializeForLoad()
                    if (sdkFailure != null) {
                        emit(sdkFailure)
                        return@flow
                    }
                    emitAll(controller.load(key, validation.context, validation.adUnitId))
                }
                is CounterDecision.Arm -> {
                    val sdkFailure = adsSdk.initializeForLoad()
                    if (sdkFailure != null) {
                        emit(sdkFailure)
                        return@flow
                    }
                    emitAll(
                        controller.load(
                            key = key,
                            context = validation.context,
                            adUnitId = validation.adUnitId,
                            onSdkRequest = { counter.commit(key, decision.nextCount) },
                        ),
                    )
                }
            }
        }
    }

    /**
     * Show the ad for [key], or a fallback if this placement allows it.
     * Does not apply the load frequency counter. When the UI continues is `navigateOn` on the config row.
     */
    fun show(activity: Activity?, key: InterstitialAdKey): Flow<AdShowResult> {
        val placement = InterstitialAdConfig[key]
        val request = InterstitialShowRequest(
            key = key,
            activity = activity,
            isAppPurchased = sharedPrefManager.isAppPurchased,
        )
        return when (val validation = validator.validateShow(request)) {
            is InterstitialShowValidation.Valid -> controller.show(
                key = key,
                activity = validation.activity,
                canUseAvailableFallback = placement.canUseAvailableFallback,
            )

            is InterstitialShowValidation.Invalid -> flowOf(AdShowResult.Failed(validation.reason))
        }
    }

    /** Drop the cached / in-flight ad for [key] only. */
    fun destroy(key: InterstitialAdKey) {
        controller.destroy(key)
    }

    /** Drop every cached / in-flight interstitial. */
    fun destroyAll() {
        controller.destroyAll()
    }
}