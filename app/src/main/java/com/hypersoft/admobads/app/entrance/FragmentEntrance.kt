package com.hypersoft.admobads.app.entrance

import android.view.View
import com.hypersoft.admobads.R
import com.hypersoft.admobads.ads.appOpen.screen.callbacks.AppOpenOnLoadCallBack
import com.hypersoft.admobads.ads.appOpen.screen.callbacks.AppOpenOnShowCallBack
import com.hypersoft.admobads.ads.appOpen.screen.enums.AppOpenAdKey
import com.hypersoft.admobads.ads.natives.presentation.enums.NativeAdKey
import com.hypersoft.admobads.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.databinding.FragmentEntranceBinding
import com.hypersoft.admobads.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.utilities.extensions.navigateTo
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentEntrance : BaseFragment<FragmentEntranceBinding>(FragmentEntranceBinding::inflate) {

    private val viewModelNative by viewModel<ViewModelNative>()
    private var responseCounter = 0

    override fun onViewCreated() {
        initRemoteConfigs()
        initObservers()

        binding.mbNavigateEntrance.setOnClickListener { checkAppOpenAd() }
    }

    private fun initRemoteConfigs() {
        diComponent.remoteConfiguration.checkRemoteConfig {
            binding.mtvRemoteConfigTextEntrance.visibility = View.GONE
            loadNative()
            loadAppOpen()
            diComponent.appOpenAdManager.loadAppOpen()
        }
    }

    private fun loadNative() {
        binding.mtvNativeTextEntrance.visibility = View.VISIBLE
        viewModelNative.loadNativeAd(NativeAdKey.LANGUAGE)
    }

    private fun loadAppOpen() {
        binding.mtvAppOpenTextEntrance.visibility = View.VISIBLE
        diComponent.appOpenAdsConfig.loadAppOpenAd(AppOpenAdKey.SPLASH, object : AppOpenOnLoadCallBack {
            override fun onResponse(successfullyLoaded: Boolean, errorMessage: String?) = onAppOpenResponse()
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

    private fun onAppOpenResponse() {
        binding.mtvAppOpenTextEntrance.setText(R.string.app_open_response)
        showButton()
    }

    private fun showButton() {
        responseCounter++
        if (responseCounter >= 2 && isAdded) {
            binding.mbNavigateEntrance.isEnabled = true
        }
    }

    private fun checkAppOpenAd() {
        when (diComponent.appOpenAdsConfig.isAppOpenLoaded()) {
            true -> showAppOpen()
            false -> navigateScreen()
        }
    }

    private fun showAppOpen() {
        diComponent.appOpenAdsConfig.showAppOpenAd(activity, AppOpenAdKey.SPLASH, object : AppOpenOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen()
            override fun onAdImpressionDelayed() = navigateScreen()
        })
    }

    private fun navigateScreen() {
        navigateTo(R.id.fragmentEntrance, R.id.action_fragmentEntrance_to_fragmentLanguage)
    }

    override fun onDestroy() {
        super.onDestroy()
        diComponent.appOpenAdManager.isSplash = false
    }
}