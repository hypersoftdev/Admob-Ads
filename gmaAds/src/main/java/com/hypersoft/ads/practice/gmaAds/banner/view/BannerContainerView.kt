package com.hypersoft.ads.practice.gmaAds.banner.view

import com.google.android.gms.ads.AdView

interface BannerContainerView {

    fun setAdView(adView: AdView)

    fun clearView()
}