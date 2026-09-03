# Admob-Ads Architecture

This file is the project memory for Admob-Ads. Follow it when adding screens, navigation, or app-level structure.

## App structure

- **Single-activity architecture.** `MainActivity` is the only `Activity`. All screens are `Fragment`s hosted in that activity.
- Do not add new activities for app flows (splash, language, onboarding, dashboard, settings, etc.).

## Navigation

- Use **XML Navigation Component** (`NavHostFragment` + navigation graph XML). Do not use Jetpack Compose Navigation.
- Keep the graph in `res/navigation/` (`nav_graph.xml` for the app funnel, `nav_graph_dashboard.xml` for Dashboard tabs).
- Navigate with the Navigation Component APIs (`NavController`, Safe Args if added). Do not replace fragments by hand with `FragmentManager` transactions unless Navigation cannot express the case.

## Start destination flow

Screens run in this order. Do not skip or reorder this funnel unless product logic (already completed onboarding, saved language) explicitly jumps ahead.

1. **Entrance** — first destination; app entry / splash-style gate
2. **Language** — language selection (first-time)
3. **OnBoarding** — onboarding (first-time)
4. **Menu** — returning-user hub
5. **Dashboard** — main app after the funnel (nested bottom nav: Home, Trending, Settings)
6. **Feature One / Feature Two** — screens opened from Home; they live on the root graph as siblings of Dashboard
7. **Exit** — opened from Dashboard back press; Cancel returns to Dashboard, Exit closes the app

Forward navigation:

- First-time user: `Entrance → Language → OnBoarding → Dashboard`
- Returning user: `Entrance → Menu → Dashboard`
- Language already selected, onboarding incomplete: `Entrance → OnBoarding → Dashboard`

## Conventions

- Layouts stay XML (this is not a Compose UI app).
- New feature UI belongs in fragments, not in `MainActivity` beyond the nav host.
- Fragments extend `BaseFragment` (`:app`). ViewBinding lifecycle lives in `ParentFragment` (`:core`).
- Do not inject `AdsManager` in screen fragments. Load/show ads through Fragment/Activity extensions (`loadAppOpenAd` / `showAppOpenAd` / `showAppOpenOrInterstitialAd`, `loadBannerAd(key, container)`, `loadInterstitialAd` / `showInterstitialAd`, `loadNativeAd` / `showNativeAd`, `loadRewardedAd` / `showRewardedAd`, `loadRewardedInterstitialAd` / `showRewardedInterstitialAd`).
- Ads live in the `:gmaAds` module and are invoked from fragments/activity as needed; they do not own navigation. `AppOpenLoadingActivity` is an ads overlay in `:gmaAds`, not an app funnel screen.
- How ads work, and how to add or remove a placement for every format, is in `gmaAds/README.md`. Do not duplicate that catalog here.
- Shared helpers live in the `:core` module (`Constants`, `platform.InternetManager`).
- Persistence and Remote Config live in the `:data` module (`SharedPrefManager` local, `RemoteConfigDataSource` remote). Fetch RC into SharedPreferences; do not read Firebase keys directly from UI.
- Dependency injection is **Koin** (`lazyModule` per feature/module, started from `App`). Do not add a service locator on `Application`.