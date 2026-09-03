package com.hypersoft.ads.practice.menu

import androidx.navigation.fragment.findNavController
import com.hypersoft.ads.practice.R
import com.hypersoft.ads.practice.base.fragment.BaseFragment
import com.hypersoft.ads.practice.databinding.FragmentMenuBinding
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadNativeAd
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey

class MenuFragment : BaseFragment<FragmentMenuBinding>(FragmentMenuBinding::inflate) {

    override fun onViewCreated() {
        loadNative()

        binding.btnOpenDashboardMenu.setOnClickListener { findNavController().navigate(R.id.action_menuFragment_to_dashboardFragment) }
    }

    private fun loadNative() {
        loadNativeAd(NativeAdKey.MENU, binding.nativeAdViewMenu)
    }
}