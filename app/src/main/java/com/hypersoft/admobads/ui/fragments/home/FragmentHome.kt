package com.hypersoft.admobads.ui.fragments.home

import android.util.Log
import com.hypersoft.admobads.R
import com.hypersoft.admobads.adsconfig.banners.AdmobBanner
import com.hypersoft.admobads.adsconfig.banners.callbacks.BannerCallBack
import com.hypersoft.admobads.adsconfig.banners.enums.BannerType
import com.hypersoft.admobads.adsconfig.natives.AdmobNative
import com.hypersoft.admobads.adsconfig.natives.callbacks.NativeCallBack
import com.hypersoft.admobads.adsconfig.natives.enums.NativeType
import com.hypersoft.admobads.adsconfig.rewarded.AdmobRewarded
import com.hypersoft.admobads.adsconfig.rewarded.callbacks.RewardedOnLoadCallBack
import com.hypersoft.admobads.adsconfig.rewarded.callbacks.RewardedOnShowCallBack
import com.hypersoft.admobads.databinding.FragmentHomeBinding
import com.hypersoft.admobads.helpers.firebase.RemoteConstants
import com.hypersoft.admobads.helpers.listeners.RapidSafeListener.setOnRapidClickSafeListener
import com.hypersoft.admobads.ui.activities.MainActivity
import com.hypersoft.admobads.ui.fragments.base.BaseFragment

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class FragmentHome : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val admobBanner by lazy { AdmobBanner() }
    private val admobNative by lazy { AdmobNative() }
    private val admobRewarded by lazy { AdmobRewarded() }

    override fun onViewCreatedOneTime() {
        binding.mbClickSample.setOnRapidClickSafeListener {
            navigateTo(R.id.fragmentHome, R.id.action_fragmentHome_to_fragmentSample)
            (activity as MainActivity).checkCounter()
        }
        binding.mbClickBanner.setOnRapidClickSafeListener {
            navigateTo(R.id.fragmentHome, R.id.action_fragmentHome_to_fragmentBanner)
            (activity as MainActivity).checkCounter()
        }

        binding.mbClickRewarded.setOnRapidClickSafeListener {
            binding.mbClickRewarded.isEnabled = false
            loadRewardedAd()
        }

        loadAds()
    }

    override fun onViewCreatedEverytime() {}

    override fun navIconBackPressed() {
        onBackPressed()
    }

    override fun onBackPressed() {}

    fun loadRewardedAd(){
        Log.d("AdsInformation", "Call Admob Rewarded")
        admobRewarded.loadRewardedAd(
            activity,
            getString(R.string.admob_rewarded_ids),
            RemoteConstants.rcvRewardAd,
            diComponent.sharedPreferenceUtils.isAppPurchased,
            diComponent.internetManager.isInternetConnected,
            object : RewardedOnLoadCallBack {
                override fun onAdFailedToLoad(adError: String) {
                    binding.mbClickRewarded.isEnabled = true
                }
                override fun onAdLoaded() {
                    showRewardedAd()
                    binding.mbClickRewarded.isEnabled = true
                }
                override fun onPreloaded() {
                    showRewardedAd()
                    binding.mbClickRewarded.isEnabled = true
                }
            }
        )
    }

    fun showRewardedAd(){
        admobRewarded.showRewardedAd(
            activity,
            object : RewardedOnShowCallBack {
                override fun onAdDismissedFullScreenContent() {}
                override fun onAdFailedToShowFullScreenContent() {}
                override fun onAdShowedFullScreenContent() {}
                override fun onUserEarnedReward() {}
            }
        )
    }

    private fun loadAds() {
        Log.d("AdsInformation", "Call Admob Banner")
        admobBanner.loadBannerAds(
            activity,
            binding.adsBannerPlaceHolder,
            getResString(R.string.admob_banner_ids),
            RemoteConstants.rcvBannerAd,
            diComponent.sharedPreferenceUtils.isAppPurchased,
            diComponent.internetManager.isInternetConnected,
            BannerType.ADAPTIVE_BANNER,
            object : BannerCallBack {
                override fun onAdFailedToLoad(adError: String) {}
                override fun onAdLoaded() {}
                override fun onAdImpression() {}
            }
        )

        Log.d("AdsInformation", "Call Admob Native")
        admobNative.loadNativeAds(
            activity,
            binding.adsNativePlaceHolder,
            getResString(R.string.admob_native_ids),
            RemoteConstants.rcvNativeAd,
            diComponent.sharedPreferenceUtils.isAppPurchased,
            diComponent.internetManager.isInternetConnected,
            NativeType.BANNER,
            object : NativeCallBack {
                override fun onAdFailedToLoad(adError: String) {}
                override fun onAdLoaded() {}
                override fun onAdImpression() {}
            }
        )
    }

    override fun onPause() {
        admobBanner.bannerOnPause()
        super.onPause()
    }

    override fun onResume() {
        admobBanner.bannerOnResume()
        super.onResume()
    }

    override fun onDestroy() {
        admobBanner.bannerOnDestroy()
        super.onDestroy()
    }


}