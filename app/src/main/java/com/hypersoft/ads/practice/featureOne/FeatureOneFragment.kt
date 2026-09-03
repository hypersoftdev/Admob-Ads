package com.hypersoft.ads.practice.featureOne

import androidx.navigation.fragment.findNavController
import com.hypersoft.ads.practice.base.fragment.BaseFragment
import com.hypersoft.ads.practice.core.extensions.onBackPressedDispatcher
import com.hypersoft.ads.practice.databinding.FragmentFeatureOneBinding
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

class FeatureOneFragment : BaseFragment<FragmentFeatureOneBinding>(FragmentFeatureOneBinding::inflate) {

    override fun onViewCreated() {
        loadInterstitialAd(InterstitialAdKey.BACK_PRESS)
        loadBannerAd(BannerAdKey.FEATURE_ONE, binding.bannerAdViewFeatureOne)
        loadNativeAd(NativeAdKey.FEATURE_ONE, binding.nativeAdViewFeatureOne)
    }

    override fun onResume() {
        super.onResume()
        registerBackPress()
        resumeBannerAd(BannerAdKey.FEATURE_ONE)
    }

    override fun onPause() {
        pauseBannerAd(BannerAdKey.FEATURE_ONE)
        super.onPause()
    }

    private fun registerBackPress() {
        onBackPressedDispatcher { showInterstitialAd(InterstitialAdKey.BACK_PRESS) { findNavController().popBackStack() } }
    }

    override fun onDestroyView() {
        binding.bannerAdViewFeatureOne.clearView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        destroyBannerAd(BannerAdKey.FEATURE_ONE)
        destroyNativeAd(NativeAdKey.FEATURE_ONE)
        super.onDestroy()
    }
}
