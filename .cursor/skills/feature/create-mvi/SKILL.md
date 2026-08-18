---
name: create-mvi
description: Scaffold presentation-layer MVI only (Intent/State/Effect/ViewModel/Fragment/DI/nav). No domain or data. Use when adding a new screen with ViewModel under presentation. For new UseCases/repositories use create-clean-architecture. Do not use for ads / :gmaAds.
---

# Create MVI Feature (presentation only)

Follow `.cursor/rules/` (especially `00-global`, `01-feature-checklist`, `03-android-architecture`, `04-mvi-presentation`, `06-coroutines-flow`, `07-dependency-injection`, `17-navigation`, `18-errors-result`, `19-base-ui`).

Obey `.cursor/project-settings.json` when present (`writeTestsWithFeatures`, `orientation`, `themeModes`).

## Preconditions

- Confirm feature name (camelCase folder, e.g. `userProfile`)
- Confirm layouts exist (or run `figma-to-xml` / `create-dialog` / `create-bottom-sheet` first)
- Read existing similar feature for patterns (base Fragment, DI module naming, nav)
- If **new** domain capability is needed (new UseCase / repository / DataSource): run **`create-clean-architecture`** first (or after) — **do not** invent domain/data files inside this skill’s required path
- **Do not** use this skill for ads / `:gmaAds` / AdMob. Ads keep the project’s existing managers / ad ViewModels (`21-ads-billing`) unless the user **explicitly** asks to convert ads to MVI

## Package layout (`:presentation` only)

```
presentation/<featureName>/
  di/<Feature>PresentationModule.kt
  intent/<Feature>Intent.kt
  state/<Feature>State.kt
  effect/<Feature>Effect.kt
  viewModel/<Feature>ViewModel.kt
  ui/<Feature>Fragment.kt
  adapter/     # if list
  mapper/      # if domain → UI mapping
```

## Kotlin checklist

1. **Intent** — sealed class; UI dispatches only via `handleIntent`
2. **State** — data class, defaults, computed `show*` flags
3. **Effect** — navigation, permissions, `@StringRes` errors
4. **ViewModel** — follow `04-mvi-presentation` structure:
    - `handleIntent` = single `viewModelScope.launch(exceptionHandler) { when … }`
    - handlers = `private suspend fun onX()`
    - `exceptionHandler` → `handleError`; **`handleError` last** in the class
    - sparse logs (repo primary; ViewModel failures/`Log.w` guards only)
    - Inject **existing** UseCases / domain repos only — do not create new ones here
5. **Fragment** — extend `Parent*` / `Base*`; View Binding only; member order per `19-base-ui`: `onViewCreated` (`screenStarted` + **inline** `setOnClickListener` — no `setupClicks()`) → helpers → `initObservers` → `renderState` → `handleEffect` → teardown lifecycle (if any); `collectWhenStarted` / `collectWhenCreated` via **`viewLifecycleOwner`** (`FragmentExtensions`); navigate with `navigateTo` / `popFrom`
6. **Mapping** — heavy work in Repo/UseCase; `toUi()` in ViewModel with dispatcher if large lists
7. **Logs** — `Constants.TAG*` format; prefer Repository; ViewModel not every method
8. **Images** — adapters/Fragments bind with `siv.loadImage(...)` (Glide / `ImageViewExtensions`); never `setImageResource` for dynamic list/remote assets
9. **Orientation / themes** — layouts match `project-settings.json`

## Domain + data

**Out of scope for this skill.**

If the feature needs new UseCases, repository interfaces, DataSources, Retrofit/Room/prefs wiring:

→ Use skill **`create-clean-architecture`** (patterns in `26-data-persistence.mdc` + `rules/reference/`).

Existing UseCases may be injected into the new ViewModel.

## DI (presentation module only)

```kotlin
val featurePresentationModule = lazyModule {

    //// ViewModels
    viewModel { FeatureViewModel(get(), get()) }
}
```

Register in app composition root (`KoinModules`). Always `lazyModule` — never `module { }`. Use `//// Section` headers (`07-dependency-injection`).

Do **not** add `useCaseModule` / `dataModule` entries here — that belongs to `create-clean-architecture`.

## Navigation

- Add destination to `nav_*.xml`; Safe Args if needed — property name **`navArgs`** (`by navArgs()`), not `args`
- ViewModel emits navigation **Effect**; Fragment calls `NavController`
- Every `<action>` must use reference slide anims (`17-navigation`):

```xml
app:enterAnim="@anim/slide_in_right"
app:exitAnim="@anim/slide_out_left"
app:popEnterAnim="@anim/slide_in_left"
app:popExitAnim="@anim/slide_out_right"
```

## Strings

- All copy in single `:core-ui` `strings.xml` (+ locale folders)

## Tests

- If `writeTestsWithFeatures` is `true` (default): ViewModel test Intents → State + Effects (`runTest`); UseCase tests only if UseCases already exist and are being extended in presentation flow
- If `false`: skip tests unless user asks
- Prefer `test/test-unit` skill patterns

## Verify

- [ ] No `:presentation` → `:data` dependency
- [ ] No new domain/data files created by this skill
- [ ] Portrait + landscape (or per `project-settings.json`)
- [ ] ProGuard keeps `state`/`intent`/`effect`/`model` packages
- [ ] No business logic in Fragment beyond render + intent dispatch
- [ ] Fragment collectors use `viewLifecycleOwner`; nav via `navigateTo` / `popFrom`
- [ ] Fragment member order per `19-base-ui` (`onViewCreated` with inline clicks — no `setupClicks()` → helpers → `initObservers` → `renderState` → `handleEffect`)
- [ ] ViewModel: single `handleIntent` launch, `suspend` handlers, `handleError` last
- [ ] Icon buttons use `ButtonStyle.IconButton` when applicable
- [ ] `DiffUtil.ItemCallback`: simple `areItemsTheSame` / `areContentsTheSame` as one-liners (see `04-mvi-presentation`)
