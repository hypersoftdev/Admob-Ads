package com.hypersoft.admobads.app.premium

import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentPremiumBinding
import com.hypersoft.admobads.utilities.base.fragments.BaseFragment
import com.hypersoft.admobads.utilities.extensions.popFrom

class FragmentPremium : BaseFragment<FragmentPremiumBinding>(FragmentPremiumBinding::inflate) {

    override fun onViewCreated() {
        binding.mbBackPremium.setOnClickListener { popFrom(R.id.fragmentPremium) }
    }

}