package com.hypersoft.ads.practice.gmaAds.interstitial.validation

import android.app.Activity
import android.content.Context
import android.util.Log
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.extensions.isSafeForAd
import com.hypersoft.ads.practice.gmaAds.common.logMessage
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAdKey
import com.hypersoft.ads.practice.gmaAds.interstitial.request.InterstitialLoadRequest
import com.hypersoft.ads.practice.gmaAds.interstitial.request.InterstitialShowRequest

internal sealed interface InterstitialLoadValidation {
    data class Valid(val context: Context, val adUnitId: String) : InterstitialLoadValidation
    data class Invalid(val reason: AdFailureReason) : InterstitialLoadValidation
}

internal sealed interface InterstitialShowValidation {
    data class Valid(val activity: Activity) : InterstitialShowValidation
    data class Invalid(val reason: AdFailureReason) : InterstitialShowValidation
}

internal class InterstitialAdValidator {

    fun validateLoad(request: InterstitialLoadRequest): InterstitialLoadValidation {
        val key = request.key
        val adUnitId = request.adUnitId?.trim().orEmpty()
        return when {
            request.isAppPurchased -> invalid(key, AdFailureReason.Premium)
            !request.isEnabledByRemote -> invalid(key, AdFailureReason.RemoteDisabled)
            !request.isInternetConnected -> invalid(key, AdFailureReason.NoInternet)
            adUnitId.isEmpty() -> invalid(key, AdFailureReason.EmptyAdUnitId)
            else -> InterstitialLoadValidation.Valid(request.context, adUnitId)
        }
    }

    fun validateShow(request: InterstitialShowRequest): InterstitialShowValidation {
        val key = request.key
        val activity = request.activity
        return when {
            request.isAppPurchased -> invalidShow(key, AdFailureReason.Premium)
            activity == null || !activity.isSafeForAd() -> {
                invalidShow(key, AdFailureReason.InvalidActivity)
            }
            else -> InterstitialShowValidation.Valid(activity)
        }
    }

    private fun invalid(key: InterstitialAdKey, reason: AdFailureReason): InterstitialLoadValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> interstitial -> validateLoad: ${reason.logMessage}")
        return InterstitialLoadValidation.Invalid(reason)
    }

    private fun invalidShow(key: InterstitialAdKey, reason: AdFailureReason): InterstitialShowValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> interstitial -> validateShow: ${reason.logMessage}")
        return InterstitialShowValidation.Invalid(reason)
    }
}