package com.hypersoft.ads.practice.gmaAds.nativeAd

import android.content.Context
import android.util.Log
import android.view.View
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.core.platform.InternetManager
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
import com.hypersoft.ads.practice.gmaAds.common.AdsSdk
import com.hypersoft.ads.practice.gmaAds.common.NativeShowResult
import com.hypersoft.ads.practice.gmaAds.common.extensions.hostActivity
import com.hypersoft.ads.practice.gmaAds.common.extensions.isSafeForAd
import com.hypersoft.ads.practice.gmaAds.nativeAd.controller.NativeAdController
import com.hypersoft.ads.practice.gmaAds.nativeAd.request.NativeLoadRequest
import com.hypersoft.ads.practice.gmaAds.nativeAd.request.NativeShowRequest
import com.hypersoft.ads.practice.gmaAds.nativeAd.validation.NativeAdValidator
import com.hypersoft.ads.practice.gmaAds.nativeAd.validation.NativeLoadValidation
import com.hypersoft.ads.practice.gmaAds.nativeAd.validation.NativeShowValidation
import com.hypersoft.ads.practice.gmaAds.nativeAd.view.NativeContainerView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Public native API: `adsManager.native.load / show / destroy`.
 *
 * Placement policy lives in [NativeAdConfig]. This class only runs the pipeline.
 */
class NativeAds internal constructor(
    context: Context,
    private val adsSdk: AdsSdk,
    private val sharedPrefManager: SharedPrefManager,
    private val internetManager: InternetManager,
    private val validator: NativeAdValidator,
    private val controller: NativeAdController,
) {

    private val context: Context = context.applicationContext

    fun load(key: NativeAdKey): Flow<AdLoadResult> = flow {
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

        val placement = NativeAdConfig[key]
        val request = NativeLoadRequest(
            key = key,
            context = context,
            adUnitId = context.getString(placement.adUnitResId),
            isEnabledByRemote = placement.isEnabled(sharedPrefManager),
            isInternetConnected = internetManager.isInternetConnected,
            isAppPurchased = sharedPrefManager.isAppPurchased,
        )
        when (val validation = validator.validateLoad(request)) {
            is NativeLoadValidation.Valid -> emitAll(
                controller.load(key, validation.context, validation.adUnitId),
            )

            is NativeLoadValidation.Invalid -> emit(AdLoadResult.Failed(validation.reason))
        }
    }

    fun show(key: NativeAdKey, container: NativeContainerView): Flow<NativeShowResult> = flow {
        val placement = NativeAdConfig[key]
        val request = NativeShowRequest(
            key = key,
            isAppPurchased = sharedPrefManager.isAppPurchased,
        )
        when (val validation = validator.validateShow(request)) {
            is NativeShowValidation.Valid -> {
                val hostActivity = (container as? View)?.hostActivity()
                if (hostActivity != null && !hostActivity.isSafeForAd()) {
                    Log.e(TAG_ADS, "${key.value} -> native -> show: Failed: activity finishing/destroyed")
                    emit(NativeShowResult.Failed(AdFailureReason.InvalidActivity))
                    return@flow
                }
                val target = controller.takeForShow(key, placement.canUseAvailableFallback)
                if (target == null) {
                    emit(NativeShowResult.NotAvailable)
                } else if (hostActivity != null && !hostActivity.isSafeForAd()) {
                    Log.e(TAG_ADS, "${key.value} -> native -> show: Failed: activity finishing/destroyed")
                    emit(NativeShowResult.Failed(AdFailureReason.InvalidActivity))
                } else {
                    when {
                        target.sourceKey != key ->
                            Log.d(TAG_ADS, "${key.value} -> native -> show: Fallback from ${target.sourceKey.value}")

                        target.isReshow ->
                            Log.d(TAG_ADS, "${key.value} -> native -> show: Reshowing cached ad")

                        else ->
                            Log.d(TAG_ADS, "${key.value} -> native -> show: Showing")
                    }
                    container.setNativeAd(target.ad)
                    emit(NativeShowResult.Rendered)
                }
            }

            is NativeShowValidation.Invalid -> emit(NativeShowResult.Failed(validation.reason))
        }
    }

    /**
     * Drop the cached / in-flight native for [key].
     *
     * When [destroyIfImpressionReceived] is true, [NativeAd.destroy] runs only if that
     * placement already recorded an impression; otherwise this is a no-op so an unused
     * ad can still be shown as a fallback. Default is false (always destroy).
     */
    fun destroy(key: NativeAdKey, destroyIfImpressionReceived: Boolean = false) {
        controller.destroy(key, destroyIfImpressionReceived)
    }

    /**
     * Drop every cached / in-flight native.
     *
     * When [destroyIfImpressionReceived] is true, [NativeAd.destroy] runs only for
     * placements that already recorded an impression; unused ads are left in cache
     * for fallback. Default is false (always destroy all).
     */
    fun destroyAll(destroyIfImpressionReceived: Boolean = false) {
        controller.destroyAll(destroyIfImpressionReceived)
    }
}