package com.hypersoft.admobads.ads.banner.data.entities

import com.google.android.gms.ads.AdView

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

data class ItemBannerAd(
    val adId: String,
    val adView: AdView,
    val impressionReceived: Boolean = false
)