package com.hypersoft.admobads.adsconfig.rewarded.callbacks

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
interface RewardedOnShowCallBack {
    fun onAdDismissedFullScreenContent(){}
    fun onAdFailedToShowFullScreenContent(){}
    fun onAdImpression(){}
    fun onAdShowedFullScreenContent(){}
    fun onUserEarnedReward(){}
}