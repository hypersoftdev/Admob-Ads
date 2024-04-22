package com.hypersoft.admobads.adsconfig.rewarded

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.hypersoft.admobads.adsconfig.rewarded.callbacks.RewardedOnLoadCallBack
import com.hypersoft.admobads.adsconfig.rewarded.callbacks.RewardedOnShowCallBack
import com.hypersoft.admobads.adsconfig.utils.AdsConstants.isRewardedInterLoading
import com.hypersoft.admobads.adsconfig.utils.AdsConstants.rewardedInterAd

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class AdmobRewardedInter {

    /**
     * 0 = Ads Off
     * 1 = Ads On
     */

    fun loadRewardedAd(
        activity: Activity?,
        rewardedIds: String,
        adEnable: Int,
        isAppPurchased: Boolean,
        isInternetConnected: Boolean,
        listener: RewardedOnLoadCallBack? = null
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

        if (rewardedIds.trim().isEmpty()) {
            Log.e("AdsInformation", "onAdFailedToLoad -> Ad id is empty")
            listener?.onAdFailedToLoad("onAdFailedToLoad -> Ad id is empty")
            return
        }

        if (isRewardedInterLoading) {
            Log.e("AdsInformation", "onAdFailedToLoad -> rewarded inter is loading...")
            listener?.onAdFailedToLoad("onAdFailedToLoad -> rewarded inter is loading...")
            return
        }
        
        try {
            if (rewardedInterAd == null) {
                isRewardedInterLoading = true
                RewardedInterstitialAd.load(
                    activity,
                    rewardedIds,
                    AdRequest.Builder().build(),
                    object : RewardedInterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("AdsInformation", "Rewarded onAdFailedToLoad: ${adError.message}")
                            isRewardedInterLoading = false
                            rewardedInterAd = null
                            listener?.onAdFailedToLoad(adError.toString())
                        }

                        override fun onAdLoaded(ad: RewardedInterstitialAd) {
                            Log.i("AdsInformation", "Rewarded onAdLoaded")
                            isRewardedInterLoading = false
                            rewardedInterAd = ad
                            listener?.onAdLoaded()
                        }
                    })
            } else {
                Log.i("AdsInformation", "admob Rewarded onPreloaded")
                listener?.onPreloaded()
            } 
        }catch (ex:Exception){
            Log.e("AdsInformation", "${ex.message}")
        }
    }

    fun showRewardedAd(activity: Activity?, listener: RewardedOnShowCallBack) {
        activity?.let { mActivity ->
            if (rewardedInterAd != null) {
                rewardedInterAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdsInformation", "admob Rewarded onAdDismissedFullScreenContent")
                        listener.onAdDismissedFullScreenContent()
                        rewardedInterAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdsInformation", "admob Rewarded Interstitial onAdFailedToShowFullScreenContent: ${adError.message}")
                        listener.onAdFailedToShowFullScreenContent()
                        rewardedInterAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d("AdsInformation", "admob Rewarded Interstitial onAdShowedFullScreenContent")
                        listener.onAdShowedFullScreenContent()
                        rewardedInterAd = null
                    }

                    override fun onAdImpression() {
                        Log.d("AdsInformation", "admob Rewarded Interstitial onAdImpression")
                        listener.onAdImpression()
                    }
                }
                rewardedInterAd?.let { ad ->
                    ad.show(mActivity) { rewardItem ->
                        Log.i("AdsInformation", "admob Rewarded Interstitial onUserEarnedReward")
                        listener.onUserEarnedReward()
                    }
                }
            }
        }
    }

    fun isRewardedLoaded(): Boolean {
        return rewardedInterAd != null
    }

    fun dismissRewarded() {
        rewardedInterAd = null
    }
}