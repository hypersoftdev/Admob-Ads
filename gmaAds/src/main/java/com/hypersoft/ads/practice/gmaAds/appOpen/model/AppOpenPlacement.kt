package com.hypersoft.ads.practice.gmaAds.appOpen.model

import androidx.annotation.StringRes
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager

data class AppOpenPlacement(
    @param:StringRes val adUnitResId: Int,
    val canBeUsedAsFallback: Boolean,
    val canUseAvailableFallback: Boolean,
    val navigateOn: AppOpenNavigateOn = AppOpenNavigateOn.IMPRESSION,
    val isEnabled: (SharedPrefManager) -> Boolean,
)