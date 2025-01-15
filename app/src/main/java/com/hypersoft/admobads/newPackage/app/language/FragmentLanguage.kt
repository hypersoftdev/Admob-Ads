package com.hypersoft.admobads.newPackage.app.language

import android.view.View
import com.hypersoft.admobads.databinding.FragmentLanguageBinding
import com.hypersoft.admobads.newPackage.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.newPackage.utilities.base.fragments.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FragmentLanguage : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    private val viewModelNative by activityViewModel<ViewModelNative>()

    override fun onViewCreated() {
        initObservers()
    }

    private fun initObservers() {
        viewModelNative.adViewLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdLanguage.setNativeAd(it)
        }
        viewModelNative.loadFailedLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdLanguage.visibility = View.GONE
        }
    }
}