package com.hypersoft.ads.practice.gmaAds.interstitial.model

import androidx.annotation.StringRes
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager

data class InterstitialPlacement(
    @param:StringRes val adUnitResId: Int,
    val canBeUsedAsFallback: Boolean,
    val canUseAvailableFallback: Boolean,
    val navigateOn: InterstitialNavigateOn = InterstitialNavigateOn.IMPRESSION,
    val loadOnStart: Boolean? = null,
    val isEnabled: (SharedPrefManager) -> Boolean,
    val remoteCounter: ((SharedPrefManager) -> Int)? = null,
)
