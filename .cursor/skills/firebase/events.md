# Firebase Analytics events — shared

Used by `implement-firebase-events` and `add-firebase-events`. Follow `.cursor/rules/22-platform-firebase.mdc`, `12-naming-conventions.mdc`, `16-logging.mdc`.

## Placement

| App state                                                              | Action                                                                                                                                                             |
|------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Multi-module + no events yet                                           | `EventsProvider` in **`:core-common`**. `PlatformFirebase.postFirebaseEvent()` in **`:core-platform`**.                                                            |
| Already has `EventsProvider` / `FirebaseUtils` / other event constants | **Add into that structure.** Do **not** create a second provider. Do **not** move files to `:core-common` / `PlatformFirebase` without **explicit user approval**. |
| Single-module, no events yet                                           | One `EventsProvider` object next to existing helpers (same app source set). Ask before inventing a new module.                                                     |

## Naming

```kotlin
object EventsProvider {
    const val ENTRANCE_SCREEN = "ENTRANCE_SCREEN"
    const val LANGUAGE_SCREEN = "LANGUAGE_SCREEN"
    const val LANGUAGE_CONTINUE_BUTTON = "LANGUAGE_CONTINUE_BUTTON"
}
```

- Screen: `*_SCREEN` (constant name = event string)
- Button / tap: `*_BUTTON` or `*_CROSS` when that is the control (paywall close)
- First vs returning (Photo Collage): `SPLASH_FT` / `SPLASH_ST`, `HOME_FT` / `HOME_ST` — only when prefs have `isFirstTime` (or equivalent) **and** the user wants FT/ST
- No raw event strings in UI: `EventsProvider.HOME_SCREEN.postFirebaseEvent()`

## Where to fire

Match the **app’s existing** call sites if events already exist.

If greenfield (Qibla / Photo Collage):

- **Screen** — Fragment `onViewCreated` → private `postEvent()`
- **Button** — Fragment click / `handleEffect` (user-visible taps)
- **Not** `ParentFragment` auto-log
- **Not** ViewModel unless the app already posts from `onScreenStarted`

## Poster

Speak-Translate / `setup-new-project` shape (`PlatformFirebase` in `:core-platform`):

```kotlin
fun String.postFirebaseEvent() {
    val bundle = Bundle().apply {
        putString(FirebaseAnalytics.Param.ITEM_NAME, this@postFirebaseEvent)
    }
    Firebase.analytics.logEvent(this, bundle)
    Log.d(TAG_FIREBASE, "PlatformFirebase: postFirebaseEvent: Success: event=$this")
}
```

`PlatformFirebase` stays an `object` **without** a `Context` field. Do not add ads-revenue helpers in the events skill (`logRevenueEvent(context)` only when the app has ads — Context as an argument).

Log format: `ClassName: functionName: State: details`. Never log PII or tokens.
