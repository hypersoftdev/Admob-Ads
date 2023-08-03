package com.hypersoft.admobads.adsconfig

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.hypersoft.admobads.adsconfig.callbacks.RewardedOnLoadCallBack
import com.hypersoft.admobads.adsconfig.callbacks.RewardedOnShowCallBack
import com.hypersoft.admobads.adsconfig.constants.AdsConstants

class AdmobRewardedAds {

    private val AD_TAG = "AdsInformation"

    @SuppressLint("VisibleForTests")
    fun loadRewardedAd(
        activity: Activity?,
        admobRewardedIds: String,
        adEnable: Int,
        isAppPurchased: Boolean,
        isInternetConnected: Boolean,
        mListener: RewardedOnLoadCallBack
    ) {
        activity?.let { mActivity ->
            if (isInternetConnected && adEnable != 0 && !isAppPurchased && !AdsConstants.isRewardedLoading && admobRewardedIds.isNotEmpty()) {
                if (AdsConstants.rewardedAd == null) {
                    AdsConstants.isRewardedLoading = true
                    RewardedAd.load(
                        mActivity,
                        admobRewardedIds,
                        AdRequest.Builder().build(),
                        object : RewardedAdLoadCallback() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                Log.e(AD_TAG, "admob Rewarded onAdFailedToLoad")
                                AdsConstants.isRewardedLoading = false
                                AdsConstants.rewardedAd = null
                                mListener.onAdFailedToLoad(adError.toString())
                            }

                            override fun onAdLoaded(ad: RewardedAd) {
                                Log.d(AD_TAG, "admob Rewarded onAdLoaded")
                                AdsConstants.isRewardedLoading = false
                                AdsConstants.rewardedAd = ad
                                mListener.onAdLoaded()
                            }
                        })
                } else {
                    Log.d(AD_TAG, "admob Rewarded onPreloaded")
                    mListener.onPreloaded()
                }

            } else {
                Log.e(AD_TAG, "adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
                mListener.onAdFailedToLoad("adEnable = $adEnable, isAppPurchased = $isAppPurchased, isInternetConnected = $isInternetConnected")
            }
        }
    }

    fun showRewardedAd(activity: Activity?, mListener: RewardedOnShowCallBack) {
        activity?.let { mActivity ->
            if (AdsConstants.rewardedAd != null) {
                AdsConstants.rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        Log.d(AD_TAG, "admob Rewarded onAdClicked")
                        mListener.onAdClicked()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        Log.d(AD_TAG, "admob Rewarded onAdDismissedFullScreenContent")
                        mListener.onAdDismissedFullScreenContent()
                        AdsConstants.rewardedAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e(AD_TAG, "admob Rewarded onAdFailedToShowFullScreenContent")
                        mListener.onAdFailedToShowFullScreenContent()
                        AdsConstants.rewardedAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(AD_TAG, "admob Rewarded onAdShowedFullScreenContent")
                        mListener.onAdShowedFullScreenContent()
                        AdsConstants.rewardedAd = null
                    }

                    override fun onAdImpression() {
                        Log.d(AD_TAG, "admob Rewarded onAdImpression")
                        mListener.onAdImpression()
                    }
                }
                AdsConstants.rewardedAd?.let { ad ->
                    ad.show(mActivity) { rewardItem ->
                        Log.d(AD_TAG, "admob Rewarded onUserEarnedReward")
                        mListener.onUserEarnedReward()
                    }
                }
            }
        }
    }

    fun isRewardedLoaded(): Boolean {
        return AdsConstants.rewardedAd != null
    }

    fun dismissRewardedLoaded() {
        AdsConstants.rewardedAd = null
    }
}