package com.hypersoft.admobads.newPackage.app.settings

import android.view.View
import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentSettingsBinding
import com.hypersoft.admobads.newPackage.ads.natives.presentation.enums.NativeAdKey
import com.hypersoft.admobads.newPackage.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.newPackage.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.newPackage.utilities.extensions.popFrom
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentSettings : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModelNative by viewModel<ViewModelNative>()

    override fun onViewCreated() {
        loadNative()
        initObservers()

        binding.mbBackSettings.setOnClickListener { popFrom(R.id.fragmentSettings) }
    }

    private fun loadNative() {
        viewModelNative.loadNativeAd(NativeAdKey.Settings)
    }

    private fun initObservers() {
        viewModelNative.adViewLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdSettings.setNativeAd(it)
        }
        viewModelNative.loadFailedLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdSettings.visibility = View.GONE
        }
    }
}