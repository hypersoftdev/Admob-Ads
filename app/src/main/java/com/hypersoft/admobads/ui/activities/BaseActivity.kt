package com.hypersoft.admobads.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.hypersoft.admobads.helpers.firebase.FirebaseUtils.recordException
import com.hypersoft.admobads.helpers.koin.DIComponent

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
abstract class BaseActivity<T : ViewDataBinding>(@LayoutRes layoutId: Int) : AppCompatActivity() {

    protected val binding by lazy {
        DataBindingUtil.inflate<T>(
            layoutInflater,
            layoutId,
            null,
            false
        )
    }
    protected val diComponent by lazy { DIComponent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    protected fun withDelay(delay: Long = 300, block: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(block, delay)
    }


    /* ---------- Toast ---------- */

    protected fun showToast(message: String) {
        try {
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Exception) {
            ex.recordException("showToast : ${javaClass.simpleName}")
        }
    }
}