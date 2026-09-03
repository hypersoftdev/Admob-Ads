package com.hypersoft.ads.practice.gmaAds

import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAds
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenLifecycle
import com.hypersoft.ads.practice.gmaAds.banner.BannerAds
import com.hypersoft.ads.practice.gmaAds.common.AdsSdk
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAds
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAds
import com.hypersoft.ads.practice.gmaAds.rewarded.RewardedAds
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.RewardedInterstitialAds

class AdsManager internal constructor(
    val appOpen: AppOpenAds,
    val banner: BannerAds,
    val interstitial: InterstitialAds,
    val native: NativeAds,
    val rewarded: RewardedAds,
    val rewardedInterstitial: RewardedInterstitialAds,
    private val adsSdk: AdsSdk,
    @Suppress("unused") private val appOpenLifecycle: AppOpenLifecycle,
) {
    suspend fun initialize() = adsSdk.initialize()
}