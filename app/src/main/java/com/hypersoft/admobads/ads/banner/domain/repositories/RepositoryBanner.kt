package com.hypersoft.admobads.ads.banner.domain.repositories

import com.google.android.gms.ads.AdView
import com.hypersoft.admobads.ads.banner.data.entities.ItemBannerAd
import com.hypersoft.admobads.ads.banner.presentation.enums.BannerAdType

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

interface RepositoryBanner {
    fun fetchBannerAd(adKey: String, adId: String, bannerAdType: BannerAdType, adView: AdView, callback: (ItemBannerAd?) -> Unit)
    fun destroyBanner(adKey: String): Boolean
}