package com.hypersoft.ads.practice.gmaAds.banner

import com.hypersoft.ads.practice.gmaAds.R
import com.hypersoft.ads.practice.gmaAds.banner.model.BannerFormat
import com.hypersoft.ads.practice.gmaAds.banner.model.BannerPlacement
import com.hypersoft.ads.practice.gmaAds.banner.model.BannerSlot
import com.hypersoft.ads.practice.gmaAds.banner.model.anchoredBannerFormat

/* ------------------------------------------- Keys ------------------------------------------- */

/**
 * Placement ids used by the app (`adsManager.banner.load(DASHBOARD)`).
 * Add a new constant here, then a matching row in [BannerAdConfig],
 * plus a resValue in `gmaAds/build.gradle.kts` (debug and release). See `gmaAds/README.md`.
 */
enum class BannerAdKey(val value: String) {
    LANGUAGE("language"),
    ON_BOARDING("onBoarding"),
    DASHBOARD("dashboard"),
    FEATURE_ONE("featureOne"),
    FEATURE_TWO("featureTwo"),
}

/* ------------------------------------------- Catalog ------------------------------------------- */

/**
 * Single catalog for banner placements. Edit this file (and `gmaAds/build.gradle.kts`)
 * to add, remove, or change ads. See `gmaAds/README.md`.
 *
 * [BannerPlacement] fields:
 * - **adUnitResId** — AdMob unit from `gmaAds/build.gradle.kts`.
 * - **slot** — fallback family ([BannerSlot.TOP] / [BannerSlot.BOTTOM] /
 *   [BannerSlot.INLINE] / [BannerSlot.MREC]). Fallback never crosses slots.
 * - **format** — resolved from Remote Config (anchored: `0` off, `1` adaptive,
 *   `2` collapsible). INLINE / MREC placements return a fixed format.
 * - **canBeUsedAsFallback** — other same-slot placements may show this ad if
 *   theirs is missing and this ad has not received an impression yet.
 * - **canUseAvailableFallback** — this placement may show another same-slot
 *   placement's ad when that ad has not received an impression yet.
 * - **cache** — when true, the loaded [com.google.android.gms.ads.AdView] stays
 *   in memory after show and is re-attached when the screen returns.
 *   When false, show consumes it from inventory; destroy still runs on the
 *   displayed view.
 * - **isEnabled** — Remote Config flag (`!= 0` means on).
 * - **maxHeightDp** — INLINE only; passed to inline adaptive AdSize.
 */
object BannerAdConfig {

    operator fun get(key: BannerAdKey): BannerPlacement = placements.getValue(key)

    private val placements: Map<BannerAdKey, BannerPlacement> = mapOf(
        BannerAdKey.LANGUAGE to BannerPlacement(
            adUnitResId = R.string.admob_banner_language_id,
            slot = BannerSlot.TOP,
            format = { anchoredBannerFormat(it.rcBannerLanguage, BannerSlot.TOP) },
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = false,
            isEnabled = { it.rcBannerLanguage != 0 },
        ),
        BannerAdKey.ON_BOARDING to BannerPlacement(
            adUnitResId = R.string.admob_banner_on_boarding_id,
            slot = BannerSlot.TOP,
            format = { anchoredBannerFormat(it.rcBannerOnBoarding, BannerSlot.TOP) },
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = false,
            isEnabled = { it.rcBannerOnBoarding != 0 },
        ),
        BannerAdKey.DASHBOARD to BannerPlacement(
            adUnitResId = R.string.admob_banner_dashboard_id,
            slot = BannerSlot.BOTTOM,
            format = { anchoredBannerFormat(it.rcBannerDashboard, BannerSlot.BOTTOM) },
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = true,
            isEnabled = { it.rcBannerDashboard != 0 },
        ),
        BannerAdKey.FEATURE_ONE to BannerPlacement(
            adUnitResId = R.string.admob_banner_feature_one_id,
            slot = BannerSlot.MREC,
            format = { BannerFormat.MEDIUM_RECTANGLE },
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = false,
            isEnabled = { it.rcBannerFeatureOne != 0 },
        ),
        BannerAdKey.FEATURE_TWO to BannerPlacement(
            adUnitResId = R.string.admob_banner_feature_two_id,
            slot = BannerSlot.INLINE,
            format = { BannerFormat.INLINE_ADAPTIVE_MAX_HEIGHT },
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = false,
            isEnabled = { it.rcBannerFeatureTwo != 0 },
            maxHeightDp = 150,
        ),
    )
}