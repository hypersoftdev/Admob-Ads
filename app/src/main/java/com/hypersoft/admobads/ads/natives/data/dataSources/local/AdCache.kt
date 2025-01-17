package com.hypersoft.admobads.ads.natives.data.dataSources.local

import com.hypersoft.admobads.ads.natives.data.entities.ItemNativeAd
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class AdCache {

    private val adCache = ConcurrentHashMap<String, ItemNativeAd>()

    /**
     * Return cached ad if it exists
     */
    fun getAd(adKey: String): ItemNativeAd? {
        return adCache[adKey]
    }

    /**
     * Return cached ad if it exists
     */
    fun getImpressionFreeAd(adKey: String): ItemNativeAd? {
        return adCache[adKey]?.takeIf { !it.impressionReceived }
    }

    /**
     * Store ad for cache
     */
    fun putAd(adKey: String, itemNativeAd: ItemNativeAd) {
        adCache[adKey] = itemNativeAd
    }

    /**
     * Find any free ad (an ad that hasn't received an impression).
     */
    fun getFreeAd(): ItemNativeAd? {
        return adCache.values.firstOrNull { !it.impressionReceived }
    }

    /**
     *  Only delete if impression is received else if ignore
     */
    fun deleteAd(adKey: String) {
        adCache[adKey]?.let {
            if (it.impressionReceived) {
                it.nativeAd.destroy()
                adCache.remove(adKey)
            }
        }
    }
}