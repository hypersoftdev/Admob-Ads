package com.hypersoft.admobads.newPackage.app.entrance

import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentEntranceBinding
import com.hypersoft.admobads.newPackage.ads.natives.presentation.enums.NativeAdKey
import com.hypersoft.admobads.newPackage.ads.natives.presentation.viewModels.ViewModelNative
import com.hypersoft.admobads.newPackage.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.newPackage.utilities.extensions.navigateTo
import com.hypersoft.admobads.newPackage.utilities.utils.withDelay
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FragmentEntrance : BaseFragment<FragmentEntranceBinding>(FragmentEntranceBinding::inflate) {

    private val viewModelNative by activityViewModel<ViewModelNative>()

    override fun onViewCreated() {
        loadNative()
        initObservers()
        withDelay(1000) { navigateScreen() }
    }

    private fun loadNative() {
        viewModelNative.loadNativeAd(NativeAdKey.LANGUAGE)
    }

    private fun initObservers() {
        viewModelNative.adViewLiveData.observe(viewLifecycleOwner) { navigateScreen() }
        viewModelNative.loadFailedLiveData.observe(viewLifecycleOwner) { navigateScreen() }
    }

    private fun navigateScreen() {
        navigateTo(R.id.fragmentEntrance, R.id.action_fragmentEntrance_to_fragmentLanguage)
    }
}