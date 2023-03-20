package com.hypersoft.admobads.ui.fragments.sample

import com.hypersoft.admobads.R
import com.hypersoft.admobads.adsconfig.callbacks.BannerCallBack
import com.hypersoft.admobads.adsconfig.enums.NativeType
import com.hypersoft.admobads.databinding.FragmentSampleBinding
import com.hypersoft.admobads.helpers.firebase.RemoteConstants
import com.hypersoft.admobads.ui.fragments.base.BaseFragment

class FragmentSample : BaseFragment<FragmentSampleBinding>(R.layout.fragment_sample) {

    override fun onViewCreatedOneTime() {
        loadAds()
    }

    override fun onViewCreatedEverytime() {}

    private fun loadAds() {
        diComponent.admobNativeAds.loadNativeAds(
            activity,
            binding.adsPlaceHolder,
            getResString(R.string.admob_native_home_ids),
            RemoteConstants.rcvNativeHome,
            diComponent.sharedPreferenceUtils.isAppPurchased,
            diComponent.internetManager.isInternetConnected,
            NativeType.FIX,
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

    override fun navIconBackPressed() {
        onBackPressed()
    }

    override fun onBackPressed() {
        popFrom(R.id.fragmentSample)
    }
}