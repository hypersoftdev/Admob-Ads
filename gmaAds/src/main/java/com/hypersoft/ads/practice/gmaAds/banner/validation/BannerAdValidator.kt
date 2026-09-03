package com.hypersoft.ads.practice.gmaAds.banner.validation

import android.app.Activity
import android.util.Log
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.banner.BannerAdKey
import com.hypersoft.ads.practice.gmaAds.banner.model.BannerFormat
import com.hypersoft.ads.practice.gmaAds.banner.model.isCompatibleWith
import com.hypersoft.ads.practice.gmaAds.banner.request.BannerLoadRequest
import com.hypersoft.ads.practice.gmaAds.banner.request.BannerShowRequest
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.logMessage

internal sealed interface BannerLoadValidation {
    data class Valid(
        val activity: Activity,
        val adUnitId: String,
        val adWidthDp: Int,
        val format: BannerFormat,
        val maxHeightDp: Int,
    ) : BannerLoadValidation

    data class Invalid(val reason: AdFailureReason) : BannerLoadValidation
}

internal sealed interface BannerShowValidation {
    data object Valid : BannerShowValidation
    data class Invalid(val reason: AdFailureReason) : BannerShowValidation
}

internal class BannerAdValidator {

    fun validateLoad(request: BannerLoadRequest): BannerLoadValidation {
        val key = request.key
        val adUnitId = request.adUnitId?.trim().orEmpty()
        return when {
            request.isAppPurchased -> invalid(key, AdFailureReason.Premium)
            !request.isEnabledByRemote -> invalid(key, AdFailureReason.RemoteDisabled)
            !request.isInternetConnected -> invalid(key, AdFailureReason.NoInternet)
            !request.isActivitySafe -> invalid(key, AdFailureReason.InvalidActivity)
            adUnitId.isEmpty() -> invalid(key, AdFailureReason.EmptyAdUnitId)
            !request.format.isCompatibleWith(request.slot) -> invalid(
                key,
                AdFailureReason.Sdk(0, "Incompatible banner format ${request.format} for slot ${request.slot}"),
            )
            else -> BannerLoadValidation.Valid(
                activity = request.activity,
                adUnitId = adUnitId,
                adWidthDp = request.adWidthDp,
                format = request.format,
                maxHeightDp = request.maxHeightDp,
            )
        }
    }

    fun validateShow(request: BannerShowRequest): BannerShowValidation {
        val key = request.key
        return when {
            request.isAppPurchased -> invalidShow(key, AdFailureReason.Premium)
            else -> BannerShowValidation.Valid
        }
    }

    private fun invalid(key: BannerAdKey, reason: AdFailureReason): BannerLoadValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> banner -> validateLoad: ${reason.logMessage}")
        return BannerLoadValidation.Invalid(reason)
    }

    private fun invalidShow(key: BannerAdKey, reason: AdFailureReason): BannerShowValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> banner -> validateShow: ${reason.logMessage}")
        return BannerShowValidation.Invalid(reason)
    }
}