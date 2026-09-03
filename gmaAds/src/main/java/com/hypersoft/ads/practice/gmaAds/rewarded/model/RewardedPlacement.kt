package com.hypersoft.ads.practice.gmaAds.rewarded.model

import androidx.annotation.StringRes
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager

data class RewardedPlacement(
    @param:StringRes val adUnitResId: Int,
    val canBeUsedAsFallback: Boolean,
    val canUseAvailableFallback: Boolean,
    val isEnabled: (SharedPrefManager) -> Boolean,
)