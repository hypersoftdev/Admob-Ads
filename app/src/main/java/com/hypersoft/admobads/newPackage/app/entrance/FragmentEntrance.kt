package com.hypersoft.admobads.newPackage.app.entrance

import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentEntranceBinding
import com.hypersoft.admobads.newPackage.ads.interstitial.presentation.enums.InterAdKey
import com.hypersoft.admobads.newPackage.ads.interstitial.presentation.viewModels.ViewModelInterstitial
import com.hypersoft.admobads.newPackage.ads.natives.presentation.enums.NativeAdKey
import com.hypersoft.admobads.newPackage.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.newPackage.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.newPackage.utilities.extensions.navigateTo
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FragmentEntrance : BaseFragment<FragmentEntranceBinding>(FragmentEntranceBinding::inflate) {

    private val viewModelNative by activityViewModel<ViewModelNative>()
    private val viewModelInterstitial by activityViewModel<ViewModelInterstitial>()
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
        viewModelInterstitial.loadInterAd(InterAdKey.SPLASH)
    }

    private fun initObservers() {
        viewModelNative.adViewLiveData.observe(viewLifecycleOwner) { onNativeResponse() }
        viewModelNative.loadFailedLiveData.observe(viewLifecycleOwner) { onNativeResponse() }
        viewModelInterstitial.interResponseLiveData.observe(viewLifecycleOwner) { onInterResponse() }
        viewModelInterstitial.loadFailedLiveData.observe(viewLifecycleOwner) { onInterResponse() }
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
        if (responseCounter >= 2) {
            binding.mbNavigateEntrance.isEnabled = true
        }
    }

    private fun checkInterstitialAd() {
        viewModelInterstitial.showInterstitialAd()
    }

    private fun navigateScreen() {
        navigateTo(R.id.fragmentEntrance, R.id.action_fragmentEntrance_to_fragmentLanguage)
    }
}