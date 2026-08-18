---
name: build-debug-apk
description: Find an attached Android device, build the debug APK, install it, and launch the app. Use when the user asks to build debug apk, install debug, run on device, or /build-debug-apk.
---

# Build Debug APK

## Start banner

First user-visible sentence when this skill runs (verbatim):

> We are going to find an attached device, build the debug APK, install it, and launch the app.

## Workflow (mandatory)

Follow shared steps in [common.md](../common.md).

1. **Device** — run device discovery. Stop if no usable device.
2. **Build + install** (Windows: `.\gradlew.bat`):

   ```bash
   ./gradlew :app:assembleDebug :app:installDebug
   ```

   `installDebug` alone is enough if assemble is implied by the install task; prefer both when reporting the APK path.
3. **Launch** — resolve debug package (`applicationId` + `applicationIdSuffix` if any) and launcher activity; start via `adb` (see common.md).
4. **Report** — serial, variant `debug`, APK path under `app/build/outputs/apk/debug/`, package, success/fail.

## Scope

- Debug variant only
- Device required
- No `signingConfigs` edits
- No release keystore / `local.properties` signing work

## Forbidden

- Switching to release or changing signing
- Claiming success without a `device`-state serial
- Inventing secrets (not needed for debug)
