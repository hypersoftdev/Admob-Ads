package com.hypersoft.admobads.ads.interstitial

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import com.hypersoft.admobads.R
import com.hypersoft.admobads.ads.interstitial.callbacks.InterstitialOnLoadCallBack
import com.hypersoft.admobads.ads.interstitial.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.ads.interstitial.enums.InterAdKey
import com.hypersoft.admobads.ads.interstitial.manager.InterstitialManager
import com.hypersoft.admobads.utilities.manager.InternetManager
import com.hypersoft.admobads.utilities.manager.SharedPreferencesUtils
import com.hypersoft.admobads.utilities.utils.Constants.TAG_ADS

/**
 * Created by: Sohaib Ahmed
 * Date: 1/16/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class InterstitialAdsConfig(
    private val context: Context,
    private val sharedPreferencesUtils: SharedPreferencesUtils,
    private val internetManager: InternetManager
) : InterstitialManager() {

    private val counterMap by lazy { HashMap<String, Int>() }

    fun loadInterstitialAd(adType: InterAdKey, listener: InterstitialOnLoadCallBack? = null) {
        var interAdId = ""
        var isRemoteEnable = false

        when (adType) {
            InterAdKey.ON_BOARDING -> {
                interAdId = getResString(R.string.admob_inter_on_boarding_id)
                isRemoteEnable = sharedPreferencesUtils.rcInterOnBoarding != 0
            }

            InterAdKey.FEATURE -> {
                interAdId = getResString(R.string.admob_inter_splash_id)
                isRemoteEnable = sharedPreferencesUtils.rcInterFeature != 0
            }
        }

        loadInterstitial(
            context = context,
            adType = adType.value,
            interId = interAdId,
            adEnable = isRemoteEnable,
            isAppPurchased = sharedPreferencesUtils.isAppPurchased,
            isInternetConnected = internetManager.isInternetConnected,
            listener = listener
        )
    }

    fun showInterstitialAd(activity: Activity?, adType: InterAdKey, listener: InterstitialOnShowCallBack? = null) {
        showInterstitial(
            activity = activity,
            adType = adType.value,
            isAppPurchased = sharedPreferencesUtils.isAppPurchased,
            listener
        )
    }

    /**
     * @param adType   Key of the Ad, it should be unique id and should be case-sensitive
     * @param remoteCounter   Pass remote counter value, if the value is n, it will load on "n-1". In case of <= 2, it will load everytime
     * @param loadOnStart  Determine whether ad should be load on the very first time or not?
     *
     *  e.g. remoteCounter = 4, ad will  load on "n-1" = 3
     *      if (loadOnStart) {
     *          // 1, 0, 0, 1, 0, 0, 1, 0, 0 ... so on
     *      } else {
     *          // 0, 0, 1, 0, 0, 1, 0, 0, 1 ... so on
     *      }
     */

    fun loadInterstitialAd(adType: InterAdKey, remoteCounter: Int, loadOnStart: Boolean, listener: InterstitialOnLoadCallBack? = null) {
        when (loadOnStart) {
            true -> counterMap.putIfAbsent(adType.value, remoteCounter - 1)
            false -> counterMap.putIfAbsent(adType.value, 0)
        }

        if (counterMap.containsKey(adType.value)) {
            val counter = counterMap[adType.value] ?: 0
            counterMap[adType.value] = counter + 1
            counterMap[adType.value]?.let { currentCounter ->
                Log.d(TAG_ADS, "$adType -> loadInterstitial_Counter ----- Total Counter: $remoteCounter, Current Counter: $currentCounter")
                if (currentCounter >= remoteCounter - 1) {
                    counterMap[adType.value] = 0
                    loadInterstitialAd(adType = adType, listener = listener)
                }
            }
        }
    }

    private fun getResString(@StringRes resId: Int): String {
        return context.resources.getString(resId)
    }
}