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
     * Cache mechanism for native ad
     */
    fun getCachedNativeAd(adKey: String): ItemNativeAd? {
        val item = adCache.get(adKey)
        return if (item?.impressionReceived == false) {
            item
        } else {
            adCache.getFreeAd() ?: item
        }
    }

    fun putCachedNativeAd(adKey: String, itemNativeAd: ItemNativeAd) {
        adCache.put(adKey, itemNativeAd)
    }
}