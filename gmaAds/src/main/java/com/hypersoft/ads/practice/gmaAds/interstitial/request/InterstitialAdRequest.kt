package com.hypersoft.ads.practice.gmaAds.interstitial.request

import android.app.Activity
import android.content.Context
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAdKey

internal data class InterstitialLoadRequest(
    val key: InterstitialAdKey,
    val context: Context,
    val adUnitId: String?,
    val isEnabledByRemote: Boolean,
    val isInternetConnected: Boolean,
    val isAppPurchased: Boolean,
)

internal data class InterstitialShowRequest(
    val key: InterstitialAdKey,
    val activity: Activity?,
    val isAppPurchased: Boolean,
)