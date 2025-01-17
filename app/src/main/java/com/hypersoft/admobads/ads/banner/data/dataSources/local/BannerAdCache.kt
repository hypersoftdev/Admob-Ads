package com.hypersoft.admobads.ads.banner.data.dataSources.local

import com.hypersoft.admobads.ads.banner.data.entities.ItemBannerAd
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class BannerAdCache {

    private val adCache = ConcurrentHashMap<String, ItemBannerAd>()

    /**
     * Return cached ad if it exists
     */
    fun getAd(adKey: String): ItemBannerAd? {
        return adCache[adKey]
    }

    /**
     * Return cached ad if it exists
     */
    fun getImpressionFreeAd(adKey: String): ItemBannerAd? {
        return adCache[adKey]?.takeIf { !it.impressionReceived }
    }

    /**
     * Store ad for cache
     */
    fun putAd(adKey: String, itemBannerAd: ItemBannerAd) {
        adCache[adKey] = itemBannerAd
    }

    /**
     * Find any free ad (an ad that hasn't received an impression).
     */
    fun getFreeAd(): ItemBannerAd? {
        return adCache.values.firstOrNull { !it.impressionReceived }
    }

    /**
     *  Only delete if impression is received else if ignore
     */
    fun deleteAd(adKey: String): Boolean {
        adCache[adKey]?.let {
            if (it.impressionReceived) {
                it.adView.destroy()
                adCache.remove(adKey)
                return true
            }
        }
        return false
    }
}