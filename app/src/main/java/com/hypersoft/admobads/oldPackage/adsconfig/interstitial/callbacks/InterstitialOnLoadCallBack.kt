package com.hypersoft.admobads.oldPackage.adsconfig.interstitial.callbacks

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
interface InterstitialOnLoadCallBack {
    fun onAdFailedToLoad(adError:String){}
    fun onAdLoaded(){}
    fun onPreloaded(){}
}