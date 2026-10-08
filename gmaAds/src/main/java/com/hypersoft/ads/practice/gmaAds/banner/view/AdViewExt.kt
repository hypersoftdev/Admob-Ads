package com.hypersoft.ads.practice.gmaAds.banner.view

import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView

private val detachedAdListener = object : AdListener() {}

internal fun AdView.detachFromParent() {
    (parent as? ViewGroup)?.removeView(this)
}

internal fun AdView.destroySafely() {
    adListener = detachedAdListener
    runCatching { pause() }
    detachFromParent()
    destroy()
}