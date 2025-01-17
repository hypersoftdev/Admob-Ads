package com.hypersoft.admobads.ads.appOpen.screen.manager

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.hypersoft.admobads.ads.appOpen.screen.callbacks.AppOpenOnLoadCallBack
import com.hypersoft.admobads.ads.appOpen.screen.callbacks.AppOpenOnShowCallBack
import com.hypersoft.admobads.utilities.utils.Constants.TAG_ADS

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

abstract class AppOpenManager {

    private var mAppOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false

    protected fun loadAppOpen(
        context: Context?,
        adType: String,
        appOpenId: String,
        adEnable: Boolean,
        isAppPurchased: Boolean,
        isInternetConnected: Boolean,
        listener: AppOpenOnLoadCallBack?,
    ) {

        if (isAppOpenLoaded()) {
            Log.i(TAG_ADS, "$adType -> loadAppOpen: Already loaded")
            listener?.onResponse(true)
            return
        }

        if (isAppOpenLoading) {
            Log.d(TAG_ADS, "$adType -> loadAppOpen: Ad is already loading...")
            // No need to invoke callback, in some cases (e.g. activity recreation) it interrupts our response, as we are waiting for response in Splash
            // listener?.onResponse(false)  // Uncomment if u still need to listen this case
            return
        }

        if (isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> loadAppOpen: Premium user")
            listener?.onResponse(false)
            return
        }

        if (adEnable.not()) {
            Log.e(TAG_ADS, "$adType -> loadAppOpen: Remote config is off")
            listener?.onResponse(false)
            return
        }

        if (isInternetConnected.not()) {
            Log.e(TAG_ADS, "$adType -> loadAppOpen: Internet is not connected")
            listener?.onResponse(false)
            return
        }

        if (context == null) {
            Log.e(TAG_ADS, "$adType -> loadAppOpen: Context is null")
            listener?.onResponse(false)
            return
        }

        if (appOpenId.trim().isEmpty()) {
            Log.e(TAG_ADS, "$adType -> loadAppOpen: Ad id is empty")
            listener?.onResponse(false)
            return
        }

        Log.d(TAG_ADS, "$adType -> loadAppOpen: Requesting admob server for ad...")
        isAppOpenLoading = true

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            appOpenId.trim(),
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG_ADS, "$adType -> loadAppOpen: onAdFailedToLoad: ${adError.message}")
                    isAppOpenLoading = false
                    mAppOpenAd = null
                    listener?.onResponse(false, "Error Code: ${adError.code} - Message: ${adError.message}")
                }

                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    Log.i(TAG_ADS, "$adType -> loadAppOpen: onAdLoaded")
                    isAppOpenLoading = false
                    mAppOpenAd = appOpenAd
                    listener?.onResponse(true)
                }
            })
    }

    protected fun showAppOpen(
        activity: Activity?,
        adType: String,
        isAppPurchased: Boolean,
        listener: AppOpenOnShowCallBack?
    ) {
        if (isAppOpenLoaded().not()) {
            Log.e(TAG_ADS, "$adType -> showAppOpen: AppOpen is not loaded yet")
            listener?.onAdFailedToShow()
            return
        }

        if (isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> showAppOpen: Premium user")
            if (isAppOpenLoaded()) {
                Log.d(TAG_ADS, "$adType -> Destroying loaded appOpen ad due to Premium user")
                mAppOpenAd = null
            }
            listener?.onAdFailedToShow()
            return
        }

        if (activity == null) {
            Log.e(TAG_ADS, "$adType -> showAppOpen: activity reference is null")
            listener?.onAdFailedToShow()
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.e(TAG_ADS, "$adType -> showAppOpen: activity is finishing or destroyed")
            listener?.onAdFailedToShow()
            return
        }

        Log.d(TAG_ADS, "$adType -> showAppOpen: showing ad")
        mAppOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG_ADS, "$adType -> showAppOpen: onAdDismissedFullScreenContent: called")
                listener?.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG_ADS, "$adType -> showAppOpen: onAdFailedToShowFullScreenContent: ${adError.code} -- ${adError.message}")
                listener?.onAdFailedToShow()
                if (adError.code != 3) {
                    mAppOpenAd = null
                }
            }

            override fun onAdClicked() {
                Log.d(TAG_ADS, "$adType -> showAppOpen: onAdClicked: called")
                listener?.onAdClicked()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG_ADS, "$adType -> showAppOpen: onAdShowedFullScreenContent: called")
                listener?.onAdShowedFullScreenContent()
            }

            override fun onAdImpression() {
                Log.v(TAG_ADS, "$adType -> showAppOpen: onAdImpression: called")
                mAppOpenAd = null
                listener?.onAdImpression()
                Handler(Looper.getMainLooper()).postDelayed({ listener?.onAdImpressionDelayed() }, 300)
            }
        }
        mAppOpenAd?.show(activity)
    }

    fun isAppOpenLoaded(): Boolean {
        return mAppOpenAd != null
    }

    fun getAd(): AppOpenAd? {
        return mAppOpenAd
    }
}