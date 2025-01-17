package com.hypersoft.admobads.ads.banner.data.repositories

import android.util.Log
import com.google.android.gms.ads.AdView
import com.hypersoft.admobads.ads.banner.data.dataSources.local.DataSourceLocalBanner
import com.hypersoft.admobads.ads.banner.data.dataSources.remote.DataSourceRemoteBanner
import com.hypersoft.admobads.ads.banner.data.entities.ItemBannerAd
import com.hypersoft.admobads.ads.banner.domain.repositories.RepositoryBanner
import com.hypersoft.admobads.ads.banner.presentation.enums.BannerAdType
import com.hypersoft.admobads.utilities.utils.Constants

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class RepositoryBannerImpl(
    private val dataSourceLocalBanner: DataSourceLocalBanner,
    private val dataSourceRemoteBanner: DataSourceRemoteBanner
) : RepositoryBanner {

    override fun fetchBannerAd(adView: AdView, adKey: String, adId: String, bannerAdType: BannerAdType, callback: (ItemBannerAd?) -> Unit) {

        // Check cache resource
        dataSourceLocalBanner.getCachedBannerAd(adKey)?.let { cachedAd ->
            Log.d(Constants.TAG_ADS, "$adKey -> fetchBannerAd: Reshowing Ad")
            callback.invoke(cachedAd)
            return
        }

        dataSourceRemoteBanner.fetchBannerAd(adView = adView, adKey = adKey, adId = adId, bannerAdType = bannerAdType) { remoteAd ->
            remoteAd?.let {
                dataSourceLocalBanner.putCachedBannerAd(adKey, it)
            }
            callback.invoke(remoteAd)
        }
    }

    override fun destroyBanner(adKey: String): Boolean {
        return dataSourceLocalBanner.destroyBanner(adKey)
    }
}