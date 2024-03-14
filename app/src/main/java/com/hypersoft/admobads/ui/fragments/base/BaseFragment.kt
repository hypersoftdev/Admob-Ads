package com.hypersoft.admobads.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.hypersoft.admobads.helpers.koin.DIComponent

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
abstract class BaseFragment<T : ViewDataBinding>(@LayoutRes private val layoutId: Int) : BaseNavFragment() {

    /**
     * These properties are only valid between onCreateView and onDestroyView
     * @property binding
     *          -> after onCreateView
     *          -> before onDestroyView
     */
    private var _binding: T? = null
    val binding get() = _binding!!

    private var hasInitializedRootView = false
    private var rootView: View? = null

    protected val diComponent by lazy { DIComponent() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView?.let {
            _binding = DataBindingUtil.bind(it)
            (it.parent as? ViewGroup)?.removeView(rootView)
            return it
        } ?: kotlin.run {
            _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
            rootView = binding.root
            return binding.root
        }
    }

    /**
     *      Use the following method in onViewCreated from escaping reinitializing of views
     *      if (!hasInitializedRootView) {
     *          hasInitializedRootView = true
     *          // Your Code...
     *      }
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!hasInitializedRootView) {
            hasInitializedRootView = true
            onViewCreatedOneTime()
        }
        onViewCreatedEverytime()
    }

    /**
     *  @since : Write code to be called onetime...
     */
    abstract fun onViewCreatedOneTime()

    /**
     *  @since : Write code to be called everytime...
     */
    abstract fun onViewCreatedEverytime()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        hasInitializedRootView = false
        rootView = null
    }
}