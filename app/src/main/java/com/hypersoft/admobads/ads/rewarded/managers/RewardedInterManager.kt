package com.hypersoft.admobads.ads.rewarded.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.hypersoft.admobads.ads.rewarded.callbacks.RewardedOnLoadCallBack
import com.hypersoft.admobads.ads.rewarded.callbacks.RewardedOnShowCallBack
import com.hypersoft.admobads.utilities.utils.Constants.TAG_ADS

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

abstract class RewardedInterManager {

    private var mRewardedInterstitialAd: RewardedInterstitialAd? = null
    private var isRewardedInterLoading = false

    protected fun loadRewardedInter(
        context: Context?,
        adType: String,
        rewardedInterId: String,
        adEnable: Boolean,
        isAppPurchased: Boolean,
        isInternetConnected: Boolean,
        listener: RewardedOnLoadCallBack?,
    ) {

        if (isRewardedInterLoaded()) {
            Log.i(TAG_ADS, "$adType -> loadRewardedInter: Already loaded")
            listener?.onResponse(true)
            return
        }

        if (isRewardedInterLoading) {
            Log.d(TAG_ADS, "$adType -> loadRewardedInter: Ad is already loading...")
            // No need to invoke callback, in some cases (e.g. activity recreation) it interrupts our response, as we are waiting for response in Splash
            // listener?.onResponse(false)  // Uncomment if u still need to listen this case
            return
        }

        if (isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> loadRewardedInter: Premium user")
            listener?.onResponse(false)
            return
        }

        if (adEnable.not()) {
            Log.e(TAG_ADS, "$adType -> loadRewardedInter: Remote config is off")
            listener?.onResponse(false)
            return
        }

        if (isInternetConnected.not()) {
            Log.e(TAG_ADS, "$adType -> loadRewardedInter: Internet is not connected")
            listener?.onResponse(false)
            return
        }

        if (context == null) {
            Log.e(TAG_ADS, "$adType -> loadRewardedInter: Context is null")
            listener?.onResponse(false)
            return
        }

        if (rewardedInterId.trim().isEmpty()) {
            Log.e(TAG_ADS, "$adType -> loadRewardedInter: Ad id is empty")
            listener?.onResponse(false)
            return
        }

        Log.d(TAG_ADS, "$adType -> loadRewardedInter: Requesting admob server for ad...")
        isRewardedInterLoading = true

        RewardedInterstitialAd.load(
            context,
            rewardedInterId.trim(),
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG_ADS, "$adType -> loadRewardedInter: onAdFailedToLoad: ${adError.message}")
                    isRewardedInterLoading = false
                    mRewardedInterstitialAd = null
                    listener?.onResponse(false)
                }

                override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                    Log.i(TAG_ADS, "$adType -> loadRewardedInter: onAdLoaded")
                    isRewardedInterLoading = false
                    mRewardedInterstitialAd = rewardedInterstitialAd
                    listener?.onResponse(true)
                }
            })
    }

    protected fun showRewardedInter(
        activity: Activity?,
        adType: String,
        isAppPurchased: Boolean,
        listener: RewardedOnShowCallBack?
    ) {

        if (isRewardedInterLoaded().not()) {
            Log.e(TAG_ADS, "$adType -> showRewardedInter: RewardedInter is not loaded yet")
            listener?.onAdFailedToShow()
            return
        }

        if (isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> showRewardedInter: Premium user")
            if (isRewardedInterLoaded()) {
                Log.d(TAG_ADS, "$adType -> Destroying loaded RewardedInter ad due to Premium user")
                mRewardedInterstitialAd = null
            }
            listener?.onAdFailedToShow()
            return
        }

        if (activity == null) {
            Log.e(TAG_ADS, "$adType -> showRewardedInter: activity reference is null")
            listener?.onAdFailedToShow()
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.e(TAG_ADS, "$adType -> showRewardedInter: activity is finishing or destroyed")
            listener?.onAdFailedToShow()
            return
        }

        mRewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG_ADS, "admob RewardedInter onAdDismissedFullScreenContent")
                listener?.onAdDismissedFullScreenContent()
                mRewardedInterstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG_ADS, "admob RewardedInter onAdFailedToShowFullScreenContent: ${adError.message}")
                listener?.onAdFailedToShow()
                mRewardedInterstitialAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG_ADS, "admob RewardedInter onAdShowedFullScreenContent")
                listener?.onAdShowedFullScreenContent()
                mRewardedInterstitialAd = null
            }

            override fun onAdImpression() {
                Log.v(TAG_ADS, "admob RewardedInter onAdImpression")
                listener?.onAdImpression()
            }
        }

        Log.d(TAG_ADS, "$adType -> RewardedInter: showing ad")
        mRewardedInterstitialAd?.show(activity) {
            Log.d(TAG_ADS, "admob RewardedInter onUserEarnedReward")
            listener?.onUserEarnedReward()
        }
    }

    fun isRewardedInterLoaded(): Boolean {
        return mRewardedInterstitialAd != null
    }
}