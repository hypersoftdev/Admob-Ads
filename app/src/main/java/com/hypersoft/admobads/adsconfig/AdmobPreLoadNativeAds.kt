package com.hypersoft.admobads.adsconfig

import android.app.Activity
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.hypersoft.admobads.R
import com.hypersoft.admobads.adsconfig.callbacks.BannerCallBack
import com.hypersoft.admobads.adsconfig.constants.AdsConstants.adMobPreloadNativeAd
import com.hypersoft.admobads.adsconfig.constants.AdsConstants.isNativeLoading
import com.hypersoft.admobads.adsconfig.enums.NativeType
import com.hypersoft.admobads.helpers.observers.GlobalEvent.isPreLoadNativeLoaded
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdmobPreLoadNativeAds {

    private val AD_TAG = "AdsInformation"

    /**
     * only load native ad
     */
    fun loadNativeAds(
        activity: Activity?,
        admobNativeIds: String,
        adEnable: Int,
        isAppPurchased: Boolean,
        isInternetConnected: Boolean,
        bannerCallBack: BannerCallBack
    ) {
        val handlerException = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.e("adStatus", "${throwable.message}")
            bannerCallBack.onAdFailedToLoad("${throwable.message}")
        }
        activity?.let {mActivity ->
            try {
                if (isInternetConnected && adEnable != 0 && !isAppPurchased && !isNativeLoading && admobNativeIds.isNotEmpty()) {
                    isNativeLoading = true
                    if (adMobPreloadNativeAd == null) {
                        CoroutineScope(Dispatchers.IO + handlerException).launch {
                            val builder: AdLoader.Builder = AdLoader.Builder(mActivity, admobNativeIds)
                            val adLoader =
                                builder.forNativeAd { unifiedNativeAd: NativeAd? ->
                                    if (!mActivity.isDestroyed && !mActivity.isFinishing) {
                                        adMobPreloadNativeAd = unifiedNativeAd
                                    } else {
                                        unifiedNativeAd?.destroy()
                                        return@forNativeAd
                                    }
                                }
                                    .withAdListener(object : AdListener() {
                                        override fun onAdImpression() {
                                            super.onAdImpression()
                                            Log.d(AD_TAG, "admob native onAdImpression")
                                            bannerCallBack.onAdImpression()
                                            adMobPreloadNativeAd = null
                                        }

                                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                            Log.e(AD_TAG, "admob native onAdFailedToLoad: " + loadAdError.message)
                                            bannerCallBack.onAdFailedToLoad(loadAdError.message)
                                            adMobPreloadNativeAd = null
                                            isNativeLoading = false
                                            isPreLoadNativeLoaded.value = false
                                            super.onAdFailedToLoad(loadAdError)
                                        }

                                        override fun onAdLoaded() {
                                            super.onAdLoaded()
                                            Log.d(AD_TAG, "admob native onAdLoaded")
                                            isNativeLoading = false
                                            isPreLoadNativeLoaded.value = true
                                            bannerCallBack.onAdLoaded()

                                        }

                                    }).withNativeAdOptions(
                                        com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
                                            .setAdChoicesPlacement(
                                                NativeAdOptions.ADCHOICES_TOP_RIGHT
                                            ).build()
                                    )
                                    .build()
                            adLoader.loadAd(AdRequest.Builder().build())
                        }
                    } else {
                        isNativeLoading = false
                        Log.e(AD_TAG, "Native is already loaded")
                        bannerCallBack.onPreloaded()
                    }

                } else {
                    Log.e(AD_TAG, "adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
                    bannerCallBack.onAdFailedToLoad("adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
                }

            } catch (ex: Exception) {
                isNativeLoading = false
                Log.e(AD_TAG, "${ex.message}")
                bannerCallBack.onAdFailedToLoad("${ex.message}")

            }
        }
    }

    /**
     * show native ads from preload ads
     */
    fun showNativeAds(
        activity: Activity?,
        adsPlaceHolder: FrameLayout,
        nativeType: NativeType,
    ) {
        activity?.let {mActivity ->
            adMobPreloadNativeAd?.let {
                adsPlaceHolder.visibility = View.VISIBLE
                displayNativeAd(mActivity, adsPlaceHolder, nativeType)
            } ?: kotlin.run {
                adsPlaceHolder.visibility = View.GONE
            }
        }
    }

    private fun displayNativeAd(
        activity: Activity?,
        adMobNativeContainer: FrameLayout,
        nativeType: NativeType,
    ) {
        activity?.let { mActivity ->
            try {
                adMobPreloadNativeAd?.let { ad ->
                    val inflater = LayoutInflater.from(mActivity)

                    val adView: NativeAdView = when (nativeType) {
                        NativeType.BANNER -> inflater.inflate(R.layout.admob_native_banner, null) as NativeAdView
                        NativeType.SMALL -> inflater.inflate(R.layout.admob_native_small, null) as NativeAdView
                        NativeType.LARGE -> inflater.inflate(R.layout.admob_native_large, null) as NativeAdView
                        NativeType.LARGE_ADJUSTED -> if (isSupportFullScreen(mActivity)) {
                            inflater.inflate(R.layout.admob_native_large, null) as NativeAdView
                        } else {
                            inflater.inflate(R.layout.admob_native_small, null) as NativeAdView
                        }
                        NativeType.FIX -> inflater.inflate(R.layout.admob_native_fix, null) as NativeAdView
                    }

                    val viewGroup: ViewGroup? = adView.parent as ViewGroup?
                    viewGroup?.removeView(adView)

                    adMobNativeContainer.removeAllViews()
                    adMobNativeContainer.addView(adView)

                    if (nativeType == NativeType.LARGE || nativeType == NativeType.FIX) {
                        val mediaView: MediaView = adView.findViewById(R.id.media_view)
                        adView.mediaView = mediaView
                    }
                    if (nativeType == NativeType.LARGE_ADJUSTED) {
                        if (isSupportFullScreen(mActivity)) {
                            val mediaView: MediaView = adView.findViewById(R.id.media_view)
                            adView.mediaView = mediaView
                        }
                    }

                    // Set other ad assets.
                    adView.headlineView = adView.findViewById(R.id.ad_headline)
                    adView.bodyView = adView.findViewById(R.id.ad_body)
                    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
                    adView.iconView = adView.findViewById(R.id.ad_app_icon)

                    //Headline
                    adView.headlineView?.let { headline ->
                        (headline as TextView).text = ad.headline
                        headline.isSelected = true
                    }

                    //Body
                    adView.bodyView?.let { bodyView ->
                        if (ad.body == null) {
                            bodyView.visibility = View.INVISIBLE
                        } else {
                            bodyView.visibility = View.VISIBLE
                            (bodyView as TextView).text = ad.body
                        }

                    }

                    //Call to Action
                    adView.callToActionView?.let { ctaView ->
                        if (ad.callToAction == null) {
                            ctaView.visibility = View.INVISIBLE
                        } else {
                            ctaView.visibility = View.VISIBLE
                            (ctaView as Button).text = ad.callToAction
                        }

                    }

                    //Icon
                    adView.iconView?.let { iconView ->
                        if (ad.icon == null) {
                            iconView.visibility = View.GONE
                        } else {
                            (iconView as ImageView).setImageDrawable(ad.icon?.drawable)
                            iconView.visibility = View.VISIBLE
                        }

                    }

                    adView.advertiserView?.let { adverView ->

                        if (ad.advertiser == null) {
                            adverView.visibility = View.GONE
                        } else {
                            (adverView as TextView).text = ad.advertiser
                            adverView.visibility = View.GONE
                        }
                    }

                    adView.setNativeAd(ad)
                }
            } catch (ex: Exception) {
                Log.e(AD_TAG, "displayNativeAd: ${ex.message}")
            }
        }
    }

    private fun isSupportFullScreen(activity: Activity): Boolean {
        try {
            val outMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(outMetrics)
            if (outMetrics.heightPixels > 1280) {
                return true
            }
        } catch (ignored: Exception) {
        }
        return false
    }

}