package com.hypersoft.admobads.adsconfig.callbacks

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
interface RewardedOnLoadCallBack {
    fun onAdFailedToLoad(adError:String)
    fun onAdLoaded()
    fun onPreloaded()
}