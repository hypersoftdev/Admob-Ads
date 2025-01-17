package com.hypersoft.admobads.di

import com.hypersoft.admobads.ads.appOpen.application.AppOpenAdManager
import com.hypersoft.admobads.ads.appOpen.screen.AppOpenAdsConfig
import com.hypersoft.admobads.ads.interstitial.InterstitialAdsConfig
import com.hypersoft.admobads.utilities.firebase.RemoteConfiguration
import com.hypersoft.admobads.utilities.manager.InternetManager
import com.hypersoft.admobads.utilities.manager.SharedPreferenceUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class DIComponent : KoinComponent {

    // Utils
    val sharedPreferenceUtils by inject<SharedPreferenceUtils>()

    // Managers
    val internetManager by inject<InternetManager>()

    // Remote Configuration
    val remoteConfiguration by inject<RemoteConfiguration>()

    // Admob
    val appOpenAdManager by inject<AppOpenAdManager>()
    val appOpenAdsConfig by inject<AppOpenAdsConfig>()

    val interstitialAdsConfig by inject<InterstitialAdsConfig>()
}