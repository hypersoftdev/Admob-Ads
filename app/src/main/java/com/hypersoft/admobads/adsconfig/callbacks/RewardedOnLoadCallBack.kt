package com.hypersoft.admobads.adsconfig.callbacks

interface RewardedOnLoadCallBack {
    fun onAdFailedToLoad(adError:String)
    fun onAdLoaded()
    fun onPreloaded()
}