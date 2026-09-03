package com.hypersoft.ads.practice.settings

import com.hypersoft.ads.practice.base.fragment.BaseFragment
import com.hypersoft.ads.practice.databinding.FragmentSettingsBinding
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadNativeAd
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    override fun onViewCreated() {
        loadNativeAd(NativeAdKey.SETTING, binding.nativeAdViewSetting)
    }
}