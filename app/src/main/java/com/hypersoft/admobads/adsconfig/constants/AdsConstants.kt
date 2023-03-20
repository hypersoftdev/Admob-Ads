package com.hypersoft.admobads.adsconfig.constants

import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd

object AdsConstants {
    var mAppOpenAd: AppOpenAd? = null
    var mInterstitialAd: InterstitialAd? = null
    var adMobPreloadNativeAd: NativeAd? = null

    var isInterstitialLoading = false
    var isNativeLoading = false

    fun reset(){
        mAppOpenAd = null
        mInterstitialAd = null
        adMobPreloadNativeAd?.destroy()
        adMobPreloadNativeAd = null

        isInterstitialLoading = false
        isNativeLoading = false
    }
}