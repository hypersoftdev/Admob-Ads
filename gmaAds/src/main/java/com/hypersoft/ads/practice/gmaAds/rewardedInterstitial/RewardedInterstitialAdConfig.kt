package com.hypersoft.ads.practice.gmaAds.rewardedInterstitial

import com.hypersoft.ads.practice.gmaAds.R
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.model.RewardedInterstitialPlacement

/* ------------------------------------------- Keys ------------------------------------------- */

/**
 * Placement ids used by the app (`adsManager.rewardedInterstitial.load(HOME)`).
 * Add a new constant here, then a matching row in [RewardedInterstitialAdConfig],
 * plus a string in `ad_ids.xml`. See `gmaAds/README.md`.
 */
enum class RewardedInterstitialAdKey(val value: String) {
    HOME("home"),
}

/* ------------------------------------------- Catalog ------------------------------------------- */

/**
 * Single catalog for rewarded interstitial placements. Edit this file (and `ad_ids.xml`)
 * to add, remove, or change ads. See `gmaAds/README.md`.
 *
 * [RewardedInterstitialPlacement] fields:
 * - **adUnitResId** — AdMob unit from `ad_ids.xml`.
 * - **canBeUsedAsFallback** — other placements may show this ad if theirs is missing.
 * - **canUseAvailableFallback** — this placement may show another fallback ad.
 * - **isEnabled** — Remote Config flag (`!= 0` means on).
 */
object RewardedInterstitialAdConfig {

    operator fun get(key: RewardedInterstitialAdKey): RewardedInterstitialPlacement = placements.getValue(key)

    private val placements: Map<RewardedInterstitialAdKey, RewardedInterstitialPlacement> = mapOf(
        RewardedInterstitialAdKey.HOME to RewardedInterstitialPlacement(
            adUnitResId = R.string.admob_rewarded_inter_home_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            isEnabled = { it.rcRewardedInterHome != 0 },
        ),
    )
}