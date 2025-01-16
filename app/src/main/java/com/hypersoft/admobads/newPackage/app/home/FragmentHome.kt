package com.hypersoft.admobads.newPackage.app.home

import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentHomeBinding
import com.hypersoft.admobads.newPackage.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.newPackage.utilities.extensions.navigateTo

class FragmentHome : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {



    override fun onViewCreated() {
        binding.mbFeaturesHome.setOnClickListener { navigateTo(R.id.fragmentHome, R.id.action_fragmentHome_to_fragmentFeature) }
        binding.mbSettingsHome.setOnClickListener { navigateTo(R.id.fragmentHome, R.id.action_fragmentHome_to_fragmentSettings) }
    }

}