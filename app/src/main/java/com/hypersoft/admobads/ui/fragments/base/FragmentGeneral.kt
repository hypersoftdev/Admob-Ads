package com.hypersoft.admobads.ui.fragments.base

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.hypersoft.admobads.BuildConfig
import com.hypersoft.admobads.helpers.firebase.FirebaseUtils.recordException

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
open class FragmentGeneral : Fragment() {

    private val baseTAG = "BaseTAG"

    protected fun withDelay(delay: Long = 300, block: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(block, delay)
    }

    protected fun getResString(stringId: Int): String {
        return context?.resources?.getString(stringId) ?: ""
    }

    /* ---------- Toast ---------- */

    protected fun showToast(message: String) {
        activity?.let {
            try {
                it.runOnUiThread {
                    Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                }
            } catch (ex: Exception) {
                ex.recordException("showToast : ${it.javaClass.simpleName}")
            }
        }
    }

    protected fun debugToast(message: String) {
        if (BuildConfig.DEBUG) {
            showToast(message)
        }
    }

    protected fun showToast(stringId: Int) {
        val message = getResString(stringId)
        showToast(message)
    }
}