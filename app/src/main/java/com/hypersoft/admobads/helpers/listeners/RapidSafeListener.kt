package com.hypersoft.admobads.helpers.listeners

import android.os.SystemClock
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
object RapidSafeListener {
    private const val RAPID_DEFAULT_TIME = 500L
    private var lastClickTime: Long = 0

    fun View.setOnRapidClickSafeListener(rapidTime: Long = RAPID_DEFAULT_TIME, action: () -> Unit) {
        this.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (SystemClock.elapsedRealtime() - lastClickTime < rapidTime) return
                else action()
                lastClickTime = SystemClock.elapsedRealtime()
            }
        })
    }
}