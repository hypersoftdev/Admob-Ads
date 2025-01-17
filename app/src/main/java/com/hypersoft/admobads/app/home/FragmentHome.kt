package com.hypersoft.admobads.app.home

import android.view.View
import com.hypersoft.admobads.R
import com.hypersoft.admobads.ads.banner.presentation.enums.BannerAdKey
import com.hypersoft.admobads.ads.banner.presentation.viewModels.ViewModelBanner
import com.hypersoft.admobads.ads.interstitial.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.ads.interstitial.enums.InterAdKey
import com.hypersoft.admobads.databinding.FragmentHomeBinding
import com.hypersoft.admobads.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.utilities.extensions.addCleanView
import com.hypersoft.admobads.utilities.extensions.navigateTo
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentHome : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModelBanner by viewModel<ViewModelBanner>()

    override fun onViewCreated() {
        loadBanner()
        loadInterstitial()
        initObservers()

        binding.mbPremiumHome.setOnClickListener { onPremiumClick() }
        binding.mbFeaturesHome.setOnClickListener { checkInterstitial(0) }
        binding.mbSettingsHome.setOnClickListener { checkInterstitial(1) }
    }

    private fun loadBanner() {
        viewModelBanner.loadBannerAd(BannerAdKey.HOME)
    }

    private fun loadInterstitial() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.FEATURE)
    }

    private fun initObservers() {
        viewModelBanner.adViewLiveData.observe(viewLifecycleOwner) {
            binding.bannerAdViewHome.addCleanView(it)
        }
        viewModelBanner.loadFailedLiveData.observe(viewLifecycleOwner) {
            binding.bannerAdViewHome.visibility = View.GONE
        }
        viewModelBanner.clearViewLiveData.observe(viewLifecycleOwner) {
            binding.bannerAdViewHome.removeAllViews()
        }
    }

    private fun onPremiumClick() {
        viewModelBanner.destroyBanner(BannerAdKey.HOME)
        navigateTo(R.id.fragmentHome, R.id.action_fragmentHome_to_fragmentFeature)
    }

    private fun checkInterstitial(caseType: Int) {
        when (diComponent.interstitialAdsConfig.isInterstitialLoaded()) {
            true -> showInterstitial(caseType)
            false -> navigateScreen(caseType)
        }
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