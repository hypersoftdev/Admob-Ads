package com.hypersoft.admobads.adsconfig

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.hypersoft.admobads.adsconfig.callbacks.InterstitialOnLoadCallBack
import com.hypersoft.admobads.adsconfig.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.adsconfig.constants.AdsConstants.isInterLoading
import com.hypersoft.admobads.adsconfig.constants.AdsConstants.mInterstitialAd

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
        listener: InterstitialOnLoadCallBack
    ) {
        activity?.let { mActivity ->
            if (isInternetConnected && adEnable != 0 && !isAppPurchased && !isInterLoading && interId.isNotEmpty()) {
                if (mInterstitialAd == null) {
                    isInterLoading = true
                    InterstitialAd.load(
                        mActivity,
                        interId,
                        AdRequest.Builder().build(),
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                Log.e("AdsInformation", "admob Interstitial onAdFailedToLoad: ${adError.message}")
                                isInterLoading = false
                                mInterstitialAd = null
                                listener.onAdFailedToLoad(adError.toString())
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                Log.d("AdsInformation", "admob Interstitial onAdLoaded")
                                isInterLoading = false
                                mInterstitialAd = interstitialAd
                                listener.onAdLoaded()
                            }
                        })
                } else {
                    Log.d("AdsInformation", "admob Interstitial onPreloaded")
                    listener.onPreloaded()
                }

            } else {
                Log.e("AdsInformation", "adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
                listener.onAdFailedToLoad("adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
            }
        }
    }

    fun showInterstitialAd(activity: Activity?, listener: InterstitialOnShowCallBack) {
        activity?.let { mActivity ->
            if (mInterstitialAd != null) {
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdsInformation", "admob Interstitial onAdDismissedFullScreenContent")
                        listener.onAdDismissedFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdsInformation", "admob Interstitial onAdFailedToShowFullScreenContent: ${adError.message}")
                        listener.onAdFailedToShowFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d("AdsInformation", "admob Interstitial onAdShowedFullScreenContent")
                        listener.onAdShowedFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdImpression() {
                        Log.d("AdsInformation", "admob Interstitial onAdImpression")
                        listener.onAdImpression()
                    }
                }
                mInterstitialAd?.show(mActivity)
            }
        }
    }

    fun showAndLoadInterstitialAd(activity: Activity?, interId: String, listener: InterstitialOnShowCallBack) {
        activity?.let { mActivity ->
            if (mInterstitialAd != null && interId.isNotEmpty()) {
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdsInformation", "admob Interstitial onAdDismissedFullScreenContent")
                        listener.onAdDismissedFullScreenContent()
                        loadAgainInterstitialAd(mActivity, interId)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdsInformation", "admob Interstitial onAdFailedToShowFullScreenContent: ${adError.message}")
                        listener.onAdFailedToShowFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d("AdsInformation", "admob Interstitial onAdShowedFullScreenContent")
                        listener.onAdShowedFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdImpression() {
                        Log.d("AdsInformation", "admob Interstitial onAdImpression")
                        listener.onAdImpression()
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
                            Log.d("AdsInformation", "admob Interstitial onAdLoaded")
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