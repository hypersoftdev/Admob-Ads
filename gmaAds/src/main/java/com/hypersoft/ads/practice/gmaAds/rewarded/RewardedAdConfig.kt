package com.hypersoft.ads.practice.gmaAds.rewarded

import com.hypersoft.ads.practice.gmaAds.R
import com.hypersoft.ads.practice.gmaAds.rewarded.model.RewardedPlacement

/* ------------------------------------------- Keys ------------------------------------------- */

/**
 * Placement ids used by the app (`adsManager.rewarded.load(HOME)`).
 * Add a new constant here, then a matching row in [RewardedAdConfig],
 * plus a resValue in `gmaAds/build.gradle.kts` (debug and release). See `gmaAds/README.md`.
 */
enum class RewardedAdKey(val value: String) {
    HOME("home"),
}

/* ------------------------------------------- Catalog ------------------------------------------- */

/**
 * Single catalog for rewarded placements. Edit this file (and `gmaAds/build.gradle.kts`)
 * to add, remove, or change ads. See `gmaAds/README.md`.
 *
 * [RewardedPlacement] fields:
 * - **adUnitResId** — AdMob unit from `gmaAds/build.gradle.kts`.
 * - **canBeUsedAsFallback** — other placements may show this ad if theirs is missing.
 * - **canUseAvailableFallback** — this placement may show another fallback ad.
 * - **isEnabled** — Remote Config flag (`!= 0` means on).
 */
object RewardedAdConfig {

    operator fun get(key: RewardedAdKey): RewardedPlacement = placements.getValue(key)

    private val placements: Map<RewardedAdKey, RewardedPlacement> = mapOf(
        RewardedAdKey.HOME to RewardedPlacement(
            adUnitResId = R.string.admob_rewarded_home_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            isEnabled = { it.rcRewardedHome != 0 },
        ),
    )
}