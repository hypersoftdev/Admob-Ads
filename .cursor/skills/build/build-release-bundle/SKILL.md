---
name: build-release-bundle
description: Configure release signing and generate a release Android App Bundle (AAB). No device needed. Use when the user asks to build release bundle, generate aab, Play bundle, or /build-release-bundle.
---

# Build Release Bundle

## Start banner

First user-visible sentence when this skill runs (verbatim):

> We are going to configure release signing and generate a release App Bundle (AAB) — no device is required.

## Workflow (mandatory)

Follow shared steps in [common.md](../common.md).

1. **No device** — skip `adb` / install / launch entirely.
2. **Signing** — ensure release `signingConfigs`: find `.jks` / `.keystore` (project root → `app/`), or **ask the user for the path** if none. Fill credentials via `local.properties` and wire Gradle per common.md. Ensure `buildTypes.release` uses `signingConfigs.getByName("release")`.
3. **Build** (Windows: `.\gradlew.bat`):

   ```bash
   ./gradlew :app:bundleRelease
   ```

4. **On signing failure** — stop; list missing store file / passwords / alias. Do not invent values.
5. **Report** — variant `release`, AAB path under `app/build/outputs/bundle/release/` (respect `base.archivesName`), success/fail.

## Scope

- Release AAB only
- No device, install, or launch
- May edit `:app` `signingConfigs` wiring and `local.properties` (gitignored) per common.md

## Forbidden

- Running `adb`, `installRelease`, or launching the app
- Inventing keystore secrets
- Committing `local.properties`, `.jks`, or `.keystore`
- Claiming a Play upload (build only)
