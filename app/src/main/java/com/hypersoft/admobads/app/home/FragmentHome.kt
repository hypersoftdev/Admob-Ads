package com.hypersoft.admobads.app.home

import android.view.View
import com.hypersoft.admobads.R
import com.hypersoft.admobads.ads.natives.presentation.enums.NativeAdKey
import com.hypersoft.admobads.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.databinding.FragmentHomeBinding
import com.hypersoft.admobads.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.utilities.extensions.navigateTo
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentHome : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModelNative by viewModel<ViewModelNative>()

    override fun onViewCreated() {
        loadNative()
        initObservers()

        binding.mbFeaturesHome.setOnClickListener { navigateTo(R.id.fragmentHome, R.id.action_fragmentHome_to_fragmentFeature) }
        binding.mbSettingsHome.setOnClickListener { navigateTo(R.id.fragmentHome, R.id.action_fragmentHome_to_fragmentSettings) }
    }

    private fun loadNative() {
        viewModelNative.loadNativeAd(NativeAdKey.ON_BOARDING)
    }

    private fun initObservers() {
        viewModelNative.adViewLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdViewHome.setNativeAd(it)
        }
        viewModelNative.loadFailedLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdViewHome.visibility = View.GONE
        }
    }
}