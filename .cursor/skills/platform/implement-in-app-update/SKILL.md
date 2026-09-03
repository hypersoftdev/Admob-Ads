---
name: implement-in-app-update
description: Add Google Play In-App Updates (Immediate/Flexible) with InAppUpdateManager in core-ui. Use when the user asks to add in-app update, Play Core app update, flexible/immediate update, or /implement-in-app-update.
---

# Implement In-App Update

Follow `.cursor/rules/08-gradle.mdc`, `09-resources-xml.mdc`, `13-libraries-stack.mdc`, `16-logging.mdc`, `25-in-app-update.mdc`.  
**Requires human approval** before adding `app-update-ktx` if not already in the catalog.

## Goal

Wire Play In-App Updates:

- Manager in **`:core-ui`**
- Host in **`:presentation`** (Dashboard or post-Entrance main screen)
- Optional type from **RC → SharedPref cache**
- No hardcoded snackbar copy; logs use `Constants.TAG` format

## Step 1 — Dependency (latest stable)

1. Add to `libs.versions.toml` under **Google** section (`gradle-organize` headers)
2. `implementation(libs.app.update.ktx)` on **`:core-ui`** and host **`:presentation`** (if presentation needs the type constants)

```toml
# Google
appUpdateKtx = "…"   # latest stable
app-update-ktx = { group = "com.google.android.play", name = "app-update-ktx", version.ref = "appUpdateKtx" }
```

```kotlin
// Google
implementation(libs.app.update.ktx)
```

## Step 2 — Copy manager template

1. Read [templates/InAppUpdateManager.kt](templates/InAppUpdateManager.kt)
2. Place at:
   ```
   core-ui/.../core/ui/inAppUpdate/InAppUpdateManager.kt
   ```
3. Set package to `<applicationId>.core.ui.inAppUpdate`
4. Fix `Constants` import to project `:core-common`
5. Pass `@StringRes` ids for snackbar message + restart action (from Step 3)

**Critical:** Construct the manager as a **Fragment property** (init at construction) so `registerForActivityResult` runs **before STARTED**. Do **not** create it lazily inside `onViewCreated` after the Fragment is resumed.

```kotlin
private var updateManager: InAppUpdateManager? = InAppUpdateManager(
    fragment = this,
    updateDownloadedMessageRes = R.string.in_app_update_downloaded,
    restartActionRes = R.string.action_restart,
)
```

Clear in `onDestroyView`:

```kotlin
updateManager?.destroy()
updateManager = null
```

## Step 3 — Strings (`:core-ui` `strings.xml`)

Add under **General / Actions** (and keep section order):

```xml
<!-- Actions (if missing) -->
<string name="action_restart">Restart</string>

<!-- General / In-app update -->
<string name="in_app_update_downloaded">An update has just been downloaded.</string>
```

No hardcoded English in the manager.

## Step 4 — Host wiring (presentation)

Prefer: **Dashboard** `onViewCreated`, or after **Entrance → main**. Once per session (`isChecked` inside manager).

### MVI-friendly (preferred)

```kotlin
// Effect
data object StartInAppUpdateImmediate : FeatureEffect()
data object StartInAppUpdateFlexible : FeatureEffect()

// Fragment on Effect
is StartInAppUpdateImmediate -> startInAppUpdate(AppUpdateType.IMMEDIATE)
is StartInAppUpdateFlexible -> startInAppUpdate(AppUpdateType.FLEXIBLE)

private fun startInAppUpdate(@AppUpdateType type: Int) {
    val ctx = context ?: return
    updateManager?.apply {
        initManager(ctx)
        setUpdateType(type)
        checkForUpdate { available, message ->
            Log.d(TAG, "XFragment: startInAppUpdate: checkForUpdate: available=$available: $message")
            if (available) {
                requestForUpdate { updated, msg ->
                    Log.d(TAG, "XFragment: startInAppUpdate: requestForUpdate: updated=$updated: $msg")
                    destroyManager()
                }
            } else destroyManager()
        }
    }
}
```

### Simple (match reference)

Call check from host `onViewCreated` with optional short delay; default `AppUpdateType.IMMEDIATE`.

Offline / Play Store missing / no update → no-op (manager callbacks already cover this).

## Step 5 — Optional Remote Config policy

Cache in `SharedPrefManager` (sync data source; repo uses IO dispatcher):

| Pref / RC key | Meaning |
|---------------|---------|
| `0` | Off — skip |
| `1` | Flexible |
| `2` | Immediate (default) |

ViewModel/UseCase reads cache → emits Effect. If offline or `0` → skip.

`minimumFetchIntervalInSeconds(0)` still applies to RC fetch; runtime reads **prefs cache**.

## Step 6 — Verify

- [ ] `app-update-ktx` in catalog under Google + latest stable
- [ ] `InAppUpdateManager` in `:core-ui`
- [ ] Manager created as Fragment field (Activity Result before STARTED)
- [ ] `destroy()` in `onDestroyView`
- [ ] Snackbar strings from `:core-ui`
- [ ] Logs: `ClassName: functionName: State: details` with `Constants.TAG`
- [ ] Trigger once per session; no crash when Play unavailable
- [ ] Portrait + landscape host screen still OK

## Do not

- Put `AppUpdateManager` / Activity Result in ViewModel
- Hardcode snackbar / restart text
- Use `named("io")` for dispatchers
- Call update from every Fragment — one host per session is enough

## Template

Canonical source: [templates/InAppUpdateManager.kt](templates/InAppUpdateManager.kt)
