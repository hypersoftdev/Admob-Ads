# setup-old-project — detect and extract

Companion to [SKILL.md](SKILL.md). Target architecture: [setup-new-project](../setup-new-project/SKILL.md).

## Detect → action

| Current setup                                            | Action                                                                                                              |
|----------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| Gradle Groovy (`build.gradle`, `settings.gradle`)        | Convert to Kotlin DSL + Version Catalog. Keep versions.                                                             |
| No `libs.versions.toml`                                  | Move existing `g:a:v` into the catalog at the **same** versions.                                                    |
| Single-module `:app`                                     | Create mandatory modules; **move** code out of `:app`.                                                              |
| Some modules already exist                               | Fill gaps only; do not rename a working module without need.                                                        |
| MVC / logic in Activity or Fragment                      | Extract data to DataSource/Repository/UseCase; UI to MVI Fragment. Keep XML.                                        |
| MVVM (ViewModel, no Intent/State/Effect)                 | Wrap existing VM work in `handleIntent` / State / Effect. Do not rewrite use cases.                                 |
| Already MVI (Intent/State/Effect)                        | Move packages to `presentation/<feature>/…`; fix DI to `lazyModule`.                                                |
| Activity-per-screen                                      | One `MainActivity` + `nav_graph`; one Fragment per old Activity; preserve back stack.                               |
| Navigation Component already                             | Keep graphs; set start to `entranceFragment`; Entrance routes to the old start destination.                         |
| Hilt / Dagger / `module { }`                             | Same graph as **Koin `lazyModule` / `lazyModules`**.                                                                |
| No DI                                                    | Introduce Koin `lazyModule` while extracting.                                                                       |
| Data Binding / `findViewById`                            | View Binding; same view ids and layout behavior.                                                                    |
| Jetpack Compose                                          | **Stop and ask.** Do not convert to XML unless the user explicitly approves.                                        |
| Java sources                                             | Convert to Kotlin when touching a file / moving into a new layer; keep control flow.                                |
| API in Activity/Presenter                                | DataSource (network) + Repository impl (`:data`) + interface/UseCase (`:domain`). Same URL, method, headers, parse. |
| OkHttp / Volley / Retrofit already                       | Keep the client. Place in `:data`. Add Retrofit only with approval (`00-global`).                                   |
| Room / SQLite                                            | Move DB, entities, DAOs to `:data`. **Do not** migrate schema or change queries.                                    |
| SharedPreferences / DataStore helper                     | Wrap existing file name + keys. Do not reset defaults.                                                              |
| Strings in `:app` / many modules                         | Single shared `:core-ui` `strings.xml` **plus all `values-*` locales**.                                             |
| Themes/colors in `:app`                                  | Move to `:core-ui`. Honor `themeModes`.                                                                             |
| Firebase Analytics/Crashlytics/RC                        | Relocate to `:core-platform` / `:data` as in setup-new-project. Keep event names and RC keys.                       |
| `FirebaseMessagingService` / token UI                    | **Keep.** Only add the messaging dependency if FCM was missing.                                                     |
| AdMob / mediation / `:gmaAds`                            | Keep architecture. Relocate module if ads live in `:app`. Not MVI.                                                  |
| Play Billing                                             | Keep purchase flow; domain interface + `:data` impl if extracting.                                                  |
| `google-services.json` / `google-services` plugin        | Stay on `:app`.                                                                                                     |
| ProGuard / R8 keep rules                                 | Keep; point at new class names after moves.                                                                         |
| Product flavors / build types                            | Keep.                                                                                                               |
| Portrait lock in manifest                                | Remove only if `orientation` is `both` or `landscape`. If user chose `portrait`, keep the lock.                     |
| No `values-night`                                        | Add only if `themeModes` is `night` or `both`.                                                                      |
| Old `compileSdk` / AGP blocking KTS/catalog/View Binding | Minimum bump to compile; report it. Otherwise leave SDK versions.                                                   |

## Extract example — API in an Activity

**Before (preserve this behavior):** Activity builds a URL, enqueues a call, parses JSON, then updates views.

**After (same request and parse):**

1. DataSource in `:data` — the HTTP call + response extraction (copy the existing code; do not “improve” the parser).
2. Repository impl in `:data` — `withContext(ioDispatcher)`; map to a domain entity if a mapper already existed, else keep the same model.
3. Repository interface + UseCase in `:domain`.
4. ViewModel calls the UseCase; Fragment/`renderState` binds the same views.

Do **not** change endpoint, query, headers, error fallback, or empty-state meaning while extracting.

Same idea for: prefs read/write in an Activity, Room queries in an Activity, file I/O in an Activity.

## Screen conversion — Activity → Fragment + MVI

1. Keep the layout file (rename to `fragment_*` only if required). Convert to View Binding.
2. Clicks / text / lists stay in the Fragment as **intent dispatch** (`handleIntent`) + `renderState` — same listeners, same ids.
3. Existing ViewModel methods become `private suspend fun onX()` behind `handleIntent`.
4. Navigation `startActivity` / `finish` become Effects + `navigateTo` / `popFrom` with equivalent destinations.
5. Dialogs/sheets: wrap in `ParentDialog` / `ParentSheet` only if you must move them; keep show/dismiss behavior.
6. Do not restyle, rewrite copy, or drop landscape/tablet layouts the app already has.

Ads screens/managers: skip this conversion (`21-ads-billing`).

## Entrance (preserve first screen)

Old start (examples): `SplashActivity`, `MainActivity`, `LanguageActivity`, `HomeFragment`.

1. Add `EntranceFragment` as `nav_graph` `startDestination`.
2. Put the **existing** startup routing (first-open language, session, RC fetch, go-home) into Entrance MVI / existing use cases.
3. Navigate to the previous start destination without adding a new branded splash unless one already existed.
4. Fetch Remote Config from Entrance / App startup if that matches `setup-new-project` Step 8 — **non-blocking**; do not delay the old first screen behind a new gate unless the app already waited on RC.

## Resources and locales

- Move every `values-<locale>/strings.xml` (and other localized values) to `:core-ui`. Losing a locale is a product regression.
- Drawables/mipmaps: feature-specific may live in `:presentation`; shared/themes in `:core-ui`. Do not drop density buckets.
- `orientation: both` → existing screens must work in portrait **and** landscape (add `layout-land` only where the current layout breaks; do not invent a new visual design).
- `orientation: portrait` or `landscape` → do not add the other orientation.

## Versions and libraries

- Catalog the **current** versions first.
- Do not add libraries without approval (`00-global`). Migrating a hardcoded dep into the catalog is required, not a new library.
- Replacing Data Binding with View Binding is in scope. Replacing Volley/Retrofit/Glide/Picasso with a different stack is **not**, unless the user asks.

## Verification extras

- Diff network/prefs/DB/analytics call sites: same keys, URLs, event names.
- Launch: first screen after Entrance matches the old first user-visible screen.
- Ads placements still load/show on the same screens (premium/RC gates unchanged).
- `assembleDebug` (and existing flavors if any) succeeds.
