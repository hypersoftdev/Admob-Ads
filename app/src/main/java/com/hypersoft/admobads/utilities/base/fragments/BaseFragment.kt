package com.hypersoft.admobads.utilities.base.fragments

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.hypersoft.admobads.di.DIComponent

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

abstract class BaseFragment<T : ViewBinding>(bindingFactory: (LayoutInflater) -> T) : ParentFragment<T>(bindingFactory) {

    protected val diComponent by lazy { DIComponent() }

}