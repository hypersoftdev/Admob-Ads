---
name: implement-firebase-messaging
description: Add firebase-messaging dependency to core-platform via Version Catalog only. Use when the user asks to add FCM / Firebase Cloud Messaging dependency, firebase-messaging, or /implement-firebase-messaging — not for MessagingService or push UI.
---

# Implement Firebase Messaging (dependency only)

Follow `.cursor/rules/08-gradle.mdc`, `13-libraries-stack.mdc`, `02-project-structure.mdc`, `22-platform-firebase.mdc`.  
**Requires human approval** before adding `firebase-messaging` if not already in the catalog.

## Goal

Add **only** the Play/Firebase Messaging library:

- Catalog entry under **Firebase**
- `implementation` on **`:core-platform`** (app pulls it transitively)
- **Mandatory** on every **`setup-new-project`** bootstrap
- **No** `FirebaseMessagingService`, receivers, manifest entries, tokens, DI, or managers

## Step 1 — Catalog (latest stable)

Add to `libs.versions.toml` under **Firebase** section (`gradle-organize` headers):

```toml
# Firebase
firebaseMessaging = "…"   # latest stable
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging", version.ref = "firebaseMessaging" }
```

## Step 2 — Module dependency

On **`:core-platform`** `build.gradle.kts`, under **Firebase**:

```kotlin
// Firebase
implementation(libs.firebase.messaging)
```

Do **not** re-add the same dependency on `:app` unless a future feature needs compile-time FCM APIs there (`api` / duplicate `implementation` only with explicit need).

`:app` must still have `google-services` + `google-services.json` (existing Firebase setup) — this skill does not add those.

## Step 3 — Verify

- [ ] `firebase-messaging` in catalog under Firebase + latest stable
- [ ] `implementation(libs.firebase.messaging)` on `:core-platform` only
- [ ] No `FirebaseMessagingService` / custom receiver / FCM manifest / token code added
- [ ] No new DI modules or templates

## Do not

- Create or stub `FirebaseMessagingService`
- Edit AndroidManifest for FCM
- Add notification channels, icons, or deep-link handling
- Put the dependency only on `:app` when `:core-platform` exists (prefer `:core-platform`)
- Add unrelated Firebase libraries (Analytics, Crashlytics, etc.)
