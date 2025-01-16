package com.hypersoft.admobads.oldPackage.adsconfig.interstitial

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.hypersoft.admobads.oldPackage.adsconfig.interstitial.callbacks.InterstitialOnLoadCallBack
import com.hypersoft.admobads.oldPackage.adsconfig.interstitial.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.oldPackage.adsconfig.utils.AdsConstants.isInterLoading
import com.hypersoft.admobads.oldPackage.adsconfig.utils.AdsConstants.mInterstitialAd

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class AdmobInterstitial {

    /**
     * 0 = Ads Off
     * 1 = Ads On
     */

    fun loadInterstitialAd(
        activity: Activity?,
        interId: String,
        adEnable: Int,
        isAppPurchased: Boolean,
        isInternetConnected: Boolean,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        if (isAppPurchased) {
            Log.e("AdsInformation", "onAdFailedToLoad -> Premium user")
            listener?.onAdFailedToLoad("onAdFailedToLoad -> Premium user")
            return
        }

        if (adEnable == 0) {
            Log.e("AdsInformation", "onAdFailedToLoad -> Remote config is off")
            listener?.onAdFailedToLoad("onAdFailedToLoad -> Remote config is off")
            return
        }

        if (isInternetConnected.not()) {
            Log.e("AdsInformation", "onAdFailedToLoad -> Internet is not connected")
            listener?.onAdFailedToLoad("onAdFailedToLoad -> Internet is not connected")
            return
        }

        if (activity == null) {
            Log.e("AdsInformation", "onAdFailedToLoad -> Context is null")
            listener?.onAdFailedToLoad("onAdFailedToLoad -> Context is null")
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.e("AdsInformation", "onAdFailedToLoad -> activity is finishing or destroyed")
            listener?.onAdFailedToLoad("onAdFailedToLoad -> activity is finishing or destroyed")
            return
        }

        if (interId.trim().isEmpty()) {
            Log.e("AdsInformation", "onAdFailedToLoad -> Ad id is empty")
            listener?.onAdFailedToLoad("onAdFailedToLoad -> Ad id is empty")
            return
        }

        if (isInterLoading) {
            Log.e("AdsInformation", "onAdFailedToLoad -> interstitial is loading...")
            listener?.onAdFailedToLoad("onAdFailedToLoad -> interstitial is loading...")
            return
        }

        try {
            if (mInterstitialAd == null) {
                isInterLoading = true
                InterstitialAd.load(
                    activity,
                    interId,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("AdsInformation", "admob Interstitial onAdFailedToLoad: ${adError.message}")
                            isInterLoading = false
                            mInterstitialAd = null
                            listener?.onAdFailedToLoad(adError.toString())
                        }
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            Log.i("AdsInformation", "admob Interstitial onAdLoaded")
                            isInterLoading = false
                            mInterstitialAd = interstitialAd
                            listener?.onAdLoaded()
                        }
                    })
            } else {
                Log.i("AdsInformation", "admob Interstitial onPreloaded")
                listener?.onPreloaded()
            }
        }catch (ex:Exception){
            Log.e("AdsInformation", "${ex.message}")
        }
    }

    fun showInterstitialAd(activity: Activity?, listener: InterstitialOnShowCallBack? = null) {
        activity?.let { mActivity ->
            if (mInterstitialAd != null) {
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdsInformation", "admob Interstitial onAdDismissedFullScreenContent")
                        listener?.onAdDismissedFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdsInformation", "admob Interstitial onAdFailedToShowFullScreenContent: ${adError.message}")
                        listener?.onAdFailedToShowFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d("AdsInformation", "admob Interstitial onAdShowedFullScreenContent")
                        listener?.onAdShowedFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdImpression() {
                        Log.d("AdsInformation", "admob Interstitial onAdImpression")
                        listener?.onAdImpression()
                    }
                }
                mInterstitialAd?.show(mActivity)
            }
        }
    }

    fun showAndLoadInterstitialAd(activity: Activity?, interId: String, listener: InterstitialOnShowCallBack? = null) {
        activity?.let { mActivity ->
            if (mInterstitialAd != null && interId.isNotEmpty()) {
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdsInformation", "admob Interstitial onAdDismissedFullScreenContent")
                        listener?.onAdDismissedFullScreenContent()
                        loadAgainInterstitialAd(mActivity, interId)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdsInformation", "admob Interstitial onAdFailedToShowFullScreenContent: ${adError.message}")
                        listener?.onAdFailedToShowFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d("AdsInformation", "admob Interstitial onAdShowedFullScreenContent")
                        listener?.onAdShowedFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdImpression() {
                        Log.d("AdsInformation", "admob Interstitial onAdImpression")
                        listener?.onAdImpression()
                    }
                }
                mInterstitialAd?.show(mActivity)
            }
        }
    }

    private fun loadAgainInterstitialAd(
        activity: Activity?,
        interId: String
    ) {
        activity?.let { mActivity ->
            if (mInterstitialAd == null && !isInterLoading) {
                isInterLoading = true
                InterstitialAd.load(
                    mActivity,
                    interId,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("AdsInformation", "admob Interstitial onAdFailedToLoad: $adError")
                            isInterLoading = false
                            mInterstitialAd = null
                        }

                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            Log.i("AdsInformation", "admob Interstitial onAdLoaded")
                            isInterLoading = false
                            mInterstitialAd = interstitialAd

                        }
                    })
            }
        }
    }

    fun isInterstitialLoaded(): Boolean {
        return mInterstitialAd != null
    }

    fun dismissInterstitial() {
        mInterstitialAd = null
    }

}