package com.hypersoft.admobads.app.home

import com.hypersoft.admobads.R
import com.hypersoft.admobads.ads.interstitial.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.ads.interstitial.enums.InterAdKey
import com.hypersoft.admobads.databinding.FragmentHomeBinding
import com.hypersoft.admobads.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.utilities.extensions.navigateTo

class FragmentHome : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun onViewCreated() {
        loadInterstitial()

        binding.mbFeaturesHome.setOnClickListener { checkInterstitial(0) }
        binding.mbSettingsHome.setOnClickListener { checkInterstitial(1) }
    }

    private fun checkInterstitial(caseType: Int) {
        when (diComponent.interstitialAdsConfig.isInterstitialLoaded()) {
            true -> showInterstitial(caseType)
            false -> navigateScreen(caseType)
        }
    }

    private fun loadInterstitial() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.FEATURE)
    }

    private fun showInterstitial(caseType: Int) {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.FEATURE, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen(caseType)
            override fun onAdImpressionDelayed() = navigateScreen(caseType)
        })
    }

    private fun navigateScreen(caseType: Int) {
        when (caseType) {
            0 -> navigateTo(R.id.fragmentHome, R.id.action_fragmentHome_to_fragmentFeature)
            1 -> navigateTo(R.id.fragmentHome, R.id.action_fragmentHome_to_fragmentSettings)
        }
    }
}