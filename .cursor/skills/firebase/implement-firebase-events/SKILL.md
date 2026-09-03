---
name: implement-firebase-events
description: First-time Firebase Analytics events for the full app (EventsProvider + screen and/or button posts). Use when adding Analytics events app-wide, EventsProvider, postFirebaseEvent, or /implement-firebase-events — not for a few screens (use add-firebase-events) and not for Remote Config.
---

# Implement Firebase Events (full app, first time)

Follow `.cursor/rules/22-platform-firebase.mdc`, `12-naming-conventions.mdc`, `16-logging.mdc`, `08-gradle.mdc`, `00-global.mdc`.  
Shared event rules: [events.md](../events.md).  
Photo Collage `EventsProvider` catalog + Qibla Fragment call sites.

Obey `.cursor/project-settings.json` when present.

**Requires human approval** before adding `firebase-analytics` if it is not in the catalog.

For extra screens later → `add-firebase-events`.

## Entry

| App state                                                              | Action                                                                                |
|------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| `EventsProvider` + poster + most screens already instrumented          | Stop. Point user to `add-firebase-events`.                                            |
| Poster exists (`PlatformFirebase` / `FirebaseUtils`) but few/no events | Continue: fill provider + call sites. **Do not** replace the poster without approval. |
| No events                                                              | Continue.                                                                             |

---

## Step 0 — Ask before coding

### 0.1 Event kinds (mandatory)

**AskQuestion** (single choice):

- Screen events
- Button events
- Both screen and button events

Do not invent a fourth kind. Wait for the answer.

### 0.2 Structure (only if a different poster/constants file already exists)

If `FirebaseUtils` / `Events` / similar exists: **keep it**. Ask before creating `PlatformFirebase` or moving to `:core-common`.

### 0.3 FT / ST (optional)

If `isFirstTime` (or equivalent) exists: **AskQuestion** whether entrance/home should use `SPLASH_FT`/`SPLASH_ST` and `HOME_FT`/`HOME_ST` (Photo Collage) instead of a single `*_SCREEN`.

---

## Step 1 — Catalog

Latest stable, `# Firebase` / `// Firebase`:

```toml
firebaseAnalytics = "…"   # latest stable
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics", version.ref = "firebaseAnalytics" }
```

```kotlin
// Firebase
implementation(libs.firebase.analytics)   // :core-platform (or :app if single-module)
```

`:app` still needs `google-services` + `google-services.json`.

Ensure `Constants.TAG_FIREBASE` exists (`16-logging`).

---

## Step 2 — EventsProvider + poster

Multi-module greenfield:

1. Copy [templates/EventsProvider.kt](templates/EventsProvider.kt) → `:core-common` `…/core/common/` (or `…/core/common/constants/events/` if that package already exists)
2. Add `fun String.postFirebaseEvent()` to existing `PlatformFirebase` (`:core-platform`) using the [events.md](../events.md) poster (`Param.ITEM_NAME` + `Firebase.analytics`). Create the `object` only if missing — copy `setup-new-project` [templates/firebase/PlatformFirebase.kt](../../project/setup-new-project/templates/firebase/PlatformFirebase.kt) (`recordException` + poster + `getDeviceToken`). No `Context` field; no ads-revenue helper in this skill.

Single-module / existing helper: add constants + `postFirebaseEvent` **there**.

Discover every user-visible Fragment / dialog / sheet. Add constants for the kinds chosen in 0.1 (do not add button constants if the user picked screens only).

---

## Step 3 — Wire every screen

For each Fragment (Qibla / Photo Collage):

**Screen** (if chosen):

```kotlin
override fun onViewCreated() {
    postEvent()
    // existing UI…
}

private fun postEvent() {
    EventsProvider.HOME_SCREEN.postFirebaseEvent()
}
```

**Button** (if chosen): post on the actual click / continue / cross handler, not on every bind.

Entrance + first-time: if 0.3 is FT/ST, branch on the cached first-time flag.

Do **not** auto-log in `ParentFragment`.

---

## Step 4 — Verify

- [ ] One events file (no parallel constant objects)
- [ ] No raw event string literals in UI
- [ ] Only the kinds the user picked
- [ ] Poster has no `Context` on `PlatformFirebase` if that object is used
- [ ] All discovered screens covered (or listed as skipped with reason)

## Do not

- Remote Config (`implement-firebase-remote-config`)
- New modules / migrate folder layout without approval
- Second `EventsProvider`
- Flaky sleeps
- PII in event names or bundles
