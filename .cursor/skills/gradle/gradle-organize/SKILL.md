---
name: gradle-organize
description: Organize Gradle module scripts (plugins/android/base/dependencies), signingConfigs, bundle, and libs.versions.toml into standard section order. Use when cleaning build.gradle.kts, fixing android block order, adding missing signingConfigs/bundle, or the user asks to organize Gradle.
---

# Gradle Organize

Follow `.cursor/rules/08-gradle.mdc` + [reference/gradle.md](../../../rules/reference/gradle.md). Match the standard section headers and order below.

Reference shape: Speak-Translate / Qibla Finder `:app` (`plugins` → `android` sections → `base` → `dependencies`).

## When to run

- Messy / unsorted `dependencies { }` blocks
- `:app` missing `signingConfigs`, `bundle`, or `base { archivesName }`
- `android { }` sections out of order
- Catalog missing section comments or inconsistent aliases
- New libs added at the bottom out of section
- User says “organize gradle” / “fix dependency sections”

## Scope

1. `gradle/libs.versions.toml`
2. Every module `build.gradle.kts` (`app`, `presentation`, `data`, `domain`, `core-*`, ads, features)
3. Do **not** change versions unless user also asks to bump — organizing ≠ upgrading (use `gradle-update` for bumps). If **`gradle-update`** finds hardcodes, it must migrate them into this catalog and place them under the correct section (same headers as below).

---

## Step 1 — Organize `libs.versions.toml`

### Tables (always these three, in order)

1. `[versions]`
2. `[plugins]`
3. `[libraries]`

### `[versions]` layout

```toml
[versions]
# -------------- Plugins -------------- #
agp = "…"
kotlin = "…"
# google services / crashlytics gradle / safe-args plugin versions…

# -------------- Dependencies -------------- #
coreKtx = "…"
appcompat = "…"
material = "…"
activity = "…"
constraintlayout = "…"

# Splash Screen Api
# Fragment Ktx
# Navigational Components
# Lifecycle
# Google
# Firebase
# Location & Coroutine (for await) calls
# Kotlin Coroutines (pure Kotlin; used by domain for Flow)
# Dependency Injection -> Koin
# CameraView
# Glide
# Lottie Animations
# Dots Indicator
# Shimmer Effect
# Language / time APIs
# Testing
```

### `[libraries]` — same section comments, matching order

Put Android Core libs **first without a header** (or under an implied core group), then:

```toml
# Splash Screen Api
# Fragment Ktx
# Navigational Components
# Lifecycle
# Google
# Firebase
# Location & Maps
# Kotlin Coroutines
# Dependency Injection -> Koin
# CameraView
# Glide
# Lottie Animations
# Dots Slider
# Shimmer
# java.time desugaring for API < 26
# Testing
```

### Naming (fix while organizing)

| Kind          | Convention | Example                                        |
|---------------|------------|------------------------------------------------|
| Version key   | camelCase  | `coreKtx`, `koinAndroid`                       |
| Library alias | kebab-case | `androidx-core-ktx` → `libs.androidx.core.ktx` |
| Plugin alias  | kebab-case | `android-application`, `navigation-safe-args`  |

- One shared version key per family (`lifecycle` for viewmodel/runtime/process)
- Prefer `group` + `name` + `version.ref`
- Comment out unused with `#` — keep under the correct section
- **Never** leave hardcoded `"g:a:v"` in module scripts — move into catalog (`gradle-update` must migrate + bump these, not only catalog keys)

---

## Step 2 — Organize each module script shape

Canonical Kotlin for `:app` and libraries lives in [reference/gradle.md](../../../rules/reference/gradle.md) — **do not invent a parallel template**. Apply that order while organizing.

### Top-level order (mandatory)

```
plugins { }
android { }
base { }            // :app only
dependencies { }
```

### `:app` — `android { }` section order (mandatory)

Reorder existing content into this order. **Add** missing `signingConfigs` and `bundle`.

1. `namespace`
2. `compileSdk`
3. `defaultConfig`
4. **`signingConfigs`** (always)
5. `buildTypes` (`debug`, then `release`)
6. `buildFeatures`
7. `compileOptions`
8. `kotlin` / `jvm` / `jvmToolchain` — **only if already in the project**; do not invent
9. **`bundle`** (always)

Then:

10. **`base { archivesName = "…" }`** — outside `android`, before `dependencies`

#### `signingConfigs` rules (always)

1. If `signingConfigs` is missing on `:app` → **add** it and wire `release.signingConfig`.
2. Search for `*.jks` in **project root**, then **`app/`**.
3. If found → set `storeFile = file("<path>")` (prefer path style already used in that repo: absolute or relative).
4. If **not** found → keep `storeFile = file("")` and password/alias strings **empty** (`""`).
5. When **adding** a new block: leave `storePassword` / `keyAlias` / `keyPassword` empty unless values already exist in the file — do not invent secrets.
6. When organizing an existing block: **preserve** existing password/alias values; only fill empty `storeFile` from a discovered `.jks`.

#### `bundle` rules (always on `:app`)

Add if missing: `bundle { language { enableSplit = false } }`. Do not put `bundle` on library modules.

#### `base` archivesName (`:app` only)

Format: `{AppName}-{Account}-v{versionCode}({versionName})`  
Examples: `Qibla-Finder-HS-v35(1.3.5)`, `Music-Player-DGH-v8(1.0.8)`, `Speak-Translate-HS-v1(1.0.1)`.

Derive `AppName` / `Account` from existing `archivesName`, folder name, or `project-settings.json` `appName` — do not invent a random brand.

### Library modules — same format, omit app-only sections

Keep the same **relative** order. **Do not add** what does not belong:

| Section                    | Include?                                                  |
|----------------------------|-----------------------------------------------------------|
| `plugins`                  | Yes (`android.library` + extras the module already needs) |
| `namespace` / `compileSdk` | Yes                                                       |
| `defaultConfig`            | `minSdk` only — no `applicationId` / versions             |
| `signingConfigs`           | **No**                                                    |
| `buildTypes`               | Yes — minify **off** for debug + release                  |
| `buildFeatures`            | Only if UI / needed (`viewBinding`, `buildConfig`)        |
| `compileOptions`           | Yes                                                       |
| `kotlin` / `jvm`           | Only if already present                                   |
| `bundle`                   | **No**                                                    |
| `base`                     | **No**                                                    |

`:domain` / `:core-common` often omit `buildFeatures`. Preserve lean modules — do not add View Binding where unused. Use the library template in [reference/gradle.md](../../../rules/reference/gradle.md).

---

## Step 3 — Organize each module `dependencies { }`

### Block order (mandatory)

1. **Project modules first** (no header if few; optional blank line after)
2. Library sections with `//` headers
3. Omit empty sections

### Canonical section order (use exact header text when present)

```kotlin
dependencies {
    implementation(project(":…"))
    // feature-* / core-* / domain / data / gmaAds as allowed by module boundaries

    // Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Splash Screen Api

    // Lifecycle

    // Fragment Ktx

    // Navigational Components

    // Google

    // Google Play Services, Maps & Location
    // (or shorter: // Google Play Services Location)

    // Firebase

    // Dependency Injection -> Koin
    // (short form OK in small modules: // Koin)

    // Kotlin Coroutines
    // (domain often: // Core coroutine support for Flows)
    // :data and :core-platform also: kotlinx-coroutines-play-services (Task.await)

    // CameraView

    // Lottie Animation

    // Glide

    // Dots Slider

    // Shimmer

    // Testing

    // Core library desugaring (java.time APIs support on older APIs)
}
```

### Per-module expectations (reference)

| Module           | Typical sections                                                                                                                             |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| `:app`           | projects → Android Core → Firebase → Koin → Testing → Desugaring                                                                             |
| `:presentation`  | projects → Android Core → Lifecycle → Fragment → Navigation → Google → Play Services → Firebase → Koin → Camera/Lottie/Glide/Dots/Shimmer    |
| `:data`          | projects → Android Core → Google → Firebase (`firebase-config` + BOM) → Koin → Kotlin Coroutines (`play-services` for `await()`)             |
| `:core-ui`       | projects → Android Core → Splash → Lifecycle → Navigation → Google → Firebase → Koin → Glide                                                 |
| `:core-platform` | projects → Android Core → Firebase (BOM + analytics/crashlytics/`firebase-messaging` mandatory) → Koin → Kotlin Coroutines (`play-services`) |
| `:gmaAds`        | projects → Android Core → Lifecycle → Google (`api` ads) → Koin                                                                              |
| `:domain`        | Coroutines + Koin DSL for `useCaseModule` (+ optional pure `project(":feature-*")`)                                                          |

### Scopes while organizing

- Keep `implementation` as default
- Preserve existing `api` (e.g. ads mediation) — do not silently change to `implementation`
- Keep `testImplementation` / `androidTestImplementation` under `// Testing`
- Keep `coreLibraryDesugaring` under desugaring comment

---

## Step 4 — Verify

- [ ] Catalog has Plugins + Dependencies section banners under `[versions]`
- [ ] `[libraries]` section comments align with module dependency headers
- [ ] `:app`: `android` sections in order; `signingConfigs` present; `bundle.language.enableSplit = false`; `base.archivesName` set
- [ ] `.jks` search done (root → `app/`); empty strings if none
- [ ] Library modules: no `signingConfigs` / `bundle` / `base`
- [ ] Every module: project deps first, then sectioned libs
- [ ] No hardcoded Maven coordinates in `*.gradle.kts`
- [ ] Aliases kebab-case; version keys camelCase
- [ ] Sync/build still works (`assembleDebug` if practical)
- [ ] Module boundaries unchanged (`presentation` still must not depend on `:data`)

## Report to user

```markdown
## Gradle organize summary

- Catalog: …
- Modules touched: …
- Android / signing / bundle / base: …
- Moved / regrouped deps: …
- Left unchanged (versions / api scopes / existing signing secrets): …
```

## Do not

- Add new libraries without approval (`13-libraries-stack`)
- Bump to latest unless user asked
- Invent new section header names when an existing header already matches
- Break `api` vs `implementation` semantics
- Add `signingConfigs` / `bundle` / `base` to library modules
- Invent keystore passwords or commit secrets into docs
- Add `kotlin`/`jvm` blocks to modules that never had them
