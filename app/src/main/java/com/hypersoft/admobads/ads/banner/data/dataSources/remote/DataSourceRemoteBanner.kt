package com.hypersoft.admobads.ads.banner.data.dataSources.remote

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.hypersoft.admobads.ads.banner.data.entities.ItemBannerAd
import com.hypersoft.admobads.ads.banner.presentation.enums.BannerAdType
import com.hypersoft.admobads.utilities.utils.Constants.TAG_ADS

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class DataSourceRemoteBanner(private val context: Context) {

    fun fetchBannerAd(adKey: String, adId: String, bannerAdType: BannerAdType, adView: AdView, callback: (ItemBannerAd?) -> Unit) {
        val adRequest = when (bannerAdType) {
            BannerAdType.ADAPTIVE -> {
                AdRequest.Builder().build()
            }

            BannerAdType.COLLAPSIBLE_TOP -> {
                AdRequest
                    .Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
                        putString("collapsible", "top")
                    })
                    .build()
            }

            BannerAdType.COLLAPSIBLE_BOTTOM -> {
                AdRequest
                    .Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
                        putString("collapsible", "bottom")
                    })
                    .build()
            }
        }

        val adSize = getAdSize() ?: AdSize.BANNER
        adView.apply {
            adUnitId = adId
            setAdSize(adSize)
        }
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.i(TAG_ADS, "$adKey -> loadBanner: onAdLoaded")
                callback.invoke(ItemBannerAd(adId = adId, adView = adView))
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)
                Log.e(TAG_ADS, "$adKey -> loadBanner: onAdFailedToLoad: ${adError.message}")
                callback.invoke(null)
            }

            override fun onAdImpression() {
                super.onAdImpression()
                Log.v(TAG_ADS, "$adKey -> loadBanner: onAdImpression")
                callback.invoke(ItemBannerAd(adId = adId, adView = adView, impressionReceived = true))
            }

            override fun onAdOpened() {
                super.onAdOpened()
                Log.d(TAG_ADS, "$adKey -> loadBanner: onAdOpened")
            }

            override fun onAdClosed() {
                super.onAdClosed()
                Log.d(TAG_ADS, "$adKey -> loadBanner: onAdClosed")
            }
        }
        adView.loadAd(adRequest)
        Log.d(TAG_ADS, "$adKey -> loadBanner: Requesting admob server for ad...")
    }


    @Suppress("DEPRECATION")
    private fun getAdSize(): AdSize? {
        val density = context.resources.displayMetrics.density

        val adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowManager = context.getSystemService<WindowManager>()
            val bounds = windowManager?.currentWindowMetrics?.bounds
            bounds?.width()?.toFloat()
        } else {
            val display: Display? = context.getSystemService<DisplayManager>()?.getDisplay(Display.DEFAULT_DISPLAY)
            val outMetrics = DisplayMetrics()
            display?.getMetrics(outMetrics)
            outMetrics.widthPixels.toFloat()
        }
        if (adWidthPixels == null) {
            return null
        }
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }
}