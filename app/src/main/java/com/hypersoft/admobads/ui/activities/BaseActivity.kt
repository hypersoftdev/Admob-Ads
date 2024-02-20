package com.hypersoft.admobads.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.hypersoft.admobads.BuildConfig
import com.hypersoft.admobads.helpers.firebase.FirebaseUtils.recordException
import com.hypersoft.admobads.helpers.koin.DIComponent

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

    protected fun debugToast(message: String) {
        try {
            runOnUiThread {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (ex: Exception) {
            ex.recordException("debugToast : ${javaClass.simpleName}")
        }
    }
}