package com.hypersoft.admobads.ads.natives.domain.repository

import com.hypersoft.admobads.ads.natives.data.entities.ItemNativeAd

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

interface RepositoryNative {
    fun fetchNativeAd(adKey: String, adId: String, callback: (ItemNativeAd?) -> Unit)
}