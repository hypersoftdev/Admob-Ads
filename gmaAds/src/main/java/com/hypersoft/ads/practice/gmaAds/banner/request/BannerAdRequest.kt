package com.hypersoft.ads.practice.gmaAds.banner.request

import android.app.Activity
import com.hypersoft.ads.practice.gmaAds.banner.BannerAdKey
import com.hypersoft.ads.practice.gmaAds.banner.model.BannerFormat
import com.hypersoft.ads.practice.gmaAds.banner.model.BannerSlot

internal data class BannerLoadRequest(
    val key: BannerAdKey,
    val activity: Activity,
    val adUnitId: String?,
    val adWidthDp: Int,
    val slot: BannerSlot,
    val format: BannerFormat,
    val maxHeightDp: Int,
    val isEnabledByRemote: Boolean,
    val isInternetConnected: Boolean,
    val isAppPurchased: Boolean,
    val isActivitySafe: Boolean,
)

internal data class BannerShowRequest(
    val key: BannerAdKey,
    val isAppPurchased: Boolean,
)