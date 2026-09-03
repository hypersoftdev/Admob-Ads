package com.hypersoft.ads.practice.gmaAds.nativeAd.model

import androidx.annotation.StringRes
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager

data class NativePlacement(
    @param:StringRes val adUnitResId: Int,
    val canBeUsedAsFallback: Boolean,
    val canUseAvailableFallback: Boolean,
    val cache: Boolean,
    val isEnabled: (SharedPrefManager) -> Boolean,
)