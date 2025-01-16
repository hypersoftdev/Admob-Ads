package com.hypersoft.admobads.newPackage.ads.interstitial.data.repositories

import com.google.android.gms.ads.interstitial.InterstitialAd
import com.hypersoft.admobads.newPackage.ads.interstitial.data.dataSources.remote.DataSourceRemoteInterstitial
import com.hypersoft.admobads.newPackage.ads.interstitial.domain.repositories.RepositoryInterstitial
import kotlinx.coroutines.flow.Flow

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class RepositoryInterstitialImpl(private val dataSourceRemoteInterstitial: DataSourceRemoteInterstitial) : RepositoryInterstitial {

    override fun fetchInterAd(adKey: String, adId: String): Flow<InterstitialAd?> {
        return dataSourceRemoteInterstitial.fetchInterAd(adKey, adId)
    }

    fun showInterAd(ad: InterstitialAd) {}
}