package com.hypersoft.admobads.ads.banner.data.dataSources.remote

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.hypersoft.admobads.ads.banner.data.entities.ItemBannerAd
import com.hypersoft.admobads.utilities.utils.Constants.TAG_ADS

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class DataSourceRemoteBanner (private val context: Context) {

    fun fetchBannerAd(adKey: String, adId: String, callback: (ItemBannerAd?) -> Unit) {
        /*val nativeBuilderOption = NativeAdOptions.Builder()
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .build()

        var nativeAd: NativeAd? = null

        val adLoader = AdLoader.Builder(context, adId)
            .forNativeAd { unifiedNativeAd ->
                nativeAd = unifiedNativeAd
                callback.invoke(ItemBannerAd(adId, unifiedNativeAd))
            }
            .withAdListener(object : AdListener() {
                override fun onAdImpression() {
                    super.onAdImpression()
                    Log.v(TAG_ADS, "$adKey -> loadNative: onAdImpression")
                    nativeAd?.let {
                        callback.invoke(ItemBannerAd(adId, it, true))
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
        Log.d(TAG_ADS, "$adKey -> loadNative: Requesting admob server for ad...")*/
    }
}