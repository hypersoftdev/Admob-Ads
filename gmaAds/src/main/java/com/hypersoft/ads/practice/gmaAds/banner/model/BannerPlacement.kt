package com.hypersoft.ads.practice.gmaAds.banner.model

import androidx.annotation.StringRes
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager

/**
 * Fallback family. An ad may only fill another placement in the same slot.
 *
 * - [TOP]: [BannerFormat.ADAPTIVE] or [BannerFormat.COLLAPSIBLE_TOP]
 * - [BOTTOM]: [BannerFormat.ADAPTIVE] or [BannerFormat.COLLAPSIBLE_BOTTOM]
 * - [INLINE]: [BannerFormat.INLINE_ADAPTIVE_MAX_HEIGHT] only
 * - [MREC]: [BannerFormat.MEDIUM_RECTANGLE] only
 */
enum class BannerSlot {
    TOP,
    BOTTOM,
    INLINE,
    MREC,
}

enum class BannerFormat {
    ADAPTIVE,
    INLINE_ADAPTIVE_MAX_HEIGHT,
    MEDIUM_RECTANGLE,
    COLLAPSIBLE_TOP,
    COLLAPSIBLE_BOTTOM,
}

fun BannerFormat.isCompatibleWith(slot: BannerSlot): Boolean = when (this) {
    BannerFormat.ADAPTIVE -> slot == BannerSlot.TOP || slot == BannerSlot.BOTTOM
    BannerFormat.COLLAPSIBLE_TOP -> slot == BannerSlot.TOP
    BannerFormat.COLLAPSIBLE_BOTTOM -> slot == BannerSlot.BOTTOM
    BannerFormat.INLINE_ADAPTIVE_MAX_HEIGHT -> slot == BannerSlot.INLINE
    BannerFormat.MEDIUM_RECTANGLE -> slot == BannerSlot.MREC
}

/** RC: 0 off, 1 adaptive, 2 collapsible (direction from [slot]). */
fun anchoredBannerFormat(rc: Int, slot: BannerSlot): BannerFormat {
    if (rc != 2) return BannerFormat.ADAPTIVE
    return when (slot) {
        BannerSlot.TOP -> BannerFormat.COLLAPSIBLE_TOP
        BannerSlot.BOTTOM -> BannerFormat.COLLAPSIBLE_BOTTOM
        else -> BannerFormat.ADAPTIVE
    }
}

data class BannerPlacement(
    @param:StringRes val adUnitResId: Int,
    val slot: BannerSlot,
    val format: (SharedPrefManager) -> BannerFormat,
    val canBeUsedAsFallback: Boolean,
    val canUseAvailableFallback: Boolean,
    val cache: Boolean,
    val isEnabled: (SharedPrefManager) -> Boolean,
    val maxHeightDp: Int = 0,
)