package com.hypersoft.ads.practice.gmaAds.nativeAd

import com.hypersoft.ads.practice.gmaAds.R
import com.hypersoft.ads.practice.gmaAds.nativeAd.model.NativePlacement

/* ------------------------------------------- Keys ------------------------------------------- */

/**
 * Placement ids used by the app (`adsManager.native.load(LANGUAGE)`).
 * Add a new constant here, then a matching row in [NativeAdConfig],
 * plus a string in `ad_ids.xml`. See `gmaAds/README.md`.
 */
enum class NativeAdKey(val value: String) {
    LANGUAGE("language"),
    ON_BOARDING("onBoarding"),
    MENU("menu"),
    HOME("home"),
    TRENDING("trending"),
    SETTING("setting"),
    FEATURE_ONE("featureOne"),
    FEATURE_TWO("featureTwo"),
}

/* ------------------------------------------- Catalog ------------------------------------------- */

/**
 * Single catalog for native placements. Edit this file (and `ad_ids.xml`)
 * to add, remove, or change ads. See `gmaAds/README.md`.
 *
 * [NativePlacement] fields:
 * - **adUnitResId** — AdMob unit from `ad_ids.xml`.
 * - **canBeUsedAsFallback** — other placements may show this ad if theirs is missing
 *   and this ad has not received an impression yet.
 * - **canUseAvailableFallback** — this placement may show another placement's ad when
 *   that ad has not received an impression yet; otherwise it loads its own ad unit.
 * - **cache** — when true, the loaded ad stays in memory after show and is re-bound
 *   when the screen returns (no new SDK load). When false, show consumes the ad
 *   and the next load requests a fresh one.
 * - **isEnabled** — Remote Config flag (`!= 0` means on).
 */
object NativeAdConfig {

    operator fun get(key: NativeAdKey): NativePlacement = placements.getValue(key)

    private val placements: Map<NativeAdKey, NativePlacement> = mapOf(
        NativeAdKey.LANGUAGE to NativePlacement(
            adUnitResId = R.string.admob_native_language_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = false,
            isEnabled = { it.rcNativeLanguage != 0 },
        ),
        NativeAdKey.ON_BOARDING to NativePlacement(
            adUnitResId = R.string.admob_native_on_boarding_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = false,
            isEnabled = { it.rcNativeOnBoarding != 0 },
        ),
        NativeAdKey.MENU to NativePlacement(
            adUnitResId = R.string.admob_native_menu_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = false,
            isEnabled = { it.rcNativeMenu != 0 },
        ),
        NativeAdKey.HOME to NativePlacement(
            adUnitResId = R.string.admob_native_home_id,
            canBeUsedAsFallback = false,
            canUseAvailableFallback = false,
            cache = true,
            isEnabled = { it.rcNativeHome != 0 },
        ),
        NativeAdKey.TRENDING to NativePlacement(
            adUnitResId = R.string.admob_native_trending_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = true,
            isEnabled = { it.rcNativeTrending != 0 },
        ),
        NativeAdKey.SETTING to NativePlacement(
            adUnitResId = R.string.admob_native_setting_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = true,
            isEnabled = { it.rcNativeSetting != 0 },
        ),
        NativeAdKey.FEATURE_ONE to NativePlacement(
            adUnitResId = R.string.admob_native_feature_one_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = false,
            isEnabled = { it.rcNativeFeatureOne != 0 },
        ),
        NativeAdKey.FEATURE_TWO to NativePlacement(
            adUnitResId = R.string.admob_native_feature_two_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            cache = false,
            isEnabled = { it.rcNativeFeatureTwo != 0 },
        ),
    )
}