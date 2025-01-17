package com.hypersoft.admobads.ads.rewarded.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
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

abstract class RewardedManager {

    private var mRewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    protected fun loadRewarded(
        context: Context?,
        adType: String,
        rewardedId: String,
        adEnable: Boolean,
        isAppPurchased: Boolean,
        isInternetConnected: Boolean,
        listener: RewardedOnLoadCallBack?,
    ) {

        if (isRewardedLoaded()) {
            Log.i(TAG_ADS, "$adType -> loadRewarded: Already loaded")
            listener?.onResponse(true)
            return
        }

        if (isRewardedLoading) {
            Log.d(TAG_ADS, "$adType -> loadRewarded: Ad is already loading...")
            // No need to invoke callback, in some cases (e.g. activity recreation) it interrupts our response, as we are waiting for response in Splash
            // listener?.onResponse(false)  // Uncomment if u still need to listen this case
            return
        }

        if (isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> loadRewarded: Premium user")
            listener?.onResponse(false)
            return
        }

        if (adEnable.not()) {
            Log.e(TAG_ADS, "$adType -> loadRewarded: Remote config is off")
            listener?.onResponse(false)
            return
        }

        if (isInternetConnected.not()) {
            Log.e(TAG_ADS, "$adType -> loadRewarded: Internet is not connected")
            listener?.onResponse(false)
            return
        }

        if (context == null) {
            Log.e(TAG_ADS, "$adType -> loadRewarded: Context is null")
            listener?.onResponse(false)
            return
        }

        if (rewardedId.trim().isEmpty()) {
            Log.e(TAG_ADS, "$adType -> loadRewarded: Ad id is empty")
            listener?.onResponse(false)
            return
        }

        Log.d(TAG_ADS, "$adType -> loadRewarded: Requesting admob server for ad...")
        isRewardedLoading = true

        RewardedAd.load(
            context,
            rewardedId.trim(),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG_ADS, "$adType -> loadRewarded: onAdFailedToLoad: ${adError.message}")
                    isRewardedLoading = false
                    mRewardedAd = null
                    listener?.onResponse(false)
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.i(TAG_ADS, "$adType -> loadRewarded: onAdLoaded")
                    isRewardedLoading = false
                    mRewardedAd = rewardedAd
                    listener?.onResponse(true)
                }
            })
    }

    protected fun showRewarded(
        activity: Activity?,
        adType: String,
        isAppPurchased: Boolean,
        listener: RewardedOnShowCallBack?
    ) {

        if (isRewardedLoaded().not()) {
            Log.e(TAG_ADS, "$adType -> showRewarded: Rewarded is not loaded yet")
            listener?.onAdFailedToShow()
            return
        }

        if (isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> showRewarded: Premium user")
            if (isRewardedLoaded()) {
                Log.d(TAG_ADS, "$adType -> Destroying loaded rewarded ad due to Premium user")
                mRewardedAd = null
            }
            listener?.onAdFailedToShow()
            return
        }

        if (activity == null) {
            Log.e(TAG_ADS, "$adType -> showRewarded: activity reference is null")
            listener?.onAdFailedToShow()
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.e(TAG_ADS, "$adType -> showRewarded: activity is finishing or destroyed")
            listener?.onAdFailedToShow()
            return
        }

        mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG_ADS, "admob Rewarded onAdDismissedFullScreenContent")
                listener?.onAdDismissedFullScreenContent()
                mRewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG_ADS, "admob Rewarded onAdFailedToShowFullScreenContent: ${adError.message}")
                listener?.onAdFailedToShow()
                mRewardedAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG_ADS, "admob Rewarded onAdShowedFullScreenContent")
                listener?.onAdShowedFullScreenContent()
                mRewardedAd = null
            }

            override fun onAdImpression() {
                Log.v(TAG_ADS, "admob Rewarded onAdImpression")
                listener?.onAdImpression()
            }
        }

        Log.d(TAG_ADS, "$adType -> Rewarded: showing ad")
        mRewardedAd?.show(activity) {
            Log.d(TAG_ADS, "admob Rewarded onUserEarnedReward")
            listener?.onUserEarnedReward()
        }
    }

    fun isRewardedLoaded(): Boolean {
        return mRewardedAd != null
    }
}