package com.hypersoft.admobads.ui.fragments.home

import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentHomeBinding
import com.hypersoft.admobads.adsconfig.AdmobBannerAds
import com.hypersoft.admobads.adsconfig.callbacks.BannerCallBack
import com.hypersoft.admobads.adsconfig.enums.CollapsiblePositionType
import com.hypersoft.admobads.adsconfig.enums.NativeType
import com.hypersoft.admobads.helpers.firebase.RemoteConstants
import com.hypersoft.admobads.helpers.listeners.DebounceListener.setDebounceClickListener
import com.hypersoft.admobads.ui.activities.MainActivity
import com.hypersoft.admobads.ui.fragments.base.BaseFragment

class FragmentHome : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    /**
     * Don't use AdmobBannerAds in DI
     */
    private val admobBannerAds by lazy { AdmobBannerAds() }

    override fun onViewCreatedOneTime() {
        binding.mbClickHome.setDebounceClickListener {
            navigateTo(R.id.fragmentHome, R.id.action_fragmentHome_to_fragmentSample)
            (activity as MainActivity).checkCounter()
        }
        binding.mbClickBanner.setDebounceClickListener {
            navigateTo(R.id.fragmentHome, R.id.action_fragmentHome_to_fragmentBanner)
            (activity as MainActivity).checkCounter()
        }

        loadAds()
    }

    override fun onViewCreatedEverytime() {}

    override fun navIconBackPressed() {
        onBackPressed()
    }

    override fun onBackPressed() {

    }

    private fun loadAds() {
        admobBannerAds.loadBannerAds(
            activity,
            binding.adsBannerPlaceHolder,
            getResString(R.string.admob_banner_home_ids),
            RemoteConstants.rcvBannerHome,
            diComponent.sharedPreferenceUtils.isAppPurchased,
            diComponent.internetManager.isInternetConnected,
            CollapsiblePositionType.NONE,
            object : BannerCallBack {
                override fun onAdFailedToLoad(adError: String) {}
                override fun onAdLoaded() {}
                override fun onAdImpression() {}
                override fun onPreloaded() {}
                override fun onAdClicked() {}
                override fun onAdClosed() {}
                override fun onAdOpened() {}
                override fun onAdSwipeGestureClicked() {}
            }
        )

        diComponent.admobNativeAds.loadNativeAds(
            activity,
            binding.adsNativePlaceHolder,
            getResString(R.string.admob_native_home_ids),
            RemoteConstants.rcvNativeHome,
            diComponent.sharedPreferenceUtils.isAppPurchased,
            diComponent.internetManager.isInternetConnected,
            NativeType.BANNER,
            object : BannerCallBack {
                override fun onAdFailedToLoad(adError: String) {}
                override fun onAdLoaded() {}
                override fun onAdImpression() {}
                override fun onPreloaded() {}
                override fun onAdClicked() {}
                override fun onAdClosed() {}
                override fun onAdOpened() {}
                override fun onAdSwipeGestureClicked() {}
            }
        )
    }

    override fun onPause() {
        admobBannerAds.bannerOnPause()
        super.onPause()
    }

    override fun onResume() {
        admobBannerAds.bannerOnResume()
        super.onResume()
    }

    override fun onDestroy() {
        admobBannerAds.bannerOnDestroy()
        super.onDestroy()
    }


}