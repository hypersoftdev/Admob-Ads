package com.hypersoft.admobads.oldPackage.adsconfig.utils

import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
object AdsConstants {
    var mAppOpenAd: AppOpenAd? = null
    var rewardedAd: RewardedAd? = null
    var rewardedInterAd: RewardedInterstitialAd? = null
    var mInterstitialAd: InterstitialAd? = null
    var preloadNativeAd: NativeAd? = null

    var isOpenAdLoading = false
    var isRewardedLoading = false
    var isRewardedInterLoading = false
    var isInterLoading = false
    var isNativeLoading = false


    fun reset(){
        mAppOpenAd = null
        rewardedAd = null
        rewardedInterAd = null
        mInterstitialAd = null
        preloadNativeAd?.destroy()
        preloadNativeAd = null

        isOpenAdLoading = false
        isRewardedLoading = false
        isRewardedInterLoading = false
        isInterLoading = false
        isNativeLoading = false
    }
}