package com.hypersoft.ads.practice.featureTwo

import androidx.navigation.fragment.findNavController
import com.hypersoft.ads.practice.base.fragment.BaseFragment
import com.hypersoft.ads.practice.core.extensions.onBackPressedDispatcher
import com.hypersoft.ads.practice.databinding.FragmentFeatureTwoBinding
import com.hypersoft.ads.practice.gmaAds.banner.BannerAdKey
import com.hypersoft.ads.practice.gmaAds.common.extensions.destroyBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.destroyNativeAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadInterstitialAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadNativeAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.pauseBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.resumeBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.showInterstitialAd
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAdKey
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey

class FeatureTwoFragment : BaseFragment<FragmentFeatureTwoBinding>(FragmentFeatureTwoBinding::inflate) {

    override fun onViewCreated() {
        loadInterstitialAd(InterstitialAdKey.BACK_PRESS)
        loadBannerAd(BannerAdKey.FEATURE_TWO, binding.bannerAdViewFeatureTwo)
        loadNativeAd(NativeAdKey.FEATURE_TWO, binding.nativeAdViewFeatureTwo)
    }

    override fun onResume() {
        super.onResume()
        registerBackPress()
        resumeBannerAd(BannerAdKey.FEATURE_TWO)
    }

    override fun onPause() {
        pauseBannerAd(BannerAdKey.FEATURE_TWO)
        super.onPause()
    }

    private fun registerBackPress() {
        onBackPressedDispatcher { showInterstitialAd(InterstitialAdKey.BACK_PRESS) { findNavController().popBackStack() } }
    }

    override fun onDestroyView() {
        binding.bannerAdViewFeatureTwo.clearView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        destroyBannerAd(BannerAdKey.FEATURE_TWO)
        destroyNativeAd(NativeAdKey.FEATURE_TWO)
        super.onDestroy()
    }
}
