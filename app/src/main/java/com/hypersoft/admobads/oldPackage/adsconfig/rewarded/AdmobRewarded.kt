package com.hypersoft.admobads.oldPackage.adsconfig.rewarded

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.hypersoft.admobads.oldPackage.adsconfig.rewarded.callbacks.RewardedOnLoadCallBack
import com.hypersoft.admobads.oldPackage.adsconfig.rewarded.callbacks.RewardedOnShowCallBack
import com.hypersoft.admobads.oldPackage.adsconfig.utils.AdsConstants.isRewardedLoading
import com.hypersoft.admobads.oldPackage.adsconfig.utils.AdsConstants.rewardedAd

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class AdmobRewarded {

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

        if (isRewardedLoading) {
            Log.e("AdsInformation", "onAdFailedToLoad -> rewarded is loading...")
            listener?.onAdFailedToLoad("onAdFailedToLoad -> rewarded is loading...")
            return
        }

        try {
            if (rewardedAd == null) {
                isRewardedLoading = true
                RewardedAd.load(
                    activity,
                    rewardedIds,
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("AdsInformation", "Rewarded onAdFailedToLoad: ${adError.message}")
                            isRewardedLoading = false
                            rewardedAd = null
                            listener?.onAdFailedToLoad(adError.toString())
                        }

                        override fun onAdLoaded(ad: RewardedAd) {
                            Log.i("AdsInformation", "Rewarded onAdLoaded")
                            isRewardedLoading = false
                            rewardedAd = ad
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

    fun showRewardedAd(activity: Activity?, listener: RewardedOnShowCallBack? = null) {
        activity?.let { mActivity ->
            if (rewardedAd != null) {
                rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdsInformation", "admob Rewarded onAdDismissedFullScreenContent")
                        listener?.onAdDismissedFullScreenContent()
                        rewardedAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdsInformation", "admob Rewarded onAdFailedToShowFullScreenContent: ${adError.message}")

                        listener?.onAdFailedToShowFullScreenContent()
                        rewardedAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d("AdsInformation", "admob Rewarded onAdShowedFullScreenContent")
                        listener?.onAdShowedFullScreenContent()
                        rewardedAd = null
                    }

                    override fun onAdImpression() {
                        Log.d("AdsInformation", "admob Rewarded onAdImpression")
                        listener?.onAdImpression()
                    }
                }
                rewardedAd?.let { ad ->
                    ad.show(mActivity) { rewardItem ->
                        Log.i("AdsInformation", "admob Rewarded onUserEarnedReward")
                        listener?.onUserEarnedReward()
                    }
                }
            }
        }
    }

    fun isRewardedLoaded(): Boolean {
        return rewardedAd != null
    }

    fun dismissRewarded() {
        rewardedAd = null
    }
}