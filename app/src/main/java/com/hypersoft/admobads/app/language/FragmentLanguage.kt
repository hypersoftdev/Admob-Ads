package com.hypersoft.admobads.app.language

import android.view.View
import com.hypersoft.admobads.R
import com.hypersoft.admobads.ads.appOpen.screen.callbacks.AppOpenOnShowCallBack
import com.hypersoft.admobads.ads.appOpen.screen.enums.AppOpenAdKey
import com.hypersoft.admobads.ads.natives.presentation.enums.NativeAdKey
import com.hypersoft.admobads.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.databinding.FragmentLanguageBinding
import com.hypersoft.admobads.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.utilities.extensions.navigateTo
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentLanguage : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    private val viewModelNative by viewModel<ViewModelNative>()

    override fun onViewCreated() {
        loadNative()
        initObservers()

        binding.mbContinueLanguage.setOnClickListener { checkAppOpenAd() }
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
        navigateTo(R.id.fragmentLanguage, R.id.action_fragmentLanguage_to_fragmentOnBoarding)
    }
}