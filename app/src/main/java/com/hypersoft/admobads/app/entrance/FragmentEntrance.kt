package com.hypersoft.admobads.app.entrance

import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentEntranceBinding
import com.hypersoft.admobads.ads.interstitial.callbacks.InterstitialOnLoadCallBack
import com.hypersoft.admobads.ads.interstitial.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.ads.interstitial.enums.InterAdKey
import com.hypersoft.admobads.ads.natives.presentation.enums.NativeAdKey
import com.hypersoft.admobads.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.utilities.extensions.navigateTo
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentEntrance : BaseFragment<FragmentEntranceBinding>(FragmentEntranceBinding::inflate) {

    private val viewModelNative by viewModel<ViewModelNative>()
    private var responseCounter = 0

    override fun onViewCreated() {
        initRemoteConfigs()
        initObservers()

        binding.mbNavigateEntrance.setOnClickListener { checkInterstitialAd() }
    }

    private fun initRemoteConfigs() {
        diComponent.remoteConfiguration.checkRemoteConfig {
            loadNative()
            loadInterstitial()
        }
    }

    private fun loadNative() {
        viewModelNative.loadNativeAd(NativeAdKey.LANGUAGE)
    }

    private fun loadInterstitial() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.SPLASH, object : InterstitialOnLoadCallBack {
            override fun onResponse(successfullyLoaded: Boolean) = onInterResponse()
        })
    }

    private fun initObservers() {
        viewModelNative.adViewLiveData.observe(viewLifecycleOwner) { onNativeResponse() }
        viewModelNative.loadFailedLiveData.observe(viewLifecycleOwner) { onNativeResponse() }
    }

    private fun onNativeResponse() {
        binding.mtvNativeTextEntrance.setText(R.string.native_response)
        showButton()
    }

    private fun onInterResponse() {
        binding.mtvInterTextEntrance.setText(R.string.inter_response)
        showButton()
    }

    private fun showButton() {
        responseCounter++
        if (responseCounter >= 2 && isAdded) {
            binding.mbNavigateEntrance.isEnabled = true
        }
    }

    private fun checkInterstitialAd() {
        when (diComponent.interstitialAdsConfig.isInterstitialLoaded()) {
            true -> showInterstitial()
            false -> navigateScreen()
        }
    }

    private fun showInterstitial() {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.SPLASH, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen()
            override fun onAdImpressionDelayed() = navigateScreen()
        })
    }

    private fun navigateScreen() {
        navigateTo(R.id.fragmentEntrance, R.id.action_fragmentEntrance_to_fragmentLanguage)
    }
}