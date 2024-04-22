package com.hypersoft.admobads.adsconfig.utils

import android.app.Activity
import android.util.DisplayMetrics

object ScreenUtils {
    fun Activity?.isSupportFullScreen(): Boolean {
        try {
            val outMetrics = DisplayMetrics()
            this?.windowManager?.defaultDisplay?.getMetrics(outMetrics)
            if (outMetrics.heightPixels > 1280) {
                return true
            }
        } catch (ignored: Exception) {}
        return false
    }
}