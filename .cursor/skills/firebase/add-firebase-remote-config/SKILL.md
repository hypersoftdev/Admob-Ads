---
name: add-firebase-remote-config
description: Add one or more Remote Config keys to an existing SharedPreferences cache. Asks the user for value/values (Firebase key, type, default), then matches this app’s SharedPref and Remote Config classes (DataSource or repositories). Use when extending RC cache keys, adding a flag to SharedPrefManager/saveValues, or /add-firebase-remote-config — not for first-time RC wiring (use implement-firebase-remote-config).
---

# Add Firebase Remote Config keys

Follow `.cursor/rules/22-platform-firebase.mdc`, `26-data-persistence.mdc`, `16-logging.mdc`.  
Prefs shape: [`.cursor/rules/reference/shared-preferences.md`](../../rules/reference/shared-preferences.md).  
First-time RC stack → **`implement-firebase-remote-config`**.

Obey `.cursor/project-settings.json` when present.

## Goal

Ask the user for value/values, then check the existing pattern and add each value in **both**:

1. **SharedPref classes** — DataSource (`SharedPrefManager`) and/or repositories, whichever this app already uses for RC cache
2. **Remote Config classes** — DataSource and/or repositories, whichever this app already uses to fetch/cache keys

Do **not** invent a new RC/prefs layout. Clone neighboring keys.

---

## Entry

| App state                                                                 | Action |
|---------------------------------------------------------------------------|--------|
| No RC DataSource / repository **and** no RC cache properties on prefs     | Stop → **`implement-firebase-remote-config`** |
| Legacy Application helper (`RemoteConfiguration`) only                    | Add the key into **that** helper + whatever prefs it already writes. Do not migrate without approval. |
| RC + prefs cache exist                                                    | Continue. Edit **existing** files only. |

---

## Step 0 — Ask for value/values (mandatory — no code until complete)

If the user already listed keys in the query, use those. Still fill gaps (type / default) before coding.

For each value collect:

| Field | Required | Example |
|-------|----------|---------|
| Firebase key string | Yes | `bannerHome` |
| Type | Yes | `Int` / `Boolean` / `String` |
| Default | Yes | `0`, `false`, `""` |

Optional: prefs property name (default `rc` + PascalCase, e.g. `bannerHome` → `rcBannerHome`).

**AskQuestion** when anything is missing:

- If only names are known → type (`Int` / `Boolean` / `String`)
- If several keys → `allow_multiple` only when listing keys the user already named; otherwise let them reply with a table

Wait until every key has name + type + default. **Do not invent extra keys.** Skip a key that already exists (same Firebase string or `rc*` property) and say so.

---

## Step 1 — Audit existing pattern

Search this app (do not assume Speak-Translate / Qibla names):

**SharedPref**

- `SharedPrefManager` / `SharedPrefDataSource` — RC key `val`s (`bannerDashboard = "bannerDashboard"`) + `var rc…` get/set
- `SharedPrefRepository` + `SharedPrefRepositoryImpl` — suspend getters that wrap `rc*` (only if this app already does that for RC cache)
- Fakes (`FakeSharedPrefRepository`) if the domain interface will change

**Remote Config**

- `RemoteConfigDataSource` — generic `getInt`/`getBoolean`/`getString` (Speak-Translate: **no** per-key methods) **or** named getters if this app has them
- `RemoteConfigRepositoryImpl.saveValues()` (or `RemoteConfiguration`) — `rcX = remoteConfigDataSource.getInt(keyVal)` + neighbor logs

Record: file paths, naming (`rc` prefix, key `val` vs `KEY_*`), getter used (`getInt` vs `getBoolean(key, default)`), whether domain SharedPref API exposes RC values.

---

## Step 2 — SharedPref classes

Match **this app**, on every SharedPref class that already holds RC cache:

**Manager / DataSource** (always, when that is where cache lives):

```kotlin
val bannerHome = "bannerHome"

var rcBannerHome: Int
    get() = sharedPreferences.getInt(bannerHome, 0)
    set(value) = sharedPreferences.edit { putInt(bannerHome, value) }
```

Use `getBoolean` / `getString` when Step 0 type is not Int. Put the new `val` + `var` next to the same-section neighbors (banner / inter / native / feature). Sync only — no dispatcher.

**Repository** (only if this app already exposes similar RC values on `SharedPrefRepository`):

```kotlin
override suspend fun remoteBannerHome(): Int = withContext(ioDispatcher) {
    sharedPrefManager.rcBannerHome
}
```

Match existing method style (`isXEnabled()` for `!= 0` ints vs returning the raw int). Update domain interface **and** impl **and** any fake.

Do **not** inject `SharedPrefManager` into presentation. Do **not** add a UseCase unless the user asked to read the flag from a screen.

---

## Step 3 — Remote Config classes

Match **this app**:

**DataSource** — add a per-key method **only** if this DataSource already has named key getters. Otherwise keep generic getters; pass the SharedPref key `val` into `getInt` / `getBoolean` / `getString`.

**Repository / helper** — add to `saveValues()` (same `apply` / try/catch / log style as neighbors):

```kotlin
rcBannerHome = remoteConfigDataSource.getInt(bannerHome)
```

Use `getBoolean(bannerHome, default)` / `getString(bannerHome, default)` when type is not Int. If neighbors log after write, log this key the same way (`TAG_REMOTE_CONFIG`).

Do not change fetch/activate, mutex, or listener registration.

---

## Step 4 — Verify

- [ ] Only the value/values the user gave
- [ ] SharedPref cache property + key string match this app’s naming
- [ ] RC `saveValues()` (or existing helper) writes the new key
- [ ] Domain SharedPref API / fakes updated **only** when this app already exposes RC there
- [ ] No new modules, catalog libs, or `remote_config_defaults.xml`
- [ ] Runtime reads stay on prefs cache — not RC SDK in Fragments

## Do not

- First-time RC stack (`implement-firebase-remote-config`)
- Analytics / `EventsProvider` (`add-firebase-events`)
- Copy ads/premium keys from another app
- Per-key methods on a generic `RemoteConfigDataSource`
- `SharedPreferences` in Fragment / ViewModel
- Dispatcher on `SharedPrefManager`
- `PlatformFirebase` changes
