---
name: implement-in-app-review
description: Add Google Play In-App Review with InAppReviewManager in core-ui. Use when the user asks to add in-app review, Play Core review, review-ktx, rate flow API, or /implement-in-app-review.
---

# Implement In-App Review

Follow `.cursor/rules/08-gradle.mdc`, `13-libraries-stack.mdc`, `16-logging.mdc`, `27-in-app-review.mdc`.  
**Requires human approval** before adding `review-ktx` if not already in the catalog.

## Goal

Wire Play In-App Review:

- Manager in **`:core-ui`**
- Host in **`:presentation`** (call site chosen by product — e.g. Share/success)
- Clean API: `initManager` → `launchReview` → `destroy`
- No rating dialog, prefs throttle, or Feedback screen in this skill
- Logs use `Constants.TAG` format

## Step 1 — Dependency (latest stable)

1. Add to `libs.versions.toml` under **Google** section (`gradle-organize` headers)
2. `implementation(libs.review.ktx)` on **`:core-ui`**

```toml
# Google
reviewKtx = "…"   # latest stable (e.g. 2.0.2)
review-ktx = { group = "com.google.android.play", name = "review-ktx", version.ref = "reviewKtx" }
```

```kotlin
// Google
implementation(libs.review.ktx)
```

## Step 2 — Copy manager template

1. Read [templates/InAppReviewManager.kt](templates/InAppReviewManager.kt)
2. Place at:
   ```
   core-ui/.../core/ui/inAppReview/InAppReviewManager.kt
   ```
3. Set package to `<applicationId>.core.ui.inAppReview`
4. Fix `Constants` import to project `:core-common`

```kotlin
private var reviewManager: InAppReviewManager? = InAppReviewManager(fragment = this)
```

Clear in `onDestroyView` (or after a one-shot call finishes):

```kotlin
reviewManager?.destroy()
reviewManager = null
```

## Step 3 — Host wiring (presentation)

### MVI-friendly (preferred)

```kotlin
// Effect
data object StartInAppReview : FeatureEffect()

// Fragment on Effect
is StartInAppReview -> startInAppReview()

private fun startInAppReview() {
    val ctx = context ?: return
    reviewManager?.apply {
        initManager(ctx)
        launchReview { launched, message ->
            Log.d(TAG, "XFragment: startInAppReview: launchReview: launched=$launched: $message")
            destroy()
        }
    }
}
```

### Simple

Call `initManager` + `launchReview` from the host when the product moment arrives (e.g. after share/save).

Offline / Play Store missing / quota → no-op via callback (`launched=false`); do not crash.

**API note:** `launched=true` means the Play flow Task completed — not that the user left a review (Play may skip the UI).

## Step 4 — Verify

- [ ] `review-ktx` in catalog under Google + latest stable
- [ ] `InAppReviewManager` in `:core-ui`
- [ ] Host calls `initManager` → `launchReview`; `destroy()` when done / `onDestroyView`
- [ ] Logs: `ClassName: functionName: State: details` with `Constants.TAG`
- [ ] No crash when Play unavailable
- [ ] No custom rating dialog / prefs throttle added by this skill

## Do not

- Put `ReviewManager` / `launchReviewFlow` in ViewModel
- Add rating dialogs, SharedPref counters, or Feedback navigation here (product-specific — out of scope)
- Use `named("io")` for dispatchers
- Assume Task success = user reviewed

## Template

Canonical source: [templates/InAppReviewManager.kt](templates/InAppReviewManager.kt)