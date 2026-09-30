package com.hypersoft.ads.practice.gmaAds.banner

import android.app.Activity
import android.util.Log
import android.view.View
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.core.platform.InternetManager
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager
import com.hypersoft.ads.practice.gmaAds.banner.controller.BannerAdController
import com.hypersoft.ads.practice.gmaAds.banner.request.BannerLoadRequest
import com.hypersoft.ads.practice.gmaAds.banner.request.BannerShowRequest
import com.hypersoft.ads.practice.gmaAds.banner.validation.BannerAdValidator
import com.hypersoft.ads.practice.gmaAds.banner.validation.BannerLoadValidation
import com.hypersoft.ads.practice.gmaAds.banner.validation.BannerShowValidation
import com.hypersoft.ads.practice.gmaAds.banner.view.BannerContainerView
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
import com.hypersoft.ads.practice.gmaAds.common.AdsSdk
import com.hypersoft.ads.practice.gmaAds.common.BannerShowResult
import com.hypersoft.ads.practice.gmaAds.common.extensions.hostActivity
import com.hypersoft.ads.practice.gmaAds.common.extensions.isSafeForAd
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Public banner API: `adsManager.banner.load / show / destroy`.
 *
 * App fragments call `loadBannerAd(key, container)` (load then show on that screen).
 * Placement policy lives in [BannerAdConfig]. This class only runs the pipeline.
 */
class BannerAds internal constructor(
    private val adsSdk: AdsSdk,
    private val sharedPrefManager: SharedPrefManager,
    private val internetManager: InternetManager,
    private val validator: BannerAdValidator,
    private val controller: BannerAdController,
) {

    fun load(activity: Activity, key: BannerAdKey, adWidthDp: Int): Flow<AdLoadResult> = flow {
        val placement = BannerAdConfig[key]
        val request = BannerLoadRequest(
            key = key,
            activity = activity,
            adUnitId = activity.getString(placement.adUnitResId),
            adWidthDp = adWidthDp,
            slot = placement.slot,
            format = placement.format(sharedPrefManager),
            maxHeightDp = placement.maxHeightDp,
            isEnabledByRemote = placement.isEnabled(sharedPrefManager),
            isInternetConnected = internetManager.isInternetConnected,
            isAppPurchased = sharedPrefManager.isAppPurchased,
            isActivitySafe = activity.isSafeForAd(),
        )
        when (val validation = validator.validateLoad(request)) {
            is BannerLoadValidation.Invalid -> emit(AdLoadResult.Failed(validation.reason))
            is BannerLoadValidation.Valid -> {
                val sdkFailure = adsSdk.initializeForLoad()
                if (sdkFailure != null) {
                    emit(sdkFailure)
                    return@flow
                }
                emitAll(
                    controller.load(
                        key = key,
                        activity = validation.activity,
                        adUnitId = validation.adUnitId,
                        adWidthDp = validation.adWidthDp,
                        format = validation.format,
                        maxHeightDp = validation.maxHeightDp,
                    ),
                )
            }
        }
    }

    fun show(key: BannerAdKey, container: BannerContainerView): Flow<BannerShowResult> = flow {
        val placement = BannerAdConfig[key]
        val request = BannerShowRequest(
            key = key,
            isAppPurchased = sharedPrefManager.isAppPurchased,
        )
        when (val validation = validator.validateShow(request)) {
            is BannerShowValidation.Valid -> {
                val hostActivity = (container as? View)?.hostActivity()
                if (hostActivity != null && !hostActivity.isSafeForAd()) {
                    Log.e(TAG_ADS, "${key.value} -> banner -> show: Failed: activity finishing/destroyed")
                    emit(BannerShowResult.Failed(AdFailureReason.InvalidActivity))
                    return@flow
                }
                val target = controller.takeForShow(key, placement.canUseAvailableFallback)
                if (target == null) {
                    emit(BannerShowResult.NotAvailable)
                } else if (hostActivity != null && !hostActivity.isSafeForAd()) {
                    Log.e(TAG_ADS, "${key.value} -> banner -> show: Failed: activity finishing/destroyed")
                    emit(BannerShowResult.Failed(AdFailureReason.InvalidActivity))
                } else {
                    when {
                        target.sourceKey != key ->
                            Log.d(TAG_ADS, "${key.value} -> banner -> show: Fallback from ${target.sourceKey.value}")

                        target.isReshow ->
                            Log.d(TAG_ADS, "${key.value} -> banner -> show: Reshowing cached ad")

                        else ->
                            Log.d(TAG_ADS, "${key.value} -> banner -> show: Showing")
                    }
                    container.setAdView(target.adView)
                    emit(BannerShowResult.Rendered)
                }
            }

            is BannerShowValidation.Invalid -> emit(BannerShowResult.Failed(validation.reason))
        }
    }

    fun pause(key: BannerAdKey) {
        controller.pause(key)
    }

    fun resume(key: BannerAdKey) {
        controller.resume(key)
    }

    /**
     * Drop the cached / displayed banner for [key].
     *
     * When [destroyIfImpressionReceived] is true, the [com.google.android.gms.ads.AdView]
     * is destroyed only if that placement already recorded an impression; otherwise this
     * is a no-op so an unused ad can still be shown as a fallback. Default is false
     * (always destroy).
     */
    fun destroy(key: BannerAdKey, destroyIfImpressionReceived: Boolean = false) {
        controller.destroy(key, destroyIfImpressionReceived)
    }

    /**
     * Drop every cached / displayed banner.
     *
     * When [destroyIfImpressionReceived] is true, ads without an impression are left
     * for fallback. Default is false (always destroy all).
     */
    fun destroyAll(destroyIfImpressionReceived: Boolean = false) {
        controller.destroyAll(destroyIfImpressionReceived)
    }
}
