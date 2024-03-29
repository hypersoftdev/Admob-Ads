package com.hypersoft.admobads.adsconfig

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.hypersoft.admobads.adsconfig.callbacks.BannerCallBack
import com.hypersoft.admobads.adsconfig.enums.BannerType

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class AdmobBanner {

    private var adaptiveAdView: AdView? = null

    /**
     * 0 = Ads Off
     * 1 = Collapsible Banner
     * 2 = Adaptive Banner
     */

    fun loadBannerAds(
        activity: Activity?,
        adsPlaceHolder: FrameLayout,
        bannerId: String,
        adEnable: Int,
        isAppPurchased: Boolean,
        isInternetConnected: Boolean,
        bannerType: BannerType = BannerType.ADAPTIVE_BANNER,
        bannerCallBack: BannerCallBack
    ) {
        activity?.let { mActivity ->
            try {
                if (isInternetConnected && adEnable != 0 && !isAppPurchased && bannerId.isNotEmpty()) {
                    adsPlaceHolder.visibility = View.VISIBLE
                    adaptiveAdView = AdView(mActivity)
                    adaptiveAdView?.adUnitId = bannerId
                    try {
                        adaptiveAdView?.setAdSize(getAdSize(mActivity, adsPlaceHolder))
                    }catch (ex:Exception){
                        adaptiveAdView?.setAdSize(AdSize.BANNER)
                    }

                    val adRequest: AdRequest =   when(adEnable){
                        1 -> when (bannerType) {
                            BannerType.ADAPTIVE_BANNER -> {
                                AdRequest
                                    .Builder()
                                    .build()
                            }
                            BannerType.COLLAPSIBLE_BOTTOM -> {
                                AdRequest
                                    .Builder()
                                    .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
                                        putString("collapsible", "bottom")
                                    })
                                    .build()
                            }
                            BannerType.COLLAPSIBLE_TOP -> {
                                AdRequest
                                    .Builder()
                                    .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
                                        putString("collapsible", "top")
                                    })
                                    .build()
                            }
                        }
                        else -> AdRequest
                            .Builder()
                            .build()
                    }

                    adaptiveAdView?.loadAd(adRequest)
                    adaptiveAdView?.adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("AdsInformation", "admob banner onAdLoaded")
                            displayBannerAd(adsPlaceHolder)
                            bannerCallBack.onAdLoaded()
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("AdsInformation", "admob banner onAdFailedToLoad: ${adError.message}")
                            adsPlaceHolder.visibility = View.GONE
                            bannerCallBack.onAdFailedToLoad(adError.message)
                        }

                        override fun onAdImpression() {
                            Log.d("AdsInformation", "admob banner onAdImpression")
                            bannerCallBack.onAdImpression()
                            super.onAdImpression()
                        }

                        override fun onAdClicked() {
                            Log.d("AdsInformation", "admob banner onAdClicked")
                            bannerCallBack.onAdClicked()
                            super.onAdClicked()
                        }

                        override fun onAdClosed() {
                            Log.d("AdsInformation", "admob banner onAdClosed")
                            bannerCallBack.onAdClosed()
                            super.onAdClosed()
                        }

                        override fun onAdOpened() {
                            Log.d("AdsInformation", "admob banner onAdOpened")
                            bannerCallBack.onAdOpened()
                            super.onAdOpened()
                        }
                    }
                } else {
                    adsPlaceHolder.removeAllViews()
                    adsPlaceHolder.visibility = View.GONE
                    Log.e("AdsInformation", "adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
                    bannerCallBack.onAdFailedToLoad("adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
                }
            } catch (ex: Exception) {
                Log.e("AdsInformation", "${ex.message}")
                bannerCallBack.onAdFailedToLoad("${ex.message}")
            }
        }

    }

    private fun displayBannerAd(adsPlaceHolder: FrameLayout) {
        try {
            if (adaptiveAdView != null) {
                val viewGroup: ViewGroup? = adaptiveAdView?.parent as? ViewGroup?
                viewGroup?.removeView(adaptiveAdView)

                adsPlaceHolder.removeAllViews()
                adsPlaceHolder.addView(adaptiveAdView)
            } else {
                adsPlaceHolder.removeAllViews()
                adsPlaceHolder.visibility = View.GONE
            }
        } catch (ex: Exception) {
            Log.e("AdsInformation", "inflateBannerAd: ${ex.message}")
        }

    }

    fun bannerOnPause() {
        try {
            adaptiveAdView?.pause()
        } catch (ex: Exception) {
            Log.e("AdsInformation", "bannerOnPause: ${ex.message}")
        }

    }

    fun bannerOnResume() {
        try {
            adaptiveAdView?.resume()
        } catch (ex: Exception) {
            Log.e("AdsInformation", "bannerOnPause: ${ex.message}")
        }
    }

    fun bannerOnDestroy() {
        try {
            adaptiveAdView?.destroy()
            adaptiveAdView = null
        } catch (ex: Exception) {
            Log.e("AdsInformation", "bannerOnPause: ${ex.message}")
        }
    }

    @Suppress("DEPRECATION")
    @Throws(Exception::class)
    private fun getAdSize(mActivity: Activity, adContainer: FrameLayout): AdSize {
        val display = mActivity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = adContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth)
    }

}