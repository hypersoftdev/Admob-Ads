package com.hypersoft.ads.practice.gmaAds.nativeAd.view

import com.google.android.gms.ads.nativead.NativeAd

interface NativeContainerView {

    fun setNativeAd(nativeAd: NativeAd)

    fun clearView()
}
