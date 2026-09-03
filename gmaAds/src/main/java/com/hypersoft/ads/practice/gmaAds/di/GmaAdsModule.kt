package com.hypersoft.ads.practice.gmaAds.di

import com.hypersoft.ads.practice.gmaAds.AdsManager
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAds
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenLifecycle
import com.hypersoft.ads.practice.gmaAds.appOpen.controller.AppOpenAdController
import com.hypersoft.ads.practice.gmaAds.appOpen.validation.AppOpenAdValidator
import com.hypersoft.ads.practice.gmaAds.banner.BannerAds
import com.hypersoft.ads.practice.gmaAds.banner.controller.BannerAdController
import com.hypersoft.ads.practice.gmaAds.banner.validation.BannerAdValidator
import com.hypersoft.ads.practice.gmaAds.common.AdsSdk
import com.hypersoft.ads.practice.gmaAds.common.FullscreenAdGate
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAds
import com.hypersoft.ads.practice.gmaAds.interstitial.controller.InterstitialAdController
import com.hypersoft.ads.practice.gmaAds.interstitial.counter.InterstitialLoadCounter
import com.hypersoft.ads.practice.gmaAds.interstitial.validation.InterstitialAdValidator
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAds
import com.hypersoft.ads.practice.gmaAds.nativeAd.controller.NativeAdController
import com.hypersoft.ads.practice.gmaAds.nativeAd.validation.NativeAdValidator
import com.hypersoft.ads.practice.gmaAds.rewarded.RewardedAds
import com.hypersoft.ads.practice.gmaAds.rewarded.controller.RewardedAdController
import com.hypersoft.ads.practice.gmaAds.rewarded.validation.RewardedAdValidator
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.RewardedInterstitialAds
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.controller.RewardedInterstitialAdController
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.validation.RewardedInterstitialAdValidator
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.lazyModule

val gmaAdsModule = lazyModule {

    single { AdsSdk(context = get()) }
    single { FullscreenAdGate() }

    /* ------------------------------------------- App Open ------------------------------------------- */

    single { AppOpenAdValidator() }
    single { AppOpenAdController(fullscreenAdGate = get()) }
    single { AppOpenAds(context = get(), adsSdk = get(), sharedPrefManager = get(), internetManager = get(), validator = get(), controller = get()) }
    single { AppOpenLifecycle(application = androidApplication(), appOpenAds = get(), fullscreenAdGate = get()) }

    /* ------------------------------------------- Banner ------------------------------------------- */

    single { BannerAdValidator() }
    single { BannerAdController() }
    single { BannerAds(adsSdk = get(), sharedPrefManager = get(), internetManager = get(), validator = get(), controller = get()) }

    /* ------------------------------------------- Interstitial ------------------------------------------- */

    single { InterstitialAdValidator() }
    single { InterstitialAdController(fullscreenAdGate = get()) }
    single { InterstitialLoadCounter(sharedPrefManager = get()) }
    single { InterstitialAds(context = get(), adsSdk = get(), sharedPrefManager = get(), internetManager = get(), validator = get(), counter = get(), controller = get()) }

    /* ------------------------------------------- Native ------------------------------------------- */

    single { NativeAdValidator() }
    single { NativeAdController() }
    single { NativeAds(context = get(), adsSdk = get(), sharedPrefManager = get(), internetManager = get(), validator = get(), controller = get()) }

    /* ------------------------------------------- Rewarded ------------------------------------------- */

    single { RewardedAdValidator() }
    single { RewardedAdController(fullscreenAdGate = get()) }
    single { RewardedAds(context = get(), adsSdk = get(), sharedPrefManager = get(), internetManager = get(), validator = get(), controller = get()) }

    /* ------------------------------------------- Rewarded Interstitial ------------------------------------------- */

    single { RewardedInterstitialAdValidator() }
    single { RewardedInterstitialAdController(fullscreenAdGate = get()) }
    single { RewardedInterstitialAds(context = get(), adsSdk = get(), sharedPrefManager = get(), internetManager = get(), validator = get(), controller = get()) }

    /* ------------------------------------------- Facade ------------------------------------------- */

    single { AdsManager(appOpen = get(), banner = get(), interstitial = get(), native = get(), rewarded = get(), rewardedInterstitial = get(), adsSdk = get(), appOpenLifecycle = get()) }
}