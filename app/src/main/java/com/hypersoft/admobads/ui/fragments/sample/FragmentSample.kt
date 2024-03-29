package com.hypersoft.admobads.ui.fragments.sample

import com.hypersoft.admobads.R
import com.hypersoft.admobads.adsconfig.AdmobNative
import com.hypersoft.admobads.adsconfig.callbacks.BannerCallBack
import com.hypersoft.admobads.adsconfig.enums.NativeType
import com.hypersoft.admobads.databinding.FragmentSampleBinding
import com.hypersoft.admobads.helpers.firebase.RemoteConstants
import com.hypersoft.admobads.ui.fragments.base.BaseFragment

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class FragmentSample : BaseFragment<FragmentSampleBinding>(R.layout.fragment_sample) {

    private val admobNative by lazy { AdmobNative() }

    override fun onViewCreatedOneTime() {
        loadAds()
    }

    override fun onViewCreatedEverytime() {}

    private fun loadAds() {
        admobNative.loadNativeAds(
            activity,
            binding.adsPlaceHolder,
            getResString(R.string.admob_native_ids),
            RemoteConstants.rcvNativeAd,
            diComponent.sharedPreferenceUtils.isAppPurchased,
            diComponent.internetManager.isInternetConnected,
            NativeType.LARGE,
            object : BannerCallBack {
                override fun onAdFailedToLoad(adError: String) {}
                override fun onAdLoaded() {}
                override fun onAdImpression() {}
                override fun onPreloaded() {}
                override fun onAdClicked() {}
                override fun onAdClosed() {}
                override fun onAdOpened() {}
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