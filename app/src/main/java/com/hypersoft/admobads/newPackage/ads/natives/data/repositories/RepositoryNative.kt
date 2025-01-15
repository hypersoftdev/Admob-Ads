package com.hypersoft.admobads.newPackage.ads.natives.data.repositories

import android.util.Log
import com.hypersoft.admobads.newPackage.utilities.utils.Constants
import com.hypersoft.admobads.newPackage.ads.natives.data.dataSources.local.DataSourceLocalNative
import com.hypersoft.admobads.newPackage.ads.natives.data.dataSources.remote.DataSourceRemoteNative
import com.hypersoft.admobads.newPackage.ads.natives.domain.entities.ItemNativeAd

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class RepositoryNative(
    private val dataSourceLocalNative: DataSourceLocalNative,
    private val dataSourceRemoteNative: DataSourceRemoteNative
) {

    fun fetchNativeAd(adKey: String, adId: String, callback: (ItemNativeAd?) -> Unit) {
        val cachedAd = dataSourceLocalNative.getCachedNativeAd(adKey)
        if (cachedAd != null) {
            Log.d(Constants.TAG_ADS, "$adKey -> fetchNativeAd: Reshowing Ad")
            callback.invoke(cachedAd)
            return
        }

        dataSourceRemoteNative.fetchNativeAd(adKey = adKey, adId = adId) {
            if (it == null) {
                callback.invoke(null)
            } else {
                dataSourceLocalNative.putCachedNativeAd(adKey, it)
                callback.invoke(it)
            }
        }
    }
}