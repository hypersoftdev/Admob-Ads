package com.hypersoft.ads.practice.gmaAds.common.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.core.view.doOnLayout
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun Activity.isSafeForAd(): Boolean = !isFinishing && !isDestroyed

fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}

fun View.hostActivity(): Activity? = context.findActivity()

suspend fun View.awaitBannerWidthDp(): Int = suspendCancellableCoroutine { continuation ->
    val resumeWidth = {
        if (continuation.isActive) {
            val density = resources.displayMetrics.density
            val px = if (width > 0) width else resources.displayMetrics.widthPixels
            continuation.resume((px / density).toInt())
        }
    }
    if (isLaidOut && width > 0) {
        resumeWidth()
        return@suspendCancellableCoroutine
    }
    doOnLayout { resumeWidth() }
}