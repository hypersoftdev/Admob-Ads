package com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.model

import androidx.annotation.StringRes
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager

data class RewardedInterstitialPlacement(
    @param:StringRes val adUnitResId: Int,
    val canBeUsedAsFallback: Boolean,
    val canUseAvailableFallback: Boolean,
    val isEnabled: (SharedPrefManager) -> Boolean,
)