package com.hypersoft.admobads.adsconfig.natives.callbacks

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
interface NativeCallBack {
    fun onAdFailedToLoad(adError:String){}
    fun onAdLoaded(){}
    fun onAdImpression(){}
    fun onPreloaded(){}
}