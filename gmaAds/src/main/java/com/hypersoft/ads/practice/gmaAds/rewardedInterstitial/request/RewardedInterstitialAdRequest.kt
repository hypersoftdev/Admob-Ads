package com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.request

import android.app.Activity
import android.content.Context
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.RewardedInterstitialAdKey

internal data class RewardedInterstitialLoadRequest(
    val key: RewardedInterstitialAdKey,
    val context: Context,
    val adUnitId: String?,
    val isEnabledByRemote: Boolean,
    val isInternetConnected: Boolean,
    val isAppPurchased: Boolean,
)

internal data class RewardedInterstitialShowRequest(
    val key: RewardedInterstitialAdKey,
    val activity: Activity?,
    val isAppPurchased: Boolean,
)