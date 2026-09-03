---
name: add-firebase-events
description: Add Firebase Analytics events for one or more existing screens (asks which screens and screen vs button vs both). Use when extending EventsProvider, adding events to selected Fragments, or /add-firebase-events — not for first-time full-app wiring (use implement-firebase-events).
---

# Add Firebase Events (selected screens)

Follow `.cursor/rules/22-platform-firebase.mdc`, `12-naming-conventions.mdc`, `16-logging.mdc`.  
Shared event rules: [events.md](../events.md).

Obey `.cursor/project-settings.json` when present.

For first-time full-app events → `implement-firebase-events`.

## Entry

| App state                                                      | Action                                                                                                                               |
|----------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| No `EventsProvider` (or equivalent) and no `postFirebaseEvent` | Stop → **`implement-firebase-events`**                                                                                               |
| Provider + poster exist                                        | Continue. Add into **existing** files. Do **not** migrate to `:core-common` / `PlatformFirebase` without **explicit user approval**. |

---

## Step 0 — Ask before coding

### 0.1 Which screens (mandatory)

List user-visible Fragments / dialogs / sheets that **lack** the events being added.

**AskQuestion** (`allow_multiple`): the user picks one or more screens. Wait. Do not instrument screens they did not pick.

### 0.2 Event kinds (mandatory)

**AskQuestion** (single choice):

- Screen events
- Button events
- Both screen and button events

Wait. Do not add the other kind.

### 0.3 FT / ST

Only if a picked screen is entrance/home **and** `isFirstTime` exists **and** the existing provider already uses `*_FT`/`*_ST`: match that. Otherwise use `*_SCREEN` unless the user asks for FT/ST.

---

## Step 1 — Constants

Add SCREAMING_SNAKE constants to the **existing** `EventsProvider` (same string as the name). Match neighboring names (`LANGUAGE_CONTINUE_BUTTON`, `PREMIUM_WEEKLY_CROSS`, …).

Do not create a second events file.

---

## Step 2 — Call sites

Same as [events.md](../events.md): Fragment `postEvent()` for screens; click / `handleEffect` for buttons. Follow this app’s existing posts if they already fire from ViewModel.

```kotlin
EventsProvider.SETTING_SCREEN.postFirebaseEvent()
EventsProvider.SETTING_RATE_US_BUTTON.postFirebaseEvent()
```

---

## Step 3 — Verify

- [ ] Only the screens the user picked
- [ ] Only the event kinds the user picked
- [ ] No raw event strings in UI
- [ ] No new Analytics library unless poster was missing (then stop and send user to `implement-firebase-events`)

## Do not

- Full-app sweep (that is `implement-firebase-events`)
- Remote Config (`add-firebase-remote-config`)
- Replace `FirebaseUtils` with `PlatformFirebase` without approval
- Weak assertions / unused dummy events
- Log PII
