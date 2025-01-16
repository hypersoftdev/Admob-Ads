package com.hypersoft.admobads.newPackage.app.language

import android.view.View
import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentLanguageBinding
import com.hypersoft.admobads.newPackage.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.newPackage.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.newPackage.utilities.extensions.navigateTo
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FragmentLanguage : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    private val viewModelNative by activityViewModel<ViewModelNative>()

    override fun onViewCreated() {
        initObservers()

        binding.mbContinueLanguage.setOnClickListener { navigateTo(R.id.fragmentLanguage, R.id.action_fragmentLanguage_to_fragmentOnBoarding) }
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