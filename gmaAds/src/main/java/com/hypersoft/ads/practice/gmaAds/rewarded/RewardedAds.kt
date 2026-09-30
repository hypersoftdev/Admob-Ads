package com.hypersoft.ads.practice.gmaAds.rewarded

import android.app.Activity
import android.content.Context
import com.hypersoft.ads.practice.core.platform.InternetManager
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
import com.hypersoft.ads.practice.gmaAds.common.AdsSdk
import com.hypersoft.ads.practice.gmaAds.common.RewardedShowResult
import com.hypersoft.ads.practice.gmaAds.rewarded.controller.RewardedAdController
import com.hypersoft.ads.practice.gmaAds.rewarded.request.RewardedLoadRequest
import com.hypersoft.ads.practice.gmaAds.rewarded.request.RewardedShowRequest
import com.hypersoft.ads.practice.gmaAds.rewarded.validation.RewardedAdValidator
import com.hypersoft.ads.practice.gmaAds.rewarded.validation.RewardedLoadValidation
import com.hypersoft.ads.practice.gmaAds.rewarded.validation.RewardedShowValidation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Public rewarded API: `adsManager.rewarded.load / show / destroy`.
 *
 * Placement policy lives in [RewardedAdConfig]. This class only runs the pipeline.
 */
class RewardedAds internal constructor(
    context: Context,
    private val adsSdk: AdsSdk,
    private val sharedPrefManager: SharedPrefManager,
    private val internetManager: InternetManager,
    private val validator: RewardedAdValidator,
    private val controller: RewardedAdController,
) {

    private val context: Context = context.applicationContext

    /**
     * Request an ad for [key].
     *
     * Order: read [RewardedAdConfig] → validate
     * (premium / RC / internet / ad unit) → init GMA SDK → controller load.
     * Controller may skip when a fallback is already loaded or still in flight.
     */
    fun load(key: RewardedAdKey): Flow<AdLoadResult> = flow {
        val placement = RewardedAdConfig[key]
        val request = RewardedLoadRequest(
            key = key,
            context = context,
            adUnitId = context.getString(placement.adUnitResId),
            isEnabledByRemote = placement.isEnabled(sharedPrefManager),
            isInternetConnected = internetManager.isInternetConnected,
            isAppPurchased = sharedPrefManager.isAppPurchased,
        )
        when (val validation = validator.validateLoad(request)) {
            is RewardedLoadValidation.Invalid -> emit(AdLoadResult.Failed(validation.reason))
            is RewardedLoadValidation.Valid -> {
                val sdkFailure = adsSdk.initializeForLoad()
                if (sdkFailure != null) {
                    emit(sdkFailure)
                    return@flow
                }
                emitAll(controller.load(key, validation.context, validation.adUnitId))
            }
        }
    }

    /**
     * Show the ad for [key], or a fallback if this placement allows it.
     * Emits [RewardedShowResult.Closed] with `rewardGranted` when the ad is dismissed.
     */
    fun show(activity: Activity?, key: RewardedAdKey): Flow<RewardedShowResult> {
        val placement = RewardedAdConfig[key]
        val request = RewardedShowRequest(
            key = key,
            activity = activity,
            isAppPurchased = sharedPrefManager.isAppPurchased,
        )
        return when (val validation = validator.validateShow(request)) {
            is RewardedShowValidation.Valid -> controller.show(
                key = key,
                activity = validation.activity,
                canUseAvailableFallback = placement.canUseAvailableFallback,
            )

            is RewardedShowValidation.Invalid -> flowOf(RewardedShowResult.Failed(validation.reason))
        }
    }

    /** Drop the cached / in-flight ad for [key] only. */
    fun destroy(key: RewardedAdKey) {
        controller.destroy(key)
    }

    /** Drop every cached / in-flight rewarded. */
    fun destroyAll() {
        controller.destroyAll()
    }
}