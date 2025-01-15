package com.hypersoft.admobads.newPackage.ads.natives.data.dataSources.remote

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.hypersoft.admobads.newPackage.utilities.utils.Constants.TAG_ADS
import com.hypersoft.admobads.newPackage.ads.natives.data.entities.ItemNativeAd
import java.lang.ref.WeakReference

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */


class DataSourceRemoteNative(context: Context?) {

    private val weakReference = WeakReference<Context>(context)

    fun fetchNativeAd(adKey: String, adId: String, callback: (ItemNativeAd?) -> Unit) {
        val context = weakReference.get()
        if (context == null) {
            Log.e(TAG_ADS, "fetchNativeAd: context is null")
            callback.invoke(null)
            return
        }

        val nativeBuilderOption = NativeAdOptions
            .Builder()
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .build()

        var nativeAd: NativeAd? = null

        val adLoader = AdLoader
            .Builder(context, adId)
            .forNativeAd { unifiedNativeAd ->
                nativeAd = unifiedNativeAd
                callback.invoke(ItemNativeAd(adId, unifiedNativeAd))
            }
            .withAdListener(object : AdListener() {
                override fun onAdImpression() {
                    super.onAdImpression()
                    Log.v(TAG_ADS, "$adKey -> loadNative: onAdImpression")
                    nativeAd?.let {
                        callback.invoke(ItemNativeAd(adId, it, true))
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.e(TAG_ADS, "$adKey -> loadNative: onAdFailedToLoad: ${loadAdError.message}")
                    callback.invoke(null)
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.i(TAG_ADS, "$adKey -> loadNative: onAdLoaded")
                }
            })
            .withNativeAdOptions(nativeBuilderOption)
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
        Log.d(TAG_ADS, "$adKey -> loadNative: Requesting admob server for ad...")
    }
}