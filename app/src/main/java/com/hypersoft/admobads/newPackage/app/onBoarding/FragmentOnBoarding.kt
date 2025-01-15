package com.hypersoft.admobads.newPackage.app.onBoarding

import android.view.View
import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentOnBoardingBinding
import com.hypersoft.admobads.newPackage.ads.natives.presentation.enums.NativeAdKey
import com.hypersoft.admobads.newPackage.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.newPackage.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.newPackage.utilities.extensions.navigateTo
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FragmentOnBoarding : BaseFragment<FragmentOnBoardingBinding>(FragmentOnBoardingBinding::inflate) {

    private val viewModelNative by activityViewModel<ViewModelNative>()

    override fun onViewCreated() {
        loadNativeAd()
        initObservers()

        binding.mbContinueOnBoarding.setOnClickListener { navigateTo(R.id.fragmentOnBoarding, R.id.action_fragmentOnBoarding_to_fragmentHome) }
    }

    private fun loadNativeAd() {
        viewModelNative.loadNativeAd(NativeAdKey.ON_BOARDING)
    }

    private fun initObservers() {
        viewModelNative.adViewLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdOnBoarding.setNativeAd(it)
        }
        viewModelNative.loadFailedLiveData.observe(viewLifecycleOwner) {
            binding.nativeAdOnBoarding.visibility = View.GONE
        }
    }
}