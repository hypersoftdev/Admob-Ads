package com.hypersoft.admobads.ads.interstitial.manager

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.hypersoft.admobads.ads.interstitial.callbacks.InterstitialOnLoadCallBack
import com.hypersoft.admobads.ads.interstitial.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.utilities.utils.Constants.TAG_ADS

/**
 * Created by: Sohaib Ahmed
 * Date: 1/16/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

abstract class InterstitialManager {

    private var mInterstitialAd: InterstitialAd? = null
    private var isInterLoading = false

    protected fun loadInterstitial(
        context: Context?,
        adType: String,
        interId: String,
        adEnable: Boolean,
        isAppPurchased: Boolean,
        isInternetConnected: Boolean,
        listener: InterstitialOnLoadCallBack?,
    ) {

        if (isInterstitialLoaded()) {
            Log.i(TAG_ADS, "$adType -> loadInterstitial: Already loaded")
            listener?.onResponse(true)
            return
        }

        if (isInterLoading) {
            Log.d(TAG_ADS, "$adType -> loadInterstitial: Ad is already loading...")
            // No need to invoke callback, in some cases (e.g. activity recreation) it interrupts our response, as we are waiting for response in Splash
            // listener?.onResponse(false)  // Uncomment if u still need to listen this case
            return
        }

        if (isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> loadInterstitial: Premium user")
            listener?.onResponse(false)
            return
        }

        if (adEnable.not()) {
            Log.e(TAG_ADS, "$adType -> loadInterstitial: Remote config is off")
            listener?.onResponse(false)
            return
        }

        if (isInternetConnected.not()) {
            Log.e(TAG_ADS, "$adType -> loadInterstitial: Internet is not connected")
            listener?.onResponse(false)
            return
        }

        if (context == null) {
            Log.e(TAG_ADS, "$adType -> loadInterstitial: Context is null")
            listener?.onResponse(false)
            return
        }

        if (interId.trim().isEmpty()) {
            Log.e(TAG_ADS, "$adType -> loadInterstitial: Ad id is empty")
            listener?.onResponse(false)
            return
        }

        Log.d(TAG_ADS, "$adType -> loadInterstitial: Requesting admob server for ad...")
        isInterLoading = true

        InterstitialAd.load(
            context,
            interId.trim(),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG_ADS, "$adType -> loadInterstitial: onAdFailedToLoad: ${adError.message}")
                    isInterLoading = false
                    mInterstitialAd = null
                    listener?.onResponse(false)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.i(TAG_ADS, "$adType -> loadInterstitial: onAdLoaded")
                    isInterLoading = false
                    mInterstitialAd = interstitialAd
                    listener?.onResponse(true)
                }
            })
    }

    protected fun showInterstitial(
        activity: Activity?,
        adType: String,
        isAppPurchased: Boolean,
        listener: InterstitialOnShowCallBack?
    ) {

        if (isInterstitialLoaded().not()) {
            Log.e(TAG_ADS, "$adType -> showInterstitial: Interstitial is not loaded yet")
            listener?.onAdFailedToShow()
            return
        }

        if (isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> showInterstitial: Premium user")
            if (isInterstitialLoaded()) {
                Log.d(TAG_ADS, "$adType -> Destroying loaded inter ad due to Premium user")
                mInterstitialAd = null
            }
            listener?.onAdFailedToShow()
            return
        }

        if (activity == null) {
            Log.e(TAG_ADS, "$adType -> showInterstitial: activity reference is null")
            listener?.onAdFailedToShow()
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.e(TAG_ADS, "$adType -> showInterstitial: activity is finishing or destroyed")
            listener?.onAdFailedToShow()
            return
        }

        Log.d(TAG_ADS, "$adType -> showInterstitial: showing ad")

        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG_ADS, "$adType -> showInterstitial: onAdDismissedFullScreenContent: called")
                listener?.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG_ADS, "$adType -> showInterstitial: onAdFailedToShowFullScreenContent: ${adError.code} -- ${adError.message}")
                mInterstitialAd = null
                listener?.onAdFailedToShow()
            }

            override fun onAdClicked() {
                listener?.onAdClicked()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG_ADS, "$adType -> showInterstitial: onAdShowedFullScreenContent: called")
                listener?.onAdShowedFullScreenContent()
            }

            override fun onAdImpression() {
                Log.v(TAG_ADS, "$adType -> showInterstitial: onAdImpression: called")
                mInterstitialAd = null
                listener?.onAdImpression()
                Handler(Looper.getMainLooper()).postDelayed({ listener?.onAdImpressionDelayed() }, 300)
            }
        }
        mInterstitialAd?.show(activity)
    }

    fun isInterstitialLoaded(): Boolean {
        return mInterstitialAd != null
    }
}