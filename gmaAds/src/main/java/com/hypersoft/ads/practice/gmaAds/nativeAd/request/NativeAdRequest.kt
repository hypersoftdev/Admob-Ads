package com.hypersoft.ads.practice.gmaAds.nativeAd.request

import android.content.Context
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey

internal data class NativeLoadRequest(
    val key: NativeAdKey,
    val context: Context,
    val adUnitId: String?,
    val isEnabledByRemote: Boolean,
    val isInternetConnected: Boolean,
    val isAppPurchased: Boolean,
)

internal data class NativeShowRequest(
    val key: NativeAdKey,
    val isAppPurchased: Boolean,
)