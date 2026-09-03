package com.hypersoft.ads.practice.gmaAds.rewarded.request

import android.app.Activity
import android.content.Context
import com.hypersoft.ads.practice.gmaAds.rewarded.RewardedAdKey

internal data class RewardedLoadRequest(
    val key: RewardedAdKey,
    val context: Context,
    val adUnitId: String?,
    val isEnabledByRemote: Boolean,
    val isInternetConnected: Boolean,
    val isAppPurchased: Boolean,
)

internal data class RewardedShowRequest(
    val key: RewardedAdKey,
    val activity: Activity?,
    val isAppPurchased: Boolean,
)