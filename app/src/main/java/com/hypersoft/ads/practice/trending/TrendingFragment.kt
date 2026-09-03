package com.hypersoft.ads.practice.trending

import com.hypersoft.ads.practice.base.fragment.BaseFragment
import com.hypersoft.ads.practice.databinding.FragmentTrendingBinding
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadNativeAd
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey

class TrendingFragment : BaseFragment<FragmentTrendingBinding>(FragmentTrendingBinding::inflate) {

    override fun onViewCreated() {
        loadNativeAd(NativeAdKey.TRENDING, binding.nativeAdViewTrending)
    }
}