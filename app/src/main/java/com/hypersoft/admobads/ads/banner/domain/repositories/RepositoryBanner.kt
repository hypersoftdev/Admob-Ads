package com.hypersoft.admobads.ads.banner.domain.repositories

import com.hypersoft.admobads.ads.banner.data.entities.ItemBannerAd

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

interface RepositoryBanner {
    fun fetchBannerAd(adKey: String, adId: String, callback: (ItemBannerAd?) -> Unit)
    fun destroyBanner(adKey: String): Boolean
}