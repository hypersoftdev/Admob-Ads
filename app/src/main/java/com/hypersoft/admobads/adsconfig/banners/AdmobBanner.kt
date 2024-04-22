package com.hypersoft.admobads.adsconfig.banners

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
import com.hypersoft.admobads.adsconfig.banners.callbacks.BannerCallBack
import com.hypersoft.admobads.adsconfig.banners.enums.BannerType

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class AdmobBanner {

    private var adView: AdView? = null

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
        bannerCallBack: BannerCallBack? = null
    ) {
        if (isAppPurchased) {
            Log.e("AdsInformation", "onAdFailedToLoad -> Premium user")
            bannerCallBack?.onAdFailedToLoad("onAdFailedToLoad -> Premium user")
            return
        }

        if (adEnable == 0) {
            Log.e("AdsInformation", "onAdFailedToLoad -> Remote config is off")
            bannerCallBack?.onAdFailedToLoad("onAdFailedToLoad -> Remote config is off")
            return
        }

        if (isInternetConnected.not()) {
            Log.e("AdsInformation", "onAdFailedToLoad -> Internet is not connected")
            bannerCallBack?.onAdFailedToLoad("onAdFailedToLoad -> Internet is not connected")
            return
        }

        if (activity == null) {
            Log.e("AdsInformation", "onAdFailedToLoad -> Context is null")
            bannerCallBack?.onAdFailedToLoad("onAdFailedToLoad -> Context is null")
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.e("AdsInformation", "onAdFailedToLoad -> activity is finishing or destroyed")
            bannerCallBack?.onAdFailedToLoad("onAdFailedToLoad -> activity is finishing or destroyed")
            return
        }

        if (bannerId.trim().isEmpty()) {
            Log.e("AdsInformation", "onAdFailedToLoad -> Ad id is empty")
            bannerCallBack?.onAdFailedToLoad("onAdFailedToLoad -> Ad id is empty")
            return
        }

        try {
            adsPlaceHolder.visibility = View.VISIBLE
            adView = AdView(activity)
            adView?.adUnitId = bannerId
            try {
                adView?.setAdSize(getAdSize(activity, adsPlaceHolder))
            }catch (ex:Exception){
                adView?.setAdSize(AdSize.BANNER)
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

            adView?.loadAd(adRequest)
            adView?.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.i("AdsInformation", "admob banner onAdLoaded")
                    displayBannerAd(adsPlaceHolder)
                    bannerCallBack?.onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdsInformation", "admob banner onAdFailedToLoad: ${adError.message}")
                    adsPlaceHolder.visibility = View.GONE
                    bannerCallBack?.onAdFailedToLoad(adError.message)
                }

                override fun onAdImpression() {
                    Log.d("AdsInformation", "admob banner onAdImpression")
                    bannerCallBack?.onAdImpression()
                    super.onAdImpression()
                }

                override fun onAdClicked() {
                    Log.d("AdsInformation", "admob banner onAdClicked")
                    bannerCallBack?.onAdClicked()
                    super.onAdClicked()
                }

                override fun onAdClosed() {
                    Log.d("AdsInformation", "admob banner onAdClosed")
                    bannerCallBack?.onAdClosed()
                    super.onAdClosed()
                }

                override fun onAdOpened() {
                    Log.d("AdsInformation", "admob banner onAdOpened")
                    bannerCallBack?.onAdOpened()
                    super.onAdOpened()
                }
            }
        } catch (ex: Exception) {
            Log.e("AdsInformation", "${ex.message}")
            bannerCallBack?.onAdFailedToLoad("${ex.message}")
        }

    }

    private fun displayBannerAd(adsPlaceHolder: FrameLayout) {
        try {
            if (adView != null) {
                val viewGroup: ViewGroup? = adView?.parent as? ViewGroup?
                viewGroup?.removeView(adView)

                adsPlaceHolder.removeAllViews()
                adsPlaceHolder.addView(adView)
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
            adView?.pause()
        } catch (ex: Exception) {
            Log.e("AdsInformation", "bannerOnPause: ${ex.message}")
        }

    }

    fun bannerOnResume() {
        try {
            adView?.resume()
        } catch (ex: Exception) {
            Log.e("AdsInformation", "bannerOnPause: ${ex.message}")
        }
    }

    fun bannerOnDestroy() {
        try {
            adView?.destroy()
            adView = null
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