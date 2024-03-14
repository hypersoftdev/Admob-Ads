package com.hypersoft.admobads.adsconfig

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.hypersoft.admobads.adsconfig.callbacks.RewardedOnLoadCallBack
import com.hypersoft.admobads.adsconfig.callbacks.RewardedOnShowCallBack
import com.hypersoft.admobads.adsconfig.constants.AdsConstants.isRewardedInterLoading
import com.hypersoft.admobads.adsconfig.constants.AdsConstants.rewardedInterAd

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
        listener: RewardedOnLoadCallBack
    ) {
        activity?.let { mActivity ->
            if (isInternetConnected && adEnable != 0 && !isAppPurchased && !isRewardedInterLoading && rewardedIds.isNotEmpty()) {
                if (rewardedInterAd == null) {
                    isRewardedInterLoading = true
                    RewardedInterstitialAd.load(
                        mActivity,
                        rewardedIds,
                        AdRequest.Builder().build(),
                        object : RewardedInterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                Log.e("AdsInformation", "Rewarded onAdFailedToLoad: ${adError.message}")
                                isRewardedInterLoading = false
                                rewardedInterAd = null
                                listener.onAdFailedToLoad(adError.toString())
                            }

                            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                                Log.d("AdsInformation", "Rewarded onAdLoaded")
                                isRewardedInterLoading = false
                                rewardedInterAd = ad
                                listener.onAdLoaded()
                            }
                        })
                } else {
                    Log.d("AdsInformation", "admob Rewarded onPreloaded")
                    listener.onPreloaded()
                }

            } else {
                Log.e("AdsInformation", "adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
                listener.onAdFailedToLoad("adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
            }
        }
    }

    fun showRewardedAd(activity: Activity?, listener: RewardedOnShowCallBack) {
        activity?.let { mActivity ->
            if (rewardedInterAd != null) {
                rewardedInterAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        Log.d("AdsInformation", "admob Rewarded onAdClicked")
                        listener.onAdClicked()
                    }

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
                        Log.d("AdsInformation", "admob Rewarded Interstitial onUserEarnedReward")
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