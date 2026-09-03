package com.hypersoft.ads.practice.gmaAds.nativeAd.validation

import android.content.Context
import android.util.Log
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.logMessage
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey
import com.hypersoft.ads.practice.gmaAds.nativeAd.request.NativeLoadRequest
import com.hypersoft.ads.practice.gmaAds.nativeAd.request.NativeShowRequest

internal sealed interface NativeLoadValidation {
    data class Valid(val context: Context, val adUnitId: String) : NativeLoadValidation
    data class Invalid(val reason: AdFailureReason) : NativeLoadValidation
}

internal sealed interface NativeShowValidation {
    data object Valid : NativeShowValidation
    data class Invalid(val reason: AdFailureReason) : NativeShowValidation
}

internal class NativeAdValidator {

    fun validateLoad(request: NativeLoadRequest): NativeLoadValidation {
        val key = request.key
        val adUnitId = request.adUnitId?.trim().orEmpty()
        return when {
            request.isAppPurchased -> invalid(key, AdFailureReason.Premium)
            !request.isEnabledByRemote -> invalid(key, AdFailureReason.RemoteDisabled)
            !request.isInternetConnected -> invalid(key, AdFailureReason.NoInternet)
            adUnitId.isEmpty() -> invalid(key, AdFailureReason.EmptyAdUnitId)
            else -> NativeLoadValidation.Valid(request.context, adUnitId)
        }
    }

    fun validateShow(request: NativeShowRequest): NativeShowValidation {
        val key = request.key
        return when {
            request.isAppPurchased -> invalidShow(key, AdFailureReason.Premium)
            else -> NativeShowValidation.Valid
        }
    }

    private fun invalid(key: NativeAdKey, reason: AdFailureReason): NativeLoadValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> native -> validateLoad: ${reason.logMessage}")
        return NativeLoadValidation.Invalid(reason)
    }

    private fun invalidShow(key: NativeAdKey, reason: AdFailureReason): NativeShowValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> native -> validateShow: ${reason.logMessage}")
        return NativeShowValidation.Invalid(reason)
    }
}