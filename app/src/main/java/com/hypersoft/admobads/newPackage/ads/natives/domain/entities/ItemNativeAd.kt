package com.hypersoft.admobads.newPackage.ads.natives.domain.entities

import com.google.android.gms.ads.nativead.NativeAd

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

data class ItemNativeAd(
    val adId: String,
    val nativeAd: NativeAd,
    val impressionReceived: Boolean = false
)