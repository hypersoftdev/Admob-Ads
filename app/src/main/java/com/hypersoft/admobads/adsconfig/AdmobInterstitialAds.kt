package com.hypersoft.admobads.adsconfig

import android.annotation.SuppressLint
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
import com.hypersoft.admobads.adsconfig.constants.AdsConstants.isInterstitialLoading
import com.hypersoft.admobads.adsconfig.constants.AdsConstants.mInterstitialAd

class AdmobInterstitialAds {

    private val AD_TAG = "AdsInformation"

    @SuppressLint("VisibleForTests")
    fun loadInterstitialAd(
        activity: Activity?,
        admobInterstitialIds: String,
        adEnable: Int,
        isAppPurchased: Boolean,
        isInternetConnected: Boolean,
        mListener: InterstitialOnLoadCallBack
    ) {
        activity?.let { mActivity ->
            if (isInternetConnected && adEnable != 0 && !isAppPurchased && !isInterstitialLoading && admobInterstitialIds.isNotEmpty()) {
                if (mInterstitialAd == null) {
                    isInterstitialLoading = true
                    InterstitialAd.load(
                        mActivity,
                        admobInterstitialIds,
                        AdRequest.Builder().build(),
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                Log.e(AD_TAG, "admob Interstitial onAdFailedToLoad")
                                isInterstitialLoading = false
                                mInterstitialAd = null
                                mListener.onAdFailedToLoad(adError.toString())
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                Log.d(AD_TAG, "admob Interstitial onAdLoaded")
                                isInterstitialLoading = false
                                mInterstitialAd = interstitialAd
                                mListener.onAdLoaded()

                            }
                        })
                } else {
                    Log.d(AD_TAG, "admob Interstitial onPreloaded")
                    mListener.onPreloaded()
                }

            } else {
                Log.e(AD_TAG, "adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
                mListener.onAdFailedToLoad("adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
            }
        }
    }

    fun showInterstitialAd(activity: Activity?, mListener: InterstitialOnShowCallBack) {
        activity?.let { mActivity ->
            if (mInterstitialAd != null) {
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(AD_TAG, "admob Interstitial onAdDismissedFullScreenContent")
                        mListener.onAdDismissedFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e(AD_TAG, "admob Interstitial onAdFailedToShowFullScreenContent")
                        mListener.onAdFailedToShowFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(AD_TAG, "admob Interstitial onAdShowedFullScreenContent")
                        mListener.onAdShowedFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdImpression() {
                        Log.d(AD_TAG, "admob Interstitial onAdImpression")
                        mListener.onAdImpression()
                    }
                }
                mInterstitialAd?.show(mActivity)
            }
        }
    }

    fun showAndLoadInterstitialAd(activity: Activity?, admobInterstitialIds: String, mListener: InterstitialOnShowCallBack) {
        activity?.let { mActivity ->
            if (mInterstitialAd != null && admobInterstitialIds.isNotEmpty()) {
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(AD_TAG, "admob Interstitial onAdDismissedFullScreenContent")
                        mListener.onAdDismissedFullScreenContent()
                        loadAgainInterstitialAd(mActivity, admobInterstitialIds)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e(AD_TAG, "admob Interstitial onAdFailedToShowFullScreenContent")
                        mListener.onAdFailedToShowFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(AD_TAG, "admob Interstitial onAdShowedFullScreenContent")
                        mListener.onAdShowedFullScreenContent()
                        mInterstitialAd = null
                    }

                    override fun onAdImpression() {
                        Log.d(AD_TAG, "admob Interstitial onAdImpression")
                        mListener.onAdImpression()
                    }
                }
                mInterstitialAd?.show(mActivity)
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private fun loadAgainInterstitialAd(
        activity: Activity?,
        admobInterstitialIds: String
    ) {
        activity?.let { mActivity ->
            if (mInterstitialAd == null && !isInterstitialLoading) {
                isInterstitialLoading = true
                InterstitialAd.load(
                    mActivity,
                    admobInterstitialIds,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e(AD_TAG, "admob Interstitial onAdFailedToLoad: $adError")
                            isInterstitialLoading = false
                            mInterstitialAd = null
                        }

                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            Log.d(AD_TAG, "admob Interstitial onAdLoaded")
                            isInterstitialLoading = false
                            mInterstitialAd = interstitialAd

                        }
                    })
            }
        }
    }

    fun isInterstitialLoaded(): Boolean {
        return mInterstitialAd != null
    }

    fun dismissInterstitialLoaded() {
        mInterstitialAd = null
    }

}