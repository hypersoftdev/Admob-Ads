package com.hypersoft.ads.practice.onboarding

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hypersoft.ads.practice.R
import com.hypersoft.ads.practice.base.fragment.BaseFragment
import com.hypersoft.ads.practice.databinding.FragmentOnboardingBinding
import com.hypersoft.ads.practice.gmaAds.banner.BannerAdKey
import com.hypersoft.ads.practice.gmaAds.common.extensions.destroyBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadInterstitialAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadNativeAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.pauseBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.resumeBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.showInterstitialAd
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAdKey
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey
import kotlinx.coroutines.launch

class OnboardingFragment : BaseFragment<FragmentOnboardingBinding>(FragmentOnboardingBinding::inflate) {

    override fun onViewCreated() {
        loadAds()

        binding.btnContinueOnboarding.setOnClickListener { showInterstitialAd(InterstitialAdKey.ON_BOARDING) { navigateScreen() } }
    }

    override fun onResume() {
        super.onResume()
        resumeBannerAd(BannerAdKey.ON_BOARDING)
    }

    override fun onPause() {
        pauseBannerAd(BannerAdKey.ON_BOARDING)
        super.onPause()
    }

    private fun loadAds() {
        loadBannerAd(BannerAdKey.ON_BOARDING, binding.bannerAdViewOnboarding)
        loadNativeAd(NativeAdKey.ON_BOARDING, binding.nativeAdViewOnboarding)
        loadInterstitialAd(InterstitialAdKey.ON_BOARDING)
    }

    private fun navigateScreen() {
        viewLifecycleOwner.lifecycleScope.launch {
            sharedPrefRepository.setOnboardingCompleted(true)
            findNavController().navigate(R.id.action_onboardingFragment_to_dashboardFragment)
        }
    }

    override fun onDestroyView() {
        binding.bannerAdViewOnboarding.clearView()
        super.onDestroyView()
    }
}