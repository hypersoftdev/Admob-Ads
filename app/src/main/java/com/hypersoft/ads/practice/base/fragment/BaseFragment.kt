package com.hypersoft.ads.practice.base.fragment

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.hypersoft.ads.practice.core.base.fragment.ParentFragment
import com.hypersoft.ads.practice.data.sharedPreferences.repository.SharedPrefRepository
import org.koin.android.ext.android.inject

abstract class BaseFragment<T : ViewBinding>(bindingFactory: (LayoutInflater) -> T) : ParentFragment<T>(bindingFactory) {

    protected val sharedPrefRepository: SharedPrefRepository by inject()

}