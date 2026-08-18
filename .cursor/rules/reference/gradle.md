# Gradle, build config, catalog sections/naming, and dependency management

Full detail for `08-gradle.mdc`. Do not delete lines from this file — edit here and keep the rule stub in sync.

## Structure

- Root `build.gradle.kts`: plugins with `apply false` only — no dependencies
- Module scripts: `plugins` → `android { }` → `base { }` (`:app` only) → `dependencies { }`
- `settings.gradle.kts`: `FAIL_ON_PROJECT_REPOS`, filtered repositories

## Module `build.gradle.kts` order

1. `plugins { }` — catalog aliases only
2. `android { }` — sections in the order below (omit what does not belong on that module)
3. `base { }` — **`:app` only** (`archivesName`)
4. `dependencies { }` — **project modules first**, then libraries by section

### `:app` — `android { }` section order (mandatory)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    // google-services / crashlytics / safe-args / parcelize when needed
}

android {
    namespace = "com.company.app"
    compileSdk = 37 // or compileSdk { version = release(37) { minorApiLevel = 1 } } if project uses it

    defaultConfig {
        applicationId = "com.company.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("path/to/App.jks") // or file("") if none
            storePassword = ""
            keyAlias = ""
            keyPassword = ""
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".testing"
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Only if the project already has a kotlin/jvm block — place here; do not invent
    // kotlin { … } / jvmToolchain(17)

    bundle {
        language {
            enableSplit = false
        }
    }
}

base {
    archivesName = "App-Name-Account-v${android.defaultConfig.versionCode}(${android.defaultConfig.versionName})"
}
```

### Library modules (`:presentation`, `:data`, `:domain`, `:core-*`, …)

Same section order as `:app`, but **do not add** app-only pieces:

| Section                 | Library modules                                                    |
|-------------------------|--------------------------------------------------------------------|
| `plugins`               | `android.library` (+ parcelize / safe-args when needed)            |
| `compileSdk`            | Yes                                                                |
| `defaultConfig`         | `minSdk` only (no `applicationId` / `versionCode` / `versionName`) |
| `signingConfigs`        | **Never**                                                          |
| `buildTypes`            | Yes — `isMinifyEnabled = false` for debug + release                |
| `buildFeatures`         | Only when needed (View Binding / `buildConfig` on UI modules)      |
| `compileOptions`        | Yes (Java 17)                                                      |
| `kotlin` / `jvm`        | Only if already present in that project                            |
| `bundle`                | **Never** (app only)                                               |
| `base` / `archivesName` | **Never** (app only)                                               |

```kotlin
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.company.app.presentation"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
```

### `signingConfigs` (always on `:app`)

- If missing → **add** `signingConfigs { create("release") { … } }` and wire `release { signingConfig = signingConfigs.getByName("release") }`
- Search for `*.jks` in this order: **project root**, then **`:app`** directory
- If a `.jks` is found → set `storeFile = file("<absolute-or-relative-path>")`
- If **no** `.jks` → keep `storeFile` / `storePassword` / `keyAlias` / `keyPassword` as **empty strings** (`""`)
- Do not invent passwords; do not copy secrets into skills/docs. Prefer empty placeholders; fill locally or via CI / `local.properties` outside git

### `bundle` (always on `:app`)

Every project `:app` module must include:

```kotlin
bundle {
    language {
        enableSplit = false
    }
}
```

### `base` archivesName (`:app` only)

Format: `{AppName}-{Account}-v{versionCode}({versionName})`

Examples from reference apps: `Qibla-Finder-HS-v35(1.3.5)`, `Music-Player-DGH-v8(1.0.8)`, `Speak-Translate-HS-v1(1.0.1)`.

## Dependencies block â€” sections (mandatory)

Group with **comment headers**. Prefer this order (omit empty sections):

```kotlin
dependencies {
    // Project modules (or list without a header if few)
    implementation(project(":core-common"))
    implementation(project(":core-ui"))
    implementation(project(":domain"))

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
    // Firebase
    // Dependency Injection -> Koin
    // Kotlin Coroutines
    // CameraView / Glide / Lottie / Shimmer (feature libs)
    // Testing
    // Core library desugaring (java.time APIs support on older APIs)
}
```

Rules:

- One blank line between section groups when helpful
- Keep related libs together under the matching header
- Comment out unused deps with `#` / `//` â€” do not leave orphan versions without a section

## Version catalog (`libs.versions.toml`)

### File sections

Keep **three** TOML tables. Inside `[versions]` and `[libraries]`, use the **same comment section headers** as module scripts:

```toml
[versions]
# -------------- Plugins -------------- #
agp = "â€¦"
kotlin = "â€¦"

# -------------- Dependencies -------------- #
coreKtx = "â€¦"
# Splash Screen Api
# Fragment Ktx
# Navigational Components
# Lifecycle
# Google
# Firebase
# Location & Coroutine (for await) calls
# Kotlin Coroutines
# Dependency Injection -> Koin
# Testing
# â€¦

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
# â€¦

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }

# Splash Screen Api
androidx-core-splashscreen = { â€¦ }

# Dependency Injection -> Koin
koin-android = { â€¦ }
# â€¦
```

### Catalog naming

| Kind            | Convention      | Example                                                       |
|-----------------|-----------------|---------------------------------------------------------------|
| Version keys    | camelCase       | `coreKtx`, `koinAndroid`, `playServicesLocation`              |
| Library aliases | kebab-case      | `androidx-core-ktx`, `koin-android`, `play-services-location` |
| Plugin aliases  | kebab-case      | `android-application`, `navigation-safe-args`                 |
| Gradle accessor | dots from kebab | `libs.androidx.core.ktx`, `libs.koin.android`                 |

Rules:

- Alias name mirrors artifact intent (`androidx-fragment-ktx`, not `fragment`)
- Prefer `group` + `name` + `version.ref`; `module = "g:a"` only when matching existing style for that lib
- Plugins and libraries that share a version family share one version key (e.g. one `lifecycle` for viewmodel/runtime/process)
- **Never** hardcode `"group:artifact:version"` in module scripts

```kotlin
// âœ… GOOD
implementation(libs.androidx.core.ktx)

// âŒ BAD
implementation("androidx.core:core-ktx:1.12.0")
```

### Always use latest stable versions

- When **adding** or **updating** any dependency/plugin, use the **latest stable** release available at that time
- Look up current versions (Maven Central / Google Maven / library docs) — do not copy stale versions from memory or old projects
- Prefer stable over alpha/beta/rc unless the user explicitly asks for a pre-release
- Keep AGP, Kotlin, and related plugins compatible with each other when bumping
- Update the version in `[versions]` only — all aliases using `version.ref` pick it up
- When scaffolding a new project (`setup-new-project`), seed the catalog with latest stable for the whole core stack
- **`setup-new-project` mandatory:** Firebase BOM + `firebase-analytics` / `firebase-crashlytics` / `firebase-messaging` on **`:core-platform`**; `firebase-config` + `kotlinx-coroutines-play-services` on **`:data`** (see `implement-firebase-messaging` — no MessagingService). Place `kotlinx-coroutines-play-services` under `# Kotlin Coroutines` / `// Kotlin Coroutines`.

### `gradle-update` (mandatory behavior)

When the user runs **`gradle-update`** (or asks to bump dependencies):

1. Inventory **both** `libs.versions.toml` `[versions]` **and** every hardcoded `"group:artifact:version"` in module Gradle scripts
2. Resolve latest stable for **each** (do not skip Glide / ads / Firebase / etc. because they were hardcoded)
3. If `gradle/libs.versions.toml` is missing → **create** it (`[versions]` / `[plugins]` / `[libraries]` + section comments per this doc / `gradle-organize`)
4. Migrate each hardcoded dep into the catalog (version key + library alias under the correct section, e.g. `# Glide`), then replace with `implementation(libs.…)`
5. Place module `implementation` lines under the matching `//` header (`// Glide`, not under `// Testing`)
6. Migrating an **existing** hardcoded dependency into the catalog is **not** “adding a new library” — it is required on every update run
7. Leave **zero** hardcoded Maven coordinates in `*.gradle.kts` when the update finishes

```kotlin
// ❌ BAD — leftover hardcode after gradle-update
implementation("com.github.bumptech.glide:glide:5.0.5")

// ✅ GOOD — catalog + correct section
// Glide
implementation(libs.glide)
```

```toml
# Glide
glide = "5.0.9"
# …
# Glide
glide = { group = "com.github.bumptech.glide", name = "glide", version.ref = "glide" }
```

## SDK defaults (adjust only on project-wide request)

- minSdk = 24
- targetSdk / compileSdk = **37** (bump when a newer platform ships)
- Java 17 compatibility (`compileOptions`)
- Kotlin official code style

## Dependency scope

| Scope                       | When                                            |
|-----------------------------|-------------------------------------------------|
| `implementation`            | Default for almost everything                   |
| `api`                       | Rare â€” only when types must leak to consumers |
| `testImplementation`        | Unit tests                                      |
| `androidTestImplementation` | Instrumentation tests                           |
| `coreLibraryDesugaring`     | `desugar_jdk_libs` when using `java.time`       |

## Module rules

- Keep modules independent â€” no circular deps
- Feature modules must not depend on each other directly
- Enable View Binding on UI modules â€” never enable Data Binding
- App release: minify + shrink; library modules: minify off

## Build types

- Debug (`:app`): `applicationIdSuffix = ".testing"`, no minify
- Release (`:app`): `signingConfig = signingConfigs.getByName("release")`, minify + shrink + ProGuard/R8
- Library modules: minify off for both debug and release
- Prefer no product flavors unless product requires them
- Signing: always declare `signingConfigs` on `:app` (see above); passwords empty unless already present in the project — prefer CI / `local.properties` over committing secrets

## ProGuard

- Keep rules in **app** module only unless module-specific needs arise
- Preserve (adjust package to app id):
    - `domain.entity.**`
    - `presentation.**.state.**` / `intent.**` / `effect.**` / `model.**`
    - ads entity packages when ads module exists
- Keep Parcelable/Serializable names; keep SourceFile/LineNumberTable for Crashlytics
- `android.enableR8.fullMode=true` when project uses it
- Library modules: `isMinifyEnabled = false` typically

## Other

- **Always** set `bundle.language.enableSplit = false` on `:app` (all locales in one APK/AAB)
- Enable core library desugaring when using `java.time` below API 26
- Place any existing `kotlin { }` / `jvmToolchain` block after `compileOptions` and before `bundle` — do not add JVM blocks to projects that never had them
