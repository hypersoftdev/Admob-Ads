package com.hypersoft.admobads.ui.fragments.splash

import com.hypersoft.admobads.R
import com.hypersoft.admobads.adsconfig.AdmobInterstitial
import com.hypersoft.admobads.adsconfig.AdmobNativePreload
import com.hypersoft.admobads.adsconfig.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.adsconfig.enums.NativeType
import com.hypersoft.admobads.databinding.FragmentSplashLanguageBinding
import com.hypersoft.admobads.ui.activities.SplashActivity
import com.hypersoft.admobads.ui.fragments.base.BaseFragment

class FragmentSplashLanguage : BaseFragment<FragmentSplashLanguageBinding>(R.layout.fragment_splash_language) {

    private val admobInterstitial by lazy { AdmobInterstitial() }
    private val admobNativePreload by lazy { AdmobNativePreload() }

    override fun onViewCreatedOneTime() {
        binding.mbContinueLanguage.setOnClickListener { onContinueClick() }

        showNativeAd()
    }

    override fun onViewCreatedEverytime() {}


    /**
     * Add Service in Manifest first
     */

    private fun onContinueClick() {
        if (isAdded){
            diComponent.sharedPreferenceUtils.showFirstScreen = false
            (activity as SplashActivity).nextActivity()
            admobInterstitial.showInterstitialAd(activity,object : InterstitialOnShowCallBack {
                override fun onAdDismissedFullScreenContent() {}
                override fun onAdFailedToShowFullScreenContent() {}
                override fun onAdShowedFullScreenContent() {}
                override fun onAdImpression() {}

            })
        }
    }

    private fun showNativeAd(){
        if (isAdded){
            admobNativePreload.showNativeAds(
                activity,
                binding.adsPlaceHolder,
                NativeType.LARGE_ADJUSTED
            )
        }
    }

    override fun navIconBackPressed() {}

    override fun onBackPressed() {}
}