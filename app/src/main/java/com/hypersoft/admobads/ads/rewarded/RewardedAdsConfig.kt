package com.hypersoft.admobads.ads.rewarded

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import com.hypersoft.admobads.R
import com.hypersoft.admobads.ads.rewarded.callbacks.RewardedOnLoadCallBack
import com.hypersoft.admobads.ads.rewarded.callbacks.RewardedOnShowCallBack
import com.hypersoft.admobads.ads.rewarded.enums.RewardedAdKey
import com.hypersoft.admobads.ads.rewarded.managers.RewardedManager
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
class RewardedAdsConfig(
    private val context: Context?,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val internetManager: InternetManager
) : RewardedManager() {

    fun loadRewardedAd(adType: RewardedAdKey, listener: RewardedOnLoadCallBack? = null) {
        var rewardedAdId = ""
        var isRemoteEnable = false

        when (adType) {
            RewardedAdKey.AI_FEATURE -> {
                rewardedAdId = getResString(R.string.admob_rewarded_ai_feature_id)
                isRemoteEnable = sharedPreferenceUtils.rcRewardedAiFeature != 0
            }
        }

        loadRewarded(
            context = context,
            adType = adType.value,
            rewardedId = rewardedAdId,
            adEnable = isRemoteEnable,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            isInternetConnected = internetManager.isInternetConnected,
            listener = listener
        )
    }

    fun showRewardedAd(activity: Activity?, adType: RewardedAdKey, listener: RewardedOnShowCallBack? = null) {
        showRewarded(
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