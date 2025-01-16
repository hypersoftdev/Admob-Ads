package com.hypersoft.admobads.newPackage.app.onBoarding

import android.view.View
import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentOnBoardingBinding
import com.hypersoft.admobads.newPackage.ads.interstitial.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.newPackage.ads.interstitial.enums.InterAdKey
import com.hypersoft.admobads.newPackage.ads.natives.presentation.enums.NativeAdKey
import com.hypersoft.admobads.newPackage.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.newPackage.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.newPackage.utilities.extensions.navigateTo
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentOnBoarding : BaseFragment<FragmentOnBoardingBinding>(FragmentOnBoardingBinding::inflate) {

    private val viewModelNative by viewModel<ViewModelNative>()

    override fun onViewCreated() {
        loadNativeAd()
        loadInterstitialAd()
        initObservers()

        binding.mbContinueOnBoarding.setOnClickListener { checkInterstitial() }
    }

    private fun loadNativeAd() {
        viewModelNative.loadNativeAd(NativeAdKey.ON_BOARDING)
    }

    private fun loadInterstitialAd() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.ON_BOARDING)
    }

    private fun initObservers() {
        viewModelNative.adViewLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdOnBoarding.setNativeAd(it)
        }
        viewModelNative.loadFailedLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdOnBoarding.visibility = View.GONE
        }
    }

    private fun checkInterstitial() {
        when (diComponent.interstitialAdsConfig.isInterstitialLoaded()) {
            true -> showInterstitialAd()
            false -> navigateScreen()
        }
    }

    private fun showInterstitialAd() {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.ON_BOARDING, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen()
            override fun onAdImpressionDelayed() = navigateScreen()
        })
    }

    private fun navigateScreen() {
        navigateTo(R.id.fragmentOnBoarding, R.id.action_fragmentOnBoarding_to_fragmentHome)
    }
}