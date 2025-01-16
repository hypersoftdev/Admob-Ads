package com.hypersoft.admobads.newPackage.ads.interstitial.domain.repositories

import com.google.android.gms.ads.interstitial.InterstitialAd
import kotlinx.coroutines.flow.Flow

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

interface RepositoryInterstitial {
    fun fetchInterAd(adKey: String, adId: String): Flow<InterstitialAd?>
}