package com.hypersoft.ads.practice.gmaAds.interstitial

import com.hypersoft.ads.practice.gmaAds.R
import com.hypersoft.ads.practice.gmaAds.interstitial.model.InterstitialNavigateOn
import com.hypersoft.ads.practice.gmaAds.interstitial.model.InterstitialPlacement

/* ------------------------------------------- Keys ------------------------------------------- */

/**
 * Placement ids used by the app (`adsManager.interstitial.load(HOME)`).
 * Add a new constant here, then a matching row in [InterstitialAdConfig],
 * plus a string in `ad_ids.xml`. See `gmaAds/README.md`.
 */
enum class InterstitialAdKey(val value: String) {
    ENTRANCE("entrance"),
    ON_BOARDING("onBoarding"),
    HOME("home"),
    BOTTOM_NAVIGATION("bottomNavigation"),
    BACK_PRESS("backPress"),
    EXIT("exit"),
}

/* ------------------------------------------- Catalog ------------------------------------------- */

/**
 * Single catalog for interstitial placements. Edit this file (and `ad_ids.xml`)
 * to add, remove, or change ads. See `gmaAds/README.md`.
 *
 * [InterstitialPlacement] fields:
 * - **adUnitResId** — AdMob unit from `ad_ids.xml`.
 * - **canBeUsedAsFallback** — other placements may show this ad if theirs is missing.
 * - **canUseAvailableFallback** — this placement may show another fallback ad.
 * - **navigateOn** — [InterstitialNavigateOn.IMPRESSION] or [InterstitialNavigateOn.DISMISS]
 *   (when the UI should continue after show).
 * - **loadOnStart** — `null` = no frequency cap (load every call).
 *   `true` = first call loads, then n-1. `false` = count from 0, load at n-1.
 *   Example RC counter = 5: true → 5(load), 1, 2, 3, 4(load)…  false → 1, 2, 3, 4(load)…
 *   A load tick is committed only when a real SDK request starts (not on SkipFallback /
 *   AlreadyLoaded / AlreadyLoading).
 * - **isEnabled** — Remote Config flag (`!= 0` means on).
 * - **remoteCounter** — RC n for the n-1 load cap. Omit when [InterstitialPlacement.loadOnStart] is null.
 */
object InterstitialAdConfig {

    operator fun get(key: InterstitialAdKey): InterstitialPlacement = placements.getValue(key)

    private val placements: Map<InterstitialAdKey, InterstitialPlacement> = mapOf(
        // Splash / funnel — load on Entrance, show before Language / Onboarding / Menu.
        InterstitialAdKey.ENTRANCE to InterstitialPlacement(
            adUnitResId = R.string.admob_inter_entrance_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            navigateOn = InterstitialNavigateOn.IMPRESSION,
            isEnabled = { it.rcInterEntrance != 0 },
        ),
        // Load on Onboarding, show on Continue.
        InterstitialAdKey.ON_BOARDING to InterstitialPlacement(
            adUnitResId = R.string.admob_inter_on_boarding_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            navigateOn = InterstitialNavigateOn.IMPRESSION,
            isEnabled = { it.rcInterOnboarding != 0 },
        ),
        // Load on Home, show on Home buttons. Fallbacks on. Frequency-capped.
        InterstitialAdKey.HOME to InterstitialPlacement(
            adUnitResId = R.string.admob_inter_home_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            navigateOn = InterstitialNavigateOn.IMPRESSION,
            isEnabled = { it.rcInterHome != 0 },
            loadOnStart = true,
            remoteCounter = { it.rcCounterInterHome },
        ),
        // Load on Dashboard, show on tab change. Fallbacks off. Frequency-capped.
        InterstitialAdKey.BOTTOM_NAVIGATION to InterstitialPlacement(
            adUnitResId = R.string.admob_inter_bottom_navigation_id,
            canBeUsedAsFallback = false,
            canUseAvailableFallback = false,
            navigateOn = InterstitialNavigateOn.IMPRESSION,
            isEnabled = { it.rcInterBottomNavigation != 0 },
            loadOnStart = false,
            remoteCounter = { it.rcCounterInterBottomNavigation },
        ),
        // Load on Feature One / Two, show on their back press. Fallbacks off. Frequency-capped.
        InterstitialAdKey.BACK_PRESS to InterstitialPlacement(
            adUnitResId = R.string.admob_inter_back_press_id,
            canBeUsedAsFallback = false,
            canUseAvailableFallback = false,
            navigateOn = InterstitialNavigateOn.DISMISS,
            isEnabled = { it.rcInterBackPress != 0 },
            loadOnStart = true,
            remoteCounter = { it.rcCounterInterBackPress },
        ),
        // Load on Dashboard, show before opening Exit. Fallbacks off.
        InterstitialAdKey.EXIT to InterstitialPlacement(
            adUnitResId = R.string.admob_inter_exit_id,
            canBeUsedAsFallback = false,
            canUseAvailableFallback = false,
            navigateOn = InterstitialNavigateOn.IMPRESSION,
            isEnabled = { it.rcInterExit != 0 },
        ),
    )
}