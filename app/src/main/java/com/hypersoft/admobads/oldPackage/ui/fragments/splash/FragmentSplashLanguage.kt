package com.hypersoft.admobads.oldPackage.ui.fragments.splash

import com.hypersoft.admobads.R
import com.hypersoft.admobads.oldPackage.adsconfig.interstitial.AdmobInterstitial
import com.hypersoft.admobads.oldPackage.adsconfig.natives.AdmobNativePreload
import com.hypersoft.admobads.oldPackage.adsconfig.interstitial.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.oldPackage.adsconfig.natives.enums.NativeType
import com.hypersoft.admobads.databinding.FragmentSplashLanguageBinding
import com.hypersoft.admobads.oldPackage.ui.activities.SplashActivity
import com.hypersoft.admobads.oldPackage.ui.fragments.base.BaseFragment

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
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