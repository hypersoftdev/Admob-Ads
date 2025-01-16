package com.hypersoft.admobads.newPackage.app.language

import android.view.View
import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentLanguageBinding
import com.hypersoft.admobads.newPackage.ads.interstitial.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.newPackage.ads.interstitial.enums.InterAdKey
import com.hypersoft.admobads.newPackage.ads.natives.presentation.enums.NativeAdKey
import com.hypersoft.admobads.newPackage.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.newPackage.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.newPackage.utilities.extensions.navigateTo
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentLanguage : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    private val viewModelNative by viewModel<ViewModelNative>()

    override fun onViewCreated() {
        loadNative()
        initObservers()

        binding.mbContinueLanguage.setOnClickListener { checkInterstitial() }
    }

    private fun loadNative() {
        viewModelNative.loadNativeAd(NativeAdKey.LANGUAGE)
    }

    private fun initObservers() {
        viewModelNative.adViewLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdLanguage.setNativeAd(it)
        }
        viewModelNative.loadFailedLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdLanguage.visibility = View.GONE
        }
    }

    private fun checkInterstitial() {
        when (diComponent.interstitialAdsConfig.isInterstitialLoaded()) {
            true -> showInterstitialAd()
            false -> navigateScreen()
        }
    }

    private fun showInterstitialAd() {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.SPLASH, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen()
            override fun onAdImpressionDelayed() = navigateScreen()
        })
    }

    private fun navigateScreen() {
        navigateTo(R.id.fragmentLanguage, R.id.action_fragmentLanguage_to_fragmentOnBoarding)
    }
}