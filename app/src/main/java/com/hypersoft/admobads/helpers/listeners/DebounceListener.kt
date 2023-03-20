package com.hypersoft.admobads.helpers.listeners

import android.os.SystemClock
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar

object DebounceListener {

    private const val DEBOUNCE_DEFAULT_TIME = 500L
    private var lastClickTime: Long = 0

    fun View.setDebounceClickListener(debounceTime: Long = DEBOUNCE_DEFAULT_TIME, action: () -> Unit) {
        this.setOnClickListener(object : View.OnClickListener {


            override fun onClick(v: View) {
                if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                else action()
                lastClickTime = SystemClock.elapsedRealtime()
            }
        })
    }

    fun MaterialToolbar.setDebounceNavigationClickListener(debounceTime: Long = DEBOUNCE_DEFAULT_TIME, action: () -> Unit) {
        this.setNavigationOnClickListener(object : View.OnClickListener {

            override fun onClick(v: View) {
                if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                else action()
                lastClickTime = SystemClock.elapsedRealtime()
            }
        })
    }

    fun MaterialToolbar.setDebounceMenuItemClickListener(debounceTime: Long = DEBOUNCE_DEFAULT_TIME, action: (item: MenuItem?) -> Unit) {
        this.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem?): Boolean {
                if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return false
                else action(item)
                lastClickTime = SystemClock.elapsedRealtime()
                return true
            }
        })
    }
}