package com.hypersoft.admobads.adsconfig.callbacks

interface RewardedOnShowCallBack {
    fun onAdClicked()
    fun onAdDismissedFullScreenContent()
    fun onAdFailedToShowFullScreenContent()
    fun onAdImpression()
    fun onAdShowedFullScreenContent()
    fun onUserEarnedReward()
}