# :gmaAds

Google Mobile Ads (AdMob) for Admob-Ads. Screens never talk to AdMob or inject `AdsManager`. They call Fragment / Activity extensions with a placement key.

## How it works

Each format is a pipeline: **config → validation → (interstitial counter) → controller**.

- **Config** (`*AdConfig.kt` + `*AdKey`) is the catalog: ad unit, Remote Config on/off, fallback, cache, navigate-on, banner slot/format.
- **Validator** blocks premium, RC-off, no internet, bad ad unit, unsafe activity.
- **Controller** owns load inventory, fallback, show, cache, destroy.
- **`AdsManager`** is the facade: `adsManager.appOpen` / `banner` / `interstitial` / `native` / `rewarded` / `rewardedInterstitial`.
- **Extensions** (`FragmentExtenions.kt`, `ActivityExtenions.kt`) are what `:app` calls.

Consent runs on Entrance (`ConsentManager`). SDK init is inside each `load`. Fullscreen formats share `FullscreenAdGate` so two overlays do not show at once.

## Paid user

Validators already reject `load` / `show` when `SharedPrefManager.isAppPurchased` is true. `AppOpenLifecycle` also skips `AppOpenLoadingActivity` when the user is paid or offline.

That does **not** release ads already sitting in memory. After a purchase is saved, `:app` must call `adsManager.destroyCachedAds()`. That drops app open, banner, interstitial, native, rewarded, and rewarded interstitial caches (banner/native include ads that never got an impression).

```kotlin
sharedPrefRepository.setAppPurchased(true)
adsManager.destroyCachedAds()
```

Call it on `PurchaseOutcome.Success` and `AlreadyOwned`, and when a mid-session `purchasesState` sync sets the flag true. Do **not** call it from `:data` or `BillingDataSource` — `:data` must not depend on `:gmaAds`. A failed or cancelled purchase must leave the cache alone. Restore at cold start can call it; caches are empty, so it is a no-op.

You do **not** edit controller/validator to add a placement. You add a key, a config row, a `resValue` in both `debug` and `release` in `gmaAds/build.gradle.kts`, a Remote Config flag in `:data`, then `load` / `show` on the screen.

Catalog files, extensions, `gmaAds/build.gradle.kts` ad ids, and Koin are grouped in this order: **App Open → Banner → Interstitial → Native → Rewarded → Rewarded Interstitial**.

## Call shapes

| Format                | Load                                                                          | Show                                                             | Destroy / lifecycle                                                                                                    |
|-----------------------|-------------------------------------------------------------------------------|------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| App Open              | `loadAppOpenAd(key)`                                                          | `showAppOpenAd(key)` or `showAppOpenOrInterstitialAd(ao, inter)` | `blockAppOpen` / `unblockAppOpen`                                                                                      |
| Banner                | `loadBannerAd(key, container)` only (load+show)                               | —                                                                | `pause` / `resume`; `container.clearView()` in `onDestroyView`; `destroyBannerAd` in `onDestroy` when leaving for good |
| Interstitial          | `loadInterstitialAd(key)`                                                     | `showInterstitialAd(key) { continue }`                           | none                                                                                                                   |
| Native                | `loadNativeAd(key)` preload **or** `loadNativeAd(key, container)` same screen | `showNativeAd(key, container)` after preload                     | `destroyNativeAd` when the screen is gone and `cache = false`                                                          |
| Rewarded              | `loadRewardedAd(key)`                                                         | `showRewardedAd(key) { granted -> }`                             | none                                                                                                                   |
| Rewarded interstitial | `loadRewardedInterstitialAd(key)`                                             | `showRewardedInterstitialAd(key) { granted -> }`                 | none                                                                                                                   |

Do not inject `AdsManager` in fragments.

## Add / remove a placement

Same five steps for every format. Extra UI steps only for banner and native.

### Shared steps (all formats)

**Add**

1. Enum value on `*AdKey` (string id is log-only).
2. Matching row in that format’s `*AdConfig` `placements` map.
3. AdMob unit as `resValue` in both `debug` and `release` in `gmaAds/build.gradle.kts`.
4. Remote Config in `:data`:
    - Key constant + `rc*` property on `SharedPrefManager`
    - Default in `RemoteConfigDataSource.DEFAULTS`
    - Copy into prefs in `RemoteConfigRepositoryImpl`
    - Same key in Firebase Remote Config
5. `isEnabled = { it.rcYourFlag != 0 }` on the placement (banner TOP/BOTTOM also uses the same RC int for format: `1` adaptive, `2` collapsible).
6. `load` / `show` on the screen (see format below).

**Remove**

1. Delete load/show (and XML container) from the screen.
2. Delete the config row and enum value.
3. Delete the `resValue` from both `debug` and `release` in `gmaAds/build.gradle.kts`.
4. Delete the RC key from `:data` and Firebase.
5. Search the key name and confirm nothing still references it.

RC `0` turns a placement off without deleting it.

---

### App Open

Files: `appOpen/AppOpenAdConfig.kt`, `AppOpenAdKey`

Placement fields: `adUnitResId`, `canBeUsedAsFallback`, `canUseAvailableFallback`, `navigateOn` (`IMPRESSION` or `DISMISS`), `isEnabled`.

Two roles:

| Key         | When                                                                                                                                   |
|-------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `ENTRANCE`  | Load on Entrance with interstitial. Show via `showAppOpenOrInterstitialAd` (app open first; interstitial if app open did not display). |
| `LIFECYCLE` | Process foreground.                                                                                                                    |

`AppOpenAdConfig.LOAD_LIFECYCLE_WITH_LOADING_SCREEN`:

- **true** — `AppOpenLoadingActivity` loads then shows `LIFECYCLE`. Do not preload or reload after dismiss.
- **false** — load `LIFECYCLE` on Dashboard; `AppOpenLifecycle` shows it and reloads after dismiss.

`shouldBlock` is true on Entrance (`blockAppOpen()`). `showAppOpenOrInterstitialAd` clears it. Keep it true through splash so a process `ON_START` does not steal the splash ad. Dashboard calls `unblockAppOpen()`.

**Add:** key + config + `resValue` + RC (`!= 0` on). Load where you want inventory; show on the event. For a new splash-style ad, use `showAppOpenOrInterstitialAd` or `showAppOpenAd`. For a new resume ad, you usually extend `LIFECYCLE`, not a third key.

**Remove:** drop load/show. If you remove `LIFECYCLE`, also stop `AppOpenLifecycle` / loading activity usage. If you remove `ENTRANCE`, Entrance should `showInterstitialAd` only (or navigate with no fullscreen).

---

### Banner

Files: `banner/BannerAdConfig.kt`, `BannerAdKey`

Placement fields: `adUnitResId`, `slot`, `format`, `canBeUsedAsFallback`, `canUseAvailableFallback`, `cache`, `isEnabled`, `maxHeightDp` (INLINE only).

Slots (fallback never crosses slots):

| Slot             | Format                         | RC                                     |
|------------------|--------------------------------|----------------------------------------|
| `TOP` / `BOTTOM` | adaptive or collapsible        | `0` off, `1` adaptive, `2` collapsible |
| `MREC`           | 300×250                        | `0` off, `!= 0` on                     |
| `INLINE`         | inline adaptive, `maxHeightDp` | `0` off, `!= 0` on                     |

Fallback only if the source ad has **not** received an impression. Same slot only (top adaptive ↔ collapsible_top, never MREC ↔ adaptive, never top ↔ bottom).

`cache = true` (Dashboard): keep the `AdView`, re-attach on return. `cache = false`: show consumes it.

XML: `BannerAdView`, `layout_width` `0dp` or `wrap_content`, `layout_height` `wrap_content`. Do not hardcode 300×250 or inline max height.

Screen:

```kotlin
loadBannerAd(BannerAdKey.YOURS, binding.bannerAdViewYours)
// onResume / onPause
resumeBannerAd(BannerAdKey.YOURS)
pauseBannerAd(BannerAdKey.YOURS)
// onDestroyView — detach only, do not destroy if cache = true
binding.bannerAdViewYours.clearView()
// onDestroy — when the screen is gone for good
destroyBannerAd(BannerAdKey.YOURS)
```

No Entrance preload. No split `load` then `show`.

Current map: `LANGUAGE` / `ON_BOARDING` (`TOP`, `cache = false`), `DASHBOARD` (`BOTTOM`, `cache = true`), `FEATURE_ONE` (`MREC`, `cache = false`), `FEATURE_TWO` (`INLINE`, `maxHeightDp = 150`, `cache = false`).

**Add:** shared steps + XML `BannerAdView` + `loadBannerAd` + pause/resume + `clearView` + `destroyBannerAd`.

**Remove:** those calls and the XML view, then config / ids / RC.

---

### Interstitial

Files: `interstitial/InterstitialAdConfig.kt`, `InterstitialAdKey`

Placement fields: `adUnitResId`, fallback flags, `navigateOn`, `isEnabled`, optional `loadOnStart` + `remoteCounter`.

`navigateOn`: `IMPRESSION` continues as soon as the ad is shown; `DISMISS` waits until close (`BACK_PRESS` uses dismiss).

Frequency cap (`loadOnStart` not null): RC counter `n` means load on the n-th eligible call.

- `loadOnStart = true`, n = 5 → 5 (load), 1, 2, 3, 4 (load)…
- `loadOnStart = false`, n = 5 → 1, 2, 3, 4 (load)…

A tick commits only when a real SDK request starts (not SkipFallback / AlreadyLoaded / AlreadyLoading). `loadOnStart = null` → load every call (Entrance, Onboarding, Exit).

**Add:** shared steps. Load on the screen that can wait; show on the click/back/tab with `showInterstitialAd(key) { /* navigate */ }`.

**Remove:** load/show, then config / ids / RC (and `counterInter*` if capped).

Current map: `ENTRANCE` (Entrance load, show before Language/Onboarding/Menu), `ON_BOARDING` (Continue), `HOME` (Home buttons, fallbacks on, capped), `BOTTOM_NAVIGATION` (tab change, fallbacks off, capped), `BACK_PRESS` (Feature One/Two back, fallbacks off, capped, navigate on dismiss), `EXIT` (before Exit, fallbacks off).

---

### Native

Files: `nativeAd/NativeAdConfig.kt`, `NativeAdKey`

Placement fields: `adUnitResId`, fallback flags, `cache`, `isEnabled`. RC `0` off, `!= 0` on.

Fallback only if the source ad has **not** received an impression. Using a fallback **moves** that ad to the requesting screen and clears the source cache so it reloads.

`cache = true` (Home / Trending / Setting): re-bind on return, no new SDK load. `cache = false` (funnel / features): show consumes it.

XML container — swap the class to change layout:

- `NativeSmallView`
- `NativeLargeView`
- `NativeLargeSecondView`

Patterns:

```kotlin
// Preload on screen A, show on screen B (Language)
loadNativeAd(NativeAdKey.LANGUAGE)                 // Entrance
showNativeAd(NativeAdKey.LANGUAGE, binding.native) // Language

// Load and show on the same screen
loadNativeAd(NativeAdKey.HOME, binding.nativeAdViewHome)

// Leave a non-cached screen
destroyNativeAd(NativeAdKey.FEATURE_ONE)
```

**Add:** shared steps + XML native view + one of the two load/show patterns + `destroyNativeAd` when `cache = false` and the screen is finished.

**Remove:** those calls and the XML view, then config / ids / RC.

Current map: `LANGUAGE` / `ON_BOARDING` / `MENU` (funnel, fallbacks on, `cache = false`), `HOME` (fallbacks off, `cache = true`), `TRENDING` / `SETTING` (fallbacks on, `cache = true`), `FEATURE_ONE` / `FEATURE_TWO` (`cache = false`).

---

### Rewarded

Files: `rewarded/RewardedAdConfig.kt`, `RewardedAdKey`

Placement fields: `adUnitResId`, fallback flags, `isEnabled`. RC `0` off, `!= 0` on. No cache flag, no navigate-on, no frequency cap.

```kotlin
loadRewardedAd(RewardedAdKey.HOME)
showRewardedAd(RewardedAdKey.HOME) { granted -> /* grant reward only if true */ }
```

Always check `granted`. Failed/skipped show reports `false`.

**Add:** shared steps + load on the screen that can wait + show on the button.

**Remove:** load/show, then config / ids / RC.

---

### Rewarded interstitial

Files: `rewardedInterstitial/RewardedInterstitialAdConfig.kt`, `RewardedInterstitialAdKey`

Same as rewarded: load, show, `rewardGranted`. Different AdMob format and inventory.

```kotlin
loadRewardedInterstitialAd(RewardedInterstitialAdKey.HOME)
showRewardedInterstitialAd(RewardedInterstitialAdKey.HOME) { granted -> }
```

**Add / remove:** same checklist as rewarded, using the rewarded-interstitial key/config/ids/RC.

---

## Fallback rules (all formats)

- `canUseAvailableFallback` — this placement may take another placement’s unused ad.
- `canBeUsedAsFallback` — others may take this placement’s unused ad.
- Native/banner: no fallback after impression.
- Banner: same `BannerSlot` only.

## Remote Config cheat sheet

| Format                                                          | Off | On                            |
|-----------------------------------------------------------------|-----|-------------------------------|
| App Open, interstitial, native, rewarded, rewarded interstitial | `0` | `!= 0`                        |
| Banner TOP/BOTTOM                                               | `0` | `1` adaptive, `2` collapsible |
| Banner MREC/INLINE                                              | `0` | `!= 0`                        |
| Interstitial counter                                            | —   | integer `n` for n-1 cap       |

RC is fetched into `SharedPrefManager`. Placements read prefs, not Firebase, at load time.

## What not to change for a new placement

Leave these alone unless you are changing engine behavior: `*Ads.kt`, `*AdController.kt`, `*AdValidator.kt`, `AdsSdk`, `FullscreenAdGate`, `ConsentManager`. A new **format** (not placement) would need that stack plus Koin in `GmaAdsModule.kt` and `AdsManager`.
