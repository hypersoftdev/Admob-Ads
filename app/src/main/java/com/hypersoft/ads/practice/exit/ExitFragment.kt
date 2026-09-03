package com.hypersoft.ads.practice.exit

import androidx.navigation.fragment.findNavController
import com.hypersoft.ads.practice.base.fragment.BaseFragment
import com.hypersoft.ads.practice.databinding.FragmentExitBinding

class ExitFragment : BaseFragment<FragmentExitBinding>(FragmentExitBinding::inflate) {

    override fun onViewCreated() {
        binding.mbExitAppExit.setOnClickListener { requireActivity().finishAffinity() }
        binding.mbCancelExit.setOnClickListener { findNavController().popBackStack() }
    }
}