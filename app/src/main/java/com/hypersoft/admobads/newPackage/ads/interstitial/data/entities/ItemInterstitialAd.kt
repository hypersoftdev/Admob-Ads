package com.hypersoft.admobads.newPackage.ads.interstitial.data.entities

import com.google.android.gms.ads.interstitial.InterstitialAd

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

data class ItemInterstitialAd(
    val adId: String,
    val interstitialAd: InterstitialAd,
    val impressionReceived: Boolean = false
)