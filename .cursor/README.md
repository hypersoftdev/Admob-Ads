# AndroidAI Cursor template (v1)

Company-grade **rules** + **skills** for Clean Architecture Android apps (XML + View Binding, MVI, Koin `lazyModule`).

Use this folder as the single source of truth while building the template. Share with the team by copying `.cursor/` into a project (or later syncing from a tagged template repo).

## Rules vs Skills

|      | **Rules** (`.cursor/rules/`)                   | **Skills** (`.cursor/skills/`)                             |
|------|------------------------------------------------|------------------------------------------------------------|
| Role | Company law — invariants, naming, architecture | Multi-step playbooks (+ templates)                         |
| When | Always-on or glob-matched while editing        | Invoked by name (`/` or agent pick) or trigger description |
| Size | Prefer clear invariants + BAD/GOOD             | Full workflows                                             |

**Long detail** lives under [`rules/reference/`](rules/reference/). `.mdc` rule files hold **short invariants + links** to those reference docs (so Agent context stays smaller). Do not delete reference files — edit them when rules change.

**Commands:** Prefer skills with `/` invoke. Do not grow a large `.cursor/commands/` tree unless needed.

## Project settings

After `setup-new-project` / `setup-old-project` (or when joining an app), settings live in:

**[`.cursor/project-settings.json`](project-settings.json)**

All feature/UI/test skills **must read and obey** this file when present:

| Key                      | Values                            | Meaning                                                     |
|--------------------------|-----------------------------------|-------------------------------------------------------------|
| `writeTestsWithFeatures` | `true` / `false`                  | Write unit/integration/E2E tests while scaffolding features |
| `orientation`            | `portrait` / `landscape` / `both` | Which orientations layouts must support                     |
| `themeModes`             | `day` / `night` / `both`          | Day / night / both theme resources                          |
| `applicationId`          | string                            | Root package / applicationId                                |
| `appName`                | string                            | Display name                                                |
| `figmaDesignSystemUrl`   | Figma `/design/` URL or omit/`""` | Optional; Figma file for `setup-design-system`              |

## Skill map

```
project/setup-new-project          Bootstrap multi-module app + persist settings (Firebase BOM + analytics/crashlytics/messaging + RC cache)
project/setup-old-project          Migrate existing production app to setup-new-project architecture; preserve product behavior; confirm settings first
project/setup-design-system        Figma design-system file → :core-ui tokens/themes (creates :core-ui if missing; theme-first windowBackground)
feature/create-mvi                 Presentation MVI only (no domain/data)
feature/create-clean-architecture  Domain + data + core pieces as needed
ui/figma-to-xml                    XML layouts (+ Figma design-to-code); absorbs freeform screen XML
ui/create-dialog                   Dialog XML (orchestrates figma-to-xml)
ui/create-bottom-sheet             Bottom sheet XML (orchestrates figma-to-xml)
ui/create-custom-view              Custom View / ViewGroup
review/review-architecture         Architecture / MVI / boundaries
review/review-performance          ANR / lists / dispatchers
review/review-security             Secrets / manifest / PII
review/review-complete             Runs all review-* + summary report
test/test-unit                     Write+run JVM unit/Flow tests; consent before fix
test/test-integration              Write+run multi-layer tests; consent before fix
test/test-e2e                      Write+run E2E on device; consent before fix
test/test-complete                 Full run + walkthrough; consent before fix
gradle/gradle-organize             Catalog + android/base/dependencies section order (signingConfigs, bundle)
gradle/gradle-update               Bump all deps (catalog + hardcodes); migrate to libs.versions.toml + sections
build/build-debug-apk              Debug APK → device install + launch
build/build-release-apk           Release signing + APK → device install + launch
build/build-release-bundle        Release signing + AAB (no device)
platform/implement-in-app-update   Play In-App Updates
platform/implement-in-app-review   Play In-App Review
platform/implement-firebase-messaging  firebase-messaging dep (:core-platform) only
firebase/implement-firebase-messaging  firebase-messaging dep (:core-platform) only
firebase/implement-firebase-remote-config  First-time RC + SharedPref cache + Entrance fetch
firebase/add-firebase-remote-config  Add RC keys to existing SharedPref + Remote Config classes
firebase/implement-firebase-events  First-time full-app Analytics (EventsProvider; screens/buttons)
firebase/add-firebase-events       Add Analytics events for selected screens
premium/implement-in-app-billing   Greenfield Play billing (subs + in-app, v4 stack)
premium/add-subscription-packages  Add subscription tiers to existing billing
premium/add-inapp-packages         Add one-time in-app products to existing billing
release/pre-release                Ship checklist
```

### Typical feature flow

1. `setup-new-project` (greenfield) or `setup-old-project` (existing production app)
2. `setup-design-system` — Figma tokens/themes in `:core-ui` (skip if user chose ignore-for-now)
3. `figma-to-xml` (or dialog / bottom-sheet) — XML only
4. `create-mvi` — presentation Intent/State/Effect/VM/Fragment
5. `create-clean-architecture` — when new domain/data is required

Data patterns (Retrofit, Room, SharedPreferences) live in **rules** + [`.cursor/rules/reference/`](rules/reference/) — not separate skills.

## Rules index (`00`–`27`)

| File                   | Role                                                                                                                                    |
|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `00-global`            | Always-on stack + Always/Never law                                                                                                      |
| `01-feature-checklist` | Feature scaffolding checklist (not always-on)                                                                                           |
| `02`–`03`              | Modules + Clean Architecture (+ SOLID in `03`)                                                                                          |
| `04`–`07`              | MVI, Kotlin, coroutines, DI (`04`/`07` → `reference/`)                                                                                  |
| `08`–`10`              | Gradle (`08` → `reference/gradle.md`: section order, signingConfigs, bundle, `base`), resources/XML, manifest (`09` → `reference/`)     |
| `11`–`13`              | Testing, naming, libraries                                                                                                              |
| `14`–`16`              | Security (always), compatibility, logging (always)                                                                                      |
| `17`–`20`              | Nav, errors, base UI (`19` → `reference/`), permissions                                                                                 |
| `21`–`25`              | Ads/billing (**ads are not MVI** — keep existing ads architecture unless the user asks), Firebase, startup, Figma assets, in-app update |
| `26-data-persistence`  | Retrofit / Room / SharedPreferences patterns                                                                                            |
| `27-in-app-review`     | Play In-App Review placement (`InAppReviewManager`)                                                                                     |

### `rules/reference/` (full detail)

| File                                                | Backed by rule            |
|-----------------------------------------------------|---------------------------|
| `resources-xml.md`                                  | `09-resources-xml`        |
| `dependency-injection.md`                           | `07-dependency-injection` |
| `gradle.md`                                         | `08-gradle`               |
| `mvi-presentation.md`                               | `04-mvi-presentation`     |
| `base-ui.md`                                        | `19-base-ui`              |
| `retrofit.md` / `room.md` / `shared-preferences.md` | `26-data-persistence`     |
| `premium-billing.md`                                | `21-ads-billing`          |

## Future distribution (not in v1)

**Option A:** Dedicated template repo + sync script that copies a tagged `.cursor/` into each app. VERSION / CHANGELOG / sync scripts come in a later pass — do not invent local divergent rules; update the template instead.

## How teammates use this

1. Open project with this `.cursor/` present.
2. Type `/` and pick a skill (e.g. `figma-to-xml`, `create-mvi`).
3. Obey rules automatically while editing matching files.
4. Before PRs: `/review-complete` or individual `review-*` skills.
