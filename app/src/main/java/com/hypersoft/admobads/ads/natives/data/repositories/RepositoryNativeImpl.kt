package com.hypersoft.admobads.ads.natives.data.repositories

import android.util.Log
import com.hypersoft.admobads.ads.natives.data.dataSources.local.DataSourceLocalNative
import com.hypersoft.admobads.ads.natives.data.dataSources.remote.DataSourceRemoteNative
import com.hypersoft.admobads.ads.natives.data.entities.ItemNativeAd
import com.hypersoft.admobads.ads.natives.domain.repository.RepositoryNative
import com.hypersoft.admobads.utilities.utils.Constants

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class RepositoryNativeImpl(private val dataSourceLocalNative: DataSourceLocalNative, private val dataSourceRemoteNative: DataSourceRemoteNative) : RepositoryNative {

    override fun fetchNativeAd(adKey: String, adId: String, callback: (ItemNativeAd?) -> Unit) {

        // Check cache resource
        dataSourceLocalNative.getCachedNativeAd(adKey)?.let { cachedAd ->
            Log.d(Constants.TAG_ADS, "$adKey -> fetchNativeAd: Reshowing Ad")
            callback.invoke(cachedAd)
            return
        }

        dataSourceRemoteNative.fetchNativeAd(adKey = adKey, adId = adId) { remoteAd ->
            remoteAd?.let {
                dataSourceLocalNative.putCachedNativeAd(adKey, it)
            }
            callback.invoke(remoteAd)
        }
    }

    fun destroyNative(adKey: String): Boolean {
        return dataSourceLocalNative.destroyNative(adKey)
    }
}