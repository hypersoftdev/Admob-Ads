package com.hypersoft.ads.practice.gmaAds.banner.view

import android.view.ViewGroup
import com.google.android.gms.ads.AdView

internal fun AdView.detachFromParent() {
    (parent as? ViewGroup)?.removeView(this)
}

internal fun AdView.destroySafely() {
    runCatching { pause() }
    detachFromParent()
    destroy()
}
