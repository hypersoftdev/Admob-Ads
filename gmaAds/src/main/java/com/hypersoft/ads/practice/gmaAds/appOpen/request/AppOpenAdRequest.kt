package com.hypersoft.ads.practice.gmaAds.appOpen.request

import android.app.Activity
import android.content.Context
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAdKey

internal data class AppOpenLoadRequest(
    val key: AppOpenAdKey,
    val context: Context,
    val adUnitId: String?,
    val isEnabledByRemote: Boolean,
    val isInternetConnected: Boolean,
    val isAppPurchased: Boolean,
)

internal data class AppOpenShowRequest(
    val key: AppOpenAdKey,
    val activity: Activity?,
    val isAppPurchased: Boolean,
)