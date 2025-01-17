package com.hypersoft.admobads.ads.appOpen.screen

import android.app.Activity
import android.content.Context
import com.hypersoft.admobads.R
import com.hypersoft.admobads.ads.appOpen.screen.callbacks.AppOpenOnLoadCallBack
import com.hypersoft.admobads.ads.appOpen.screen.callbacks.AppOpenOnShowCallBack
import com.hypersoft.admobads.ads.appOpen.screen.enums.AppOpenAdKey
import com.hypersoft.admobads.ads.appOpen.screen.manager.AppOpenManager
import com.hypersoft.admobads.utilities.manager.InternetManager
import com.hypersoft.admobads.utilities.manager.SharedPreferenceUtils

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class AppOpenAdsConfig(
    private val context: Context,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val internetManager: InternetManager
) : AppOpenManager() {

    fun loadAppOpenAd(adType: AppOpenAdKey, listener: AppOpenOnLoadCallBack? = null) {
        var interAdId = ""
        var isRemoteEnable = false

        when (adType) {
            AppOpenAdKey.SPLASH -> {
                interAdId = context.getString(R.string.admob_app_open_id)
                isRemoteEnable = sharedPreferenceUtils.rcAppOpen != 0
            }
        }

        loadAppOpen(
            context = context,
            adType = adType.value,
            appOpenId = interAdId,
            adEnable = isRemoteEnable,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            isInternetConnected = internetManager.isInternetConnected,
            listener = listener
        )
    }

    fun showAppOpenAd(activity: Activity?, adType: AppOpenAdKey, listener: AppOpenOnShowCallBack? = null) {
        showAppOpen(
            activity = activity,
            adType = adType.value,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            listener
        )
    }
}