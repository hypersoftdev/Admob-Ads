package com.hypersoft.admobads.ui.fragments.splash

import androidx.lifecycle.lifecycleScope
import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentSplashLanguageBinding
import com.hypersoft.admobads.adsconfig.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.adsconfig.enums.NativeType
import com.hypersoft.admobads.helpers.observers.GlobalEvent
import com.hypersoft.admobads.ui.activities.SplashActivity
import com.hypersoft.admobads.ui.fragments.base.BaseFragment

class FragmentSplashLanguage : BaseFragment<FragmentSplashLanguageBinding>(R.layout.fragment_splash_language) {

    override fun onViewCreatedOneTime() {
        binding.mbContinueLanguage.setOnClickListener { onContinueClick() }


        GlobalEvent.isPreLoadNativeLoaded.observe(viewLifecycleOwner){
            if (it){
                lifecycleScope.launchWhenResumed {
                    showNativeAd()
                }
            }
        }

    }

    override fun onViewCreatedEverytime() {}


    /**
     * Add Service in Manifest first
     */

    private fun onContinueClick() {
        if (isAdded){
            diComponent.sharedPreferenceUtils.showFirstScreen = false
            (activity as SplashActivity).nextActivity()
            diComponent.admobInterstitialAds.showInterstitialAd(activity,object : InterstitialOnShowCallBack {
                override fun onAdDismissedFullScreenContent() {}
                override fun onAdFailedToShowFullScreenContent() {}
                override fun onAdShowedFullScreenContent() {}
                override fun onAdImpression() {}

            })
        }
    }

    private fun showNativeAd(){
        if (isAdded){
            diComponent.admobPreLoadNativeAds.showNativeAds(
                activity,
                binding.adsPlaceHolder,
                NativeType.LARGE_ADJUSTED
            )
        }
    }

    override fun navIconBackPressed() {}

    override fun onBackPressed() {}
}