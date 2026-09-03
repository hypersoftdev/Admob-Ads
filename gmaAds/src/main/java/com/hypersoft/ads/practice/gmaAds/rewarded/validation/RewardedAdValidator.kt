package com.hypersoft.ads.practice.gmaAds.rewarded.validation

import android.app.Activity
import android.content.Context
import android.util.Log
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.extensions.isSafeForAd
import com.hypersoft.ads.practice.gmaAds.common.logMessage
import com.hypersoft.ads.practice.gmaAds.rewarded.RewardedAdKey
import com.hypersoft.ads.practice.gmaAds.rewarded.request.RewardedLoadRequest
import com.hypersoft.ads.practice.gmaAds.rewarded.request.RewardedShowRequest

internal sealed interface RewardedLoadValidation {
    data class Valid(val context: Context, val adUnitId: String) : RewardedLoadValidation
    data class Invalid(val reason: AdFailureReason) : RewardedLoadValidation
}

internal sealed interface RewardedShowValidation {
    data class Valid(val activity: Activity) : RewardedShowValidation
    data class Invalid(val reason: AdFailureReason) : RewardedShowValidation
}

internal class RewardedAdValidator {

    fun validateLoad(request: RewardedLoadRequest): RewardedLoadValidation {
        val key = request.key
        val adUnitId = request.adUnitId?.trim().orEmpty()
        return when {
            request.isAppPurchased -> invalid(key, AdFailureReason.Premium)
            !request.isEnabledByRemote -> invalid(key, AdFailureReason.RemoteDisabled)
            !request.isInternetConnected -> invalid(key, AdFailureReason.NoInternet)
            adUnitId.isEmpty() -> invalid(key, AdFailureReason.EmptyAdUnitId)
            else -> RewardedLoadValidation.Valid(request.context, adUnitId)
        }
    }

    fun validateShow(request: RewardedShowRequest): RewardedShowValidation {
        val key = request.key
        val activity = request.activity
        return when {
            request.isAppPurchased -> invalidShow(key, AdFailureReason.Premium)
            activity == null || !activity.isSafeForAd() -> {
                invalidShow(key, AdFailureReason.InvalidActivity)
            }
            else -> RewardedShowValidation.Valid(activity)
        }
    }

    private fun invalid(key: RewardedAdKey, reason: AdFailureReason): RewardedLoadValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> rewarded -> validateLoad: ${reason.logMessage}")
        return RewardedLoadValidation.Invalid(reason)
    }

    private fun invalidShow(key: RewardedAdKey, reason: AdFailureReason): RewardedShowValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> rewarded -> validateShow: ${reason.logMessage}")
        return RewardedShowValidation.Invalid(reason)
    }
}