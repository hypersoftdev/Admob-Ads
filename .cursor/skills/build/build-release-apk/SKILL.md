---
name: build-release-apk
description: Configure release signing, find an attached Android device, build the release APK, install it, and launch the app. Use when the user asks to build release apk, install release, or /build-release-apk.
---

# Build Release APK

## Start banner

First user-visible sentence when this skill runs (verbatim):

> We are going to configure release signing, find an attached device, build the release APK, install it, and launch the app.

## Workflow (mandatory)

Follow shared steps in [common.md](../common.md).

1. **Device** — run device discovery. Stop if no usable device.
2. **Signing** — ensure release `signingConfigs` (not debug): find `.jks` / `.keystore` (or ask path), fill credentials via `local.properties`, wire Gradle to read them. Ensure `buildTypes.release` uses `signingConfigs.getByName("release")`.
3. **Build + install** (Windows: `.\gradlew.bat`):

   ```bash
   ./gradlew :app:assembleRelease :app:installRelease
   ```

4. **Launch** — resolve **release** package (`applicationId` only, no debug suffix) and launcher activity; start via `adb`.
5. **On signing failure** — stop; list missing store file / passwords / alias. Do not invent values.
6. **Report** — serial, variant `release`, APK path under `app/build/outputs/apk/release/`, package, success/fail.

## Scope

- Release variant only
- Device required
- May edit `:app` `signingConfigs` wiring and `local.properties` (gitignored) per common.md

## Forbidden

- Using debug signing for release
- Inventing keystore secrets
- Committing `local.properties`, `.jks`, or `.keystore`
- Claiming success without a `device`-state serial
