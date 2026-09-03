package com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.validation

import android.app.Activity
import android.content.Context
import android.util.Log
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.common.AdFailureReason
import com.hypersoft.ads.practice.gmaAds.common.extensions.isSafeForAd
import com.hypersoft.ads.practice.gmaAds.common.logMessage
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.RewardedInterstitialAdKey
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.request.RewardedInterstitialLoadRequest
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.request.RewardedInterstitialShowRequest

internal sealed interface RewardedInterstitialLoadValidation {
    data class Valid(val context: Context, val adUnitId: String) : RewardedInterstitialLoadValidation
    data class Invalid(val reason: AdFailureReason) : RewardedInterstitialLoadValidation
}

internal sealed interface RewardedInterstitialShowValidation {
    data class Valid(val activity: Activity) : RewardedInterstitialShowValidation
    data class Invalid(val reason: AdFailureReason) : RewardedInterstitialShowValidation
}

internal class RewardedInterstitialAdValidator {

    fun validateLoad(request: RewardedInterstitialLoadRequest): RewardedInterstitialLoadValidation {
        val key = request.key
        val adUnitId = request.adUnitId?.trim().orEmpty()
        return when {
            request.isAppPurchased -> invalid(key, AdFailureReason.Premium)
            !request.isEnabledByRemote -> invalid(key, AdFailureReason.RemoteDisabled)
            !request.isInternetConnected -> invalid(key, AdFailureReason.NoInternet)
            adUnitId.isEmpty() -> invalid(key, AdFailureReason.EmptyAdUnitId)
            else -> RewardedInterstitialLoadValidation.Valid(request.context, adUnitId)
        }
    }

    fun validateShow(request: RewardedInterstitialShowRequest): RewardedInterstitialShowValidation {
        val key = request.key
        val activity = request.activity
        return when {
            request.isAppPurchased -> invalidShow(key, AdFailureReason.Premium)
            activity == null || !activity.isSafeForAd() -> {
                invalidShow(key, AdFailureReason.InvalidActivity)
            }
            else -> RewardedInterstitialShowValidation.Valid(activity)
        }
    }

    private fun invalid(key: RewardedInterstitialAdKey, reason: AdFailureReason): RewardedInterstitialLoadValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> rewardedInterstitial -> validateLoad: ${reason.logMessage}")
        return RewardedInterstitialLoadValidation.Invalid(reason)
    }

    private fun invalidShow(key: RewardedInterstitialAdKey, reason: AdFailureReason): RewardedInterstitialShowValidation.Invalid {
        Log.e(TAG_ADS, "${key.value} -> rewardedInterstitial -> validateShow: ${reason.logMessage}")
        return RewardedInterstitialShowValidation.Invalid(reason)
    }
}