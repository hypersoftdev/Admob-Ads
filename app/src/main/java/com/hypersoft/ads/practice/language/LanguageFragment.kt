package com.hypersoft.ads.practice.language

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hypersoft.ads.practice.R
import com.hypersoft.ads.practice.base.fragment.BaseFragment
import com.hypersoft.ads.practice.databinding.FragmentLanguageBinding
import com.hypersoft.ads.practice.gmaAds.banner.BannerAdKey
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.pauseBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.resumeBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.showNativeAd
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey
import kotlinx.coroutines.launch

class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    override fun onViewCreated() {
        loadAds()

        binding.btnContinueLanguage.setOnClickListener { navigateScreen() }
    }

    override fun onResume() {
        super.onResume()
        resumeBannerAd(BannerAdKey.LANGUAGE)
    }

    override fun onPause() {
        pauseBannerAd(BannerAdKey.LANGUAGE)
        super.onPause()
    }

    private fun loadAds() {
        loadBannerAd(BannerAdKey.LANGUAGE, binding.bannerAdViewLanguage)
        showNativeAd(NativeAdKey.LANGUAGE, binding.nativeAdViewLanguage)
    }

    private fun navigateScreen() {
        viewLifecycleOwner.lifecycleScope.launch {
            sharedPrefRepository.setLanguageSelected(true)
            findNavController().navigate(R.id.action_languageFragment_to_onboardingFragment)
        }
    }

    override fun onDestroyView() {
        binding.bannerAdViewLanguage.clearView()
        super.onDestroyView()
    }
}