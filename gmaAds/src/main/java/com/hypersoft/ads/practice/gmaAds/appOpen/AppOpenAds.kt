package com.hypersoft.ads.practice.gmaAds.appOpen

import android.app.Activity
import android.content.Context
import com.hypersoft.ads.practice.core.platform.InternetManager
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager
import com.hypersoft.ads.practice.gmaAds.appOpen.controller.AppOpenAdController
import com.hypersoft.ads.practice.gmaAds.appOpen.request.AppOpenLoadRequest
import com.hypersoft.ads.practice.gmaAds.appOpen.request.AppOpenShowRequest
import com.hypersoft.ads.practice.gmaAds.appOpen.validation.AppOpenAdValidator
import com.hypersoft.ads.practice.gmaAds.appOpen.validation.AppOpenLoadValidation
import com.hypersoft.ads.practice.gmaAds.appOpen.validation.AppOpenShowValidation
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
import com.hypersoft.ads.practice.gmaAds.common.AdShowResult
import com.hypersoft.ads.practice.gmaAds.common.AdsSdk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Public app-open API: `adsManager.appOpen.load / show / destroy`.
 *
 * Placement policy lives in [AppOpenAdConfig]. This class only runs the pipeline.
 * [shouldBlock] is for the process-lifecycle observer (splash / premium), not for between-screen show.
 */
class AppOpenAds internal constructor(
    context: Context,
    private val adsSdk: AdsSdk,
    private val sharedPrefManager: SharedPrefManager,
    private val internetManager: InternetManager,
    private val validator: AppOpenAdValidator,
    private val controller: AppOpenAdController,
) {

    private val context: Context = context.applicationContext

    @Volatile
    var shouldBlock: Boolean = true

    /**
     * Request an ad for [key].
     *
     * Order: init GMA SDK → read [AppOpenAdConfig] → validate
     * (premium / RC / internet / ad unit) → controller load.
     */
    fun load(key: AppOpenAdKey): Flow<AdLoadResult> = flow {
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

        val placement = AppOpenAdConfig[key]
        val request = AppOpenLoadRequest(
            key = key,
            context = context,
            adUnitId = context.getString(placement.adUnitResId),
            isEnabledByRemote = placement.isEnabled(sharedPrefManager),
            isInternetConnected = internetManager.isInternetConnected,
            isAppPurchased = sharedPrefManager.isAppPurchased,
        )
        when (val validation = validator.validateLoad(request)) {
            is AppOpenLoadValidation.Valid -> emitAll(
                controller.load(key, validation.context, validation.adUnitId),
            )

            is AppOpenLoadValidation.Invalid -> emit(AdLoadResult.Failed(validation.reason))
        }
    }

    /**
     * Show the ad for [key], or a fallback if this placement allows it.
     * When the UI continues is `navigateOn` on the config row.
     */
    fun show(activity: Activity?, key: AppOpenAdKey): Flow<AdShowResult> {
        val placement = AppOpenAdConfig[key]
        val request = AppOpenShowRequest(
            key = key,
            activity = activity,
            isAppPurchased = sharedPrefManager.isAppPurchased,
        )
        return when (val validation = validator.validateShow(request)) {
            is AppOpenShowValidation.Valid -> controller.show(
                key = key,
                activity = validation.activity,
                canUseAvailableFallback = placement.canUseAvailableFallback,
            )

            is AppOpenShowValidation.Invalid -> flowOf(AdShowResult.Failed(validation.reason))
        }
    }

    /** Drop the cached / in-flight ad for [key] only. */
    fun destroy(key: AppOpenAdKey) {
        controller.destroy(key)
    }

    /** Drop every cached / in-flight app open. */
    fun destroyAll() {
        controller.destroyAll()
    }
}