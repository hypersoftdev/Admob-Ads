# Build skills — shared steps

Used by `build-debug-apk`, `build-release-apk`, and `build-release-bundle`. Follow `.cursor/rules/14-security-secrets.mdc` and `.cursor/rules/reference/gradle.md`.

## Gradle wrapper

- Windows: `.\gradlew.bat …`
- Unix: `./gradlew …`
- Prefer `:app` tasks
- Use a long Shell `block_until_ms` for assemble / install / bundle (builds can exceed several minutes)

## Device discovery (APK skills only)

Required for `build-debug-apk` and `build-release-apk`. Skip for `build-release-bundle`.

1. Run `adb devices`. If `adb` is missing → stop; tell user to install platform-tools / add to `PATH`.
2. Collect serials whose state is `device`. Ignore `unauthorized`, `offline`, and empty lines.
3. **None** → stop. Suggest: enable USB debugging, accept the RSA prompt, or start an emulator.
4. **Multiple** → ask which serial; set `ANDROID_SERIAL` (or pass `-s <serial>`) for later `adb` / Gradle install.
5. **One** → use that serial.

Do not claim install/launch succeeded without a usable device.

## Signing setup (release APK + bundle)

Required for `build-release-apk` and `build-release-bundle`. Skip for debug.

### 1. Ensure release signing is wired

In `:app` `build.gradle.kts`:

- `signingConfigs { create("release") { … } }` exists
- `buildTypes.release` sets `signingConfig = signingConfigs.getByName("release")`

If missing, add them per `.cursor/rules/reference/gradle.md`. Do not switch release to debug signing.

### 2. Keystore file

1. Search for `*.jks` / `*.keystore`: **project root**, then **`app/`**.
2. If found → use that path.
3. If none → **ask the user for the keystore path**. Stop if they decline.

### 3. Credentials via `local.properties` (never invent secrets)

Preferred keys (gitignored `local.properties`):

```properties
RELEASE_STORE_FILE=path/to/App.jks
RELEASE_STORE_PASSWORD=…
RELEASE_KEY_ALIAS=…
RELEASE_KEY_PASSWORD=…
```

1. If passwords / alias are empty in Gradle and missing from `local.properties` → **ask the user once**, write only to `local.properties`.
2. Wire `:app` `signingConfigs.release` to read these properties when not already wired. Example shape:

```kotlin
signingConfigs {
    create("release") {
        val localProps = java.util.Properties().apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) f.inputStream().use { load(it) }
        }
        storeFile = file(localProps.getProperty("RELEASE_STORE_FILE", "") ?: "")
        storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD", "") ?: ""
        keyAlias = localProps.getProperty("RELEASE_KEY_ALIAS", "") ?: ""
        keyPassword = localProps.getProperty("RELEASE_KEY_PASSWORD", "") ?: ""
    }
}
```

3. If `storeFile` was previously a hardcoded empty `file("")`, replace with the `local.properties` wiring (or set `RELEASE_STORE_FILE` and keep reading from props).
4. **Never** commit `local.properties`, `.jks`, or `.keystore`.
5. **Never** leave new secrets hardcoded in `build.gradle.kts` when moving to `local.properties`.
6. On signing failure → stop; list what’s missing (`RELEASE_STORE_FILE`, passwords, alias). Do not invent values.

## Package + launch (APK skills only)

After a successful install:

1. **Package name**
   - Read `applicationId` from `:app` `defaultConfig`.
   - Debug: append `applicationIdSuffix` if present (e.g. `.testing` → `com.example.app.testing`).
   - Release: use `applicationId` only (no debug suffix).
2. **Launcher activity** — from `:app` `AndroidManifest.xml` (MAIN + LAUNCHER). Prefer fully qualified or relative component name Gradle / manifest already use.
3. **Launch**

```bash
adb -s <serial> shell am start -n <package>/<activity>
```

If the activity is unclear:

```bash
adb -s <serial> shell monkey -p <package> -c android.intent.category.LAUNCHER 1
```

## Report checklist

Always report to the user:

| Field | When |
|-------|------|
| Device serial | APK skills |
| Build variant | Always (`debug` / `release`) |
| Artifact path | APK under `app/build/outputs/apk/…` or AAB under `app/build/outputs/bundle/release/` (respect `base.archivesName`) |
| Package name | APK skills |
| Success / failure + error summary | Always |

## Forbidden

- Inventing keystore passwords, aliases, or store paths
- Committing `local.properties`, `.jks`, or `.keystore`
- Claiming device install without a `device`-state serial
- Installing or launching for `build-release-bundle`
