package com.hypersoft.admobads.ads.natives.data.dataSources.local

import com.hypersoft.admobads.ads.natives.data.entities.ItemNativeAd

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class DataSourceLocalNative {

    private val adCache by lazy { AdCache() }

    /**
     * Fetch a cached native ad for the given key.
     * Returns the cached ad if available and not yet used (impression not received).
     * If no such ad exists, it tries to find a free ad (unused ad without impressions).
     */
    fun getCachedNativeAd(adKey: String): ItemNativeAd? {
        adCache.getImpressionFreeAd(adKey)?.let {
            return it
        }
        return adCache.getFreeAd() ?: adCache.getAd(adKey)
    }

    /**
     * Cache the given native ad using the specified key.
     */
    fun putCachedNativeAd(adKey: String, itemNativeAd: ItemNativeAd) {
        adCache.putAd(adKey, itemNativeAd)
    }

    /**
     * Remove the cached native ad for the given key if it has been used (impression received).
     * This ensures only used ads are removed from the cache.
     */
    fun destroyNative(adKey: String): Boolean {
        return adCache.deleteAd(adKey)
    }
}