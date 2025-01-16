package com.hypersoft.admobads.newPackage.utilities.utils

import android.os.Handler
import android.os.Looper

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

fun withDelay(delay: Long = 300, callback: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(callback, delay)
}