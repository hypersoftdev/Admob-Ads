package com.hypersoft.admobads.newPackage.ads.interstitial.data.repositories

import com.hypersoft.admobads.newPackage.ads.interstitial.data.dataSources.remote.DataSourceRemoteInterstitial
import com.hypersoft.admobads.newPackage.ads.interstitial.data.entities.ItemInterstitialAd
import com.hypersoft.admobads.newPackage.ads.interstitial.domain.repositories.RepositoryInterstitial

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class RepositoryInterstitialImpl(private val dataSourceRemoteInterstitial: DataSourceRemoteInterstitial) : RepositoryInterstitial {


    fun fetchInterAd(adKey: String, adId: String, callback: (ItemInterstitialAd?) -> Unit) {
        dataSourceRemoteInterstitial.fetchInterAd(adKey, adId, callback)
    }


}