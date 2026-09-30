package com.hypersoft.ads.practice.gmaAds.appOpen

import com.hypersoft.ads.practice.gmaAds.R
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAdConfig.LOAD_LIFECYCLE_WITH_LOADING_SCREEN
import com.hypersoft.ads.practice.gmaAds.appOpen.model.AppOpenNavigateOn
import com.hypersoft.ads.practice.gmaAds.appOpen.model.AppOpenPlacement

/* ------------------------------------------- Keys ------------------------------------------- */

/**
 * Placement ids used by the app (`adsManager.appOpen.load(ENTRANCE)`).
 * Add a new constant here, then a matching row in [AppOpenAdConfig],
 * plus a resValue in `gmaAds/build.gradle.kts` (debug and release). See `gmaAds/README.md`.
 */
enum class AppOpenAdKey(val value: String) {
    LIFECYCLE("lifecycle"),
    ENTRANCE("entrance"),
}

/* ------------------------------------------- Catalog ------------------------------------------- */

/**
 * Single catalog for app-open placements. Edit this file (and `gmaAds/build.gradle.kts`)
 * to add, remove, or change ads. See `gmaAds/README.md`.
 *
 * [LOAD_LIFECYCLE_WITH_LOADING_SCREEN]:
 * - **true** — process ON_START opens [AppOpenLoadingActivity], which loads then shows [AppOpenAdKey.LIFECYCLE].
 *   Do not preload or reload after dismiss; the next resume loads again in the overlay.
 * - **false** — process ON_START only shows a preloaded [AppOpenAdKey.LIFECYCLE] (load from Dashboard,
 *   and reload after [AppOpenLifecycle] show dismisses).
 *
 * [AppOpenPlacement] fields:
 * - **adUnitResId** — AdMob unit from `gmaAds/build.gradle.kts`.
 * - **canBeUsedAsFallback** — other placements may show this ad if theirs is missing.
 * - **canUseAvailableFallback** — this placement may show another fallback ad.
 * - **navigateOn** — [AppOpenNavigateOn.IMPRESSION] or [AppOpenNavigateOn.DISMISS].
 * - **isEnabled** — Remote Config flag (`!= 0` means on).
 */
object AppOpenAdConfig {

    const val LOAD_LIFECYCLE_WITH_LOADING_SCREEN = true

    operator fun get(key: AppOpenAdKey): AppOpenPlacement = placements.getValue(key)

    private val placements: Map<AppOpenAdKey, AppOpenPlacement> = mapOf(
        AppOpenAdKey.LIFECYCLE to AppOpenPlacement(
            adUnitResId = R.string.admob_app_open_lifecycle_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = true,
            navigateOn = AppOpenNavigateOn.DISMISS,
            isEnabled = { it.rcAppOpen != 0 },
        ),
        AppOpenAdKey.ENTRANCE to AppOpenPlacement(
            adUnitResId = R.string.admob_app_open_entrance_id,
            canBeUsedAsFallback = true,
            canUseAvailableFallback = false,
            navigateOn = AppOpenNavigateOn.IMPRESSION,
            isEnabled = { it.rcAppOpenEntrance != 0 },
        ),
    )
}