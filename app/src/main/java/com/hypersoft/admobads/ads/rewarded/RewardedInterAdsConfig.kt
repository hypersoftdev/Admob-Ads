package com.hypersoft.admobads.ads.rewarded

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import com.hypersoft.admobads.R
import com.hypersoft.admobads.ads.rewarded.callbacks.RewardedOnLoadCallBack
import com.hypersoft.admobads.ads.rewarded.callbacks.RewardedOnShowCallBack
import com.hypersoft.admobads.ads.rewarded.enums.RewardedInterAdKey
import com.hypersoft.admobads.ads.rewarded.managers.RewardedInterManager
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


/**
 * @param context: Can be of application class
 */
class RewardedInterAdsConfig(
    private val context: Context?,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val internetManager: InternetManager
) : RewardedInterManager() {

    fun loadRewardedInterAd(adType: RewardedInterAdKey, listener: RewardedOnLoadCallBack? = null) {
        var rewardedInterAdId = ""
        var isRemoteEnable = false

        when (adType) {
            RewardedInterAdKey.AI_FEATURE -> {
                rewardedInterAdId = getResString(R.string.admob_rewarded_inter_ai_feature_id)
                isRemoteEnable = sharedPreferenceUtils.rcRewardedInterAiFeature != 0
            }
        }

        loadRewardedInter(
            context = context,
            adType = adType.value,
            rewardedInterId = rewardedInterAdId,
            adEnable = isRemoteEnable,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            isInternetConnected = internetManager.isInternetConnected,
            listener = listener
        )
    }

    fun showRewardedInterAd(activity: Activity?, adType: RewardedInterAdKey, listener: RewardedOnShowCallBack? = null) {
        showRewardedInter(
            activity = activity,
            adType = adType.value,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            listener
        )
    }

    private fun getResString(@StringRes resId: Int): String {
        return context?.resources?.getString(resId) ?: ""
    }
}