package com.hypersoft.ads.practice.gmaAds.appOpen.validation

import android.app.Activity
import android.content.Context
import android.util.Log
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAdKey
import com.hypersoft.ads.practice.gmaAds.appOpen.request.AppOpenLoadRequest
import com.hypersoft.ads.practice.gmaAds.appOpen.request.AppOpenShowRequest
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.extensions.isSafeForAd
import com.hypersoft.ads.practice.gmaAds.common.logMessage

internal sealed interface AppOpenLoadValidation {
    data class Valid(val context: Context, val adUnitId: String) : AppOpenLoadValidation
    data class Invalid(val reason: AdFailureReason) : AppOpenLoadValidation
}

internal sealed interface AppOpenShowValidation {
    data class Valid(val activity: Activity) : AppOpenShowValidation
    data class Invalid(val reason: AdFailureReason) : AppOpenShowValidation
}

internal class AppOpenAdValidator {

    fun validateLoad(request: AppOpenLoadRequest): AppOpenLoadValidation {
        val key = request.key
        val adUnitId = request.adUnitId?.trim().orEmpty()
        return when {
            request.isAppPurchased -> invalid(key, AdFailureReason.Premium)
            !request.isEnabledByRemote -> invalid(key, AdFailureReason.RemoteDisabled)
            !request.isInternetConnected -> invalid(key, AdFailureReason.NoInternet)
            adUnitId.isEmpty() -> invalid(key, AdFailureReason.EmptyAdUnitId)
            else -> AppOpenLoadValidation.Valid(request.context, adUnitId)
        }
    }

    fun validateShow(request: AppOpenShowRequest): AppOpenShowValidation {
        val key = request.key
        val activity = request.activity
        return when {
            request.isAppPurchased -> invalidShow(key, AdFailureReason.Premium)
            activity == null || !activity.isSafeForAd() -> {
                invalidShow(key, AdFailureReason.InvalidActivity)
            }
            else -> AppOpenShowValidation.Valid(activity)
        }
    }

    private fun invalid(key: AppOpenAdKey, reason: AdFailureReason): AppOpenLoadValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> appOpen -> validateLoad: ${reason.logMessage}")
        return AppOpenLoadValidation.Invalid(reason)
    }

    private fun invalidShow(key: AppOpenAdKey, reason: AdFailureReason): AppOpenShowValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> appOpen -> validateShow: ${reason.logMessage}")
        return AppOpenShowValidation.Invalid(reason)
    }
}