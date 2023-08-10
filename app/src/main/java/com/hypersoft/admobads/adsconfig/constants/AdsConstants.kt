package com.hypersoft.admobads.adsconfig.constants

import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd

object AdsConstants {
    var mAppOpenAd: AppOpenAd? = null
    var rewardedAd: RewardedAd? = null
    var mInterstitialAd: InterstitialAd? = null
    var adMobPreloadNativeAd: NativeAd? = null

    var isInterstitialLoading = false
    var isRewardedLoading = false
    var isNativeLoading = false
    var isOpenAdLoading = false

    fun reset(){
        mAppOpenAd = null
        mInterstitialAd = null
        rewardedAd = null
        adMobPreloadNativeAd?.destroy()
        adMobPreloadNativeAd = null

        isInterstitialLoading = false
        isNativeLoading = false
        isRewardedLoading = false
        var isOpenAdLoading = false
    }
}