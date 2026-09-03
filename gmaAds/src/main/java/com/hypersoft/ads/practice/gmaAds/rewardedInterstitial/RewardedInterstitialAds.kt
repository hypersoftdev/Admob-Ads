package com.hypersoft.ads.practice.gmaAds.rewardedInterstitial

import android.app.Activity
import android.content.Context
import com.hypersoft.ads.practice.core.platform.InternetManager
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
import com.hypersoft.ads.practice.gmaAds.common.AdsSdk
import com.hypersoft.ads.practice.gmaAds.common.RewardedShowResult
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.controller.RewardedInterstitialAdController
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.request.RewardedInterstitialLoadRequest
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.request.RewardedInterstitialShowRequest
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.validation.RewardedInterstitialAdValidator
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.validation.RewardedInterstitialLoadValidation
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.validation.RewardedInterstitialShowValidation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Public rewarded interstitial API: `adsManager.rewardedInterstitial.load / show / destroy`.
 *
 * Placement policy lives in [RewardedInterstitialAdConfig]. This class only runs the pipeline.
 */
class RewardedInterstitialAds internal constructor(
    context: Context,
    private val adsSdk: AdsSdk,
    private val sharedPrefManager: SharedPrefManager,
    private val internetManager: InternetManager,
    private val validator: RewardedInterstitialAdValidator,
    private val controller: RewardedInterstitialAdController,
) {

    private val context: Context = context.applicationContext

    /**
     * Request an ad for [key].
     *
     * Order: init GMA SDK → read [RewardedInterstitialAdConfig] → validate
     * (premium / RC / internet / ad unit) → controller load.
     * Controller may skip when a fallback is already loaded or still in flight.
     */
    fun load(key: RewardedInterstitialAdKey): Flow<AdLoadResult> = flow {
        try {
            adsSdk.initialize()
        } catch (e: TimeoutCancellationException) {
            emit(AdLoadResult.Failed(AdFailureReason.Sdk(0, e.message ?: "SDK initialize timeout")))
            return@flow
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(AdLoadResult.Failed(AdFailureReason.Sdk(0, e.message ?: "SDK initialize failed")))
            return@flow
        }

        val placement = RewardedInterstitialAdConfig[key]
        val request = RewardedInterstitialLoadRequest(
            key = key,
            context = context,
            adUnitId = context.getString(placement.adUnitResId),
            isEnabledByRemote = placement.isEnabled(sharedPrefManager),
            isInternetConnected = internetManager.isInternetConnected,
            isAppPurchased = sharedPrefManager.isAppPurchased,
        )
        when (val validation = validator.validateLoad(request)) {
            is RewardedInterstitialLoadValidation.Valid -> emitAll(
                controller.load(key, validation.context, validation.adUnitId),
            )

            is RewardedInterstitialLoadValidation.Invalid -> emit(AdLoadResult.Failed(validation.reason))
        }
    }

    /**
     * Show the ad for [key], or a fallback if this placement allows it.
     * Emits [RewardedShowResult.Closed] with `rewardGranted` when the ad is dismissed.
     */
    fun show(activity: Activity?, key: RewardedInterstitialAdKey): Flow<RewardedShowResult> {
        val placement = RewardedInterstitialAdConfig[key]
        val request = RewardedInterstitialShowRequest(
            key = key,
            activity = activity,
            isAppPurchased = sharedPrefManager.isAppPurchased,
        )
        return when (val validation = validator.validateShow(request)) {
            is RewardedInterstitialShowValidation.Valid -> controller.show(
                key = key,
                activity = validation.activity,
                canUseAvailableFallback = placement.canUseAvailableFallback,
            )

            is RewardedInterstitialShowValidation.Invalid -> flowOf(RewardedShowResult.Failed(validation.reason))
        }
    }

    /** Drop the cached / in-flight ad for [key] only. */
    fun destroy(key: RewardedInterstitialAdKey) {
        controller.destroy(key)
    }

    /** Drop every cached / in-flight rewarded interstitial. */
    fun destroyAll() {
        controller.destroyAll()
    }
}