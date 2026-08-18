---
name: review-architecture
description: Review Android changes against Clean Architecture, MVI, module boundaries, and project rules. Use when reviewing PRs, refactors, or asking if code follows architecture standards. Prefer review-complete for a full multi-check pass.
---

# Architecture Review

Read `.cursor/rules/` and apply systematically. Output a structured report.

Obey `.cursor/project-settings.json` when judging orientation / tests.

## Checklist

### Module boundaries

- [ ] `presentation` does not import `:data`
- [ ] `domain` has no Android UI / presentation imports
- [ ] UseCases + repository **interfaces** only in `:domain` — never in `:data`
- [ ] No circular module dependencies
- [ ] All Koin modules use `lazyModule { }` (never `module { }`); load via `lazyModules` only
- [ ] Theme applied after `startKoin` in Application; no `GlobalContext.getOrNull()` gates; `startKoin` never in Activity
- [ ] Each `lazyModule` has readable `//// Section` headers (SoC: DataSources / Repositories / ViewModels / area UseCases, etc.)
- [ ] `dataModule` ordered: `//// DataSources` then `//// Repositories`
- [ ] UseCase factories in domain `useCaseModule` (grouped by area)
- [ ] New `lazyModule` registered in composition root (list also sectioned: Core / Data / Domain / Presentation / Ads)

### MVI

- [ ] Intent / State / Effect / ViewModel pattern on **feature screens** — skip `:gmaAds` / existing ad ViewModels / ads managers (not MVI unless the user asked to convert)
- [ ] `handleIntent` single launch + `suspend` `onX` handlers; `handleError` at end of ViewModel
- [ ] Navigation via Effects — not NavController in ViewModel
- [ ] No mutable state exposed publicly
- [ ] Fragments render + dispatch intents only
- [ ] Fragment member order (`19-base-ui`): `onViewCreated` (inline clicks, no `setupClicks`) → helpers → `initObservers` → `renderState` → `handleEffect` → teardown
- [ ] Collectors use `viewLifecycleOwner` (`FragmentExtensions`); nav via `navigateTo` / `popFrom`
- [ ] ViewModel logs sparse — repo primary; failures via `handleError`

### Mapping

- [ ] Heavy mapping in Repo / UseCase — not Fragment/Adapter
- [ ] `toUi()` in ViewModel only when needed, with dispatcher for large lists
- [ ] Adapters bind `*UiItem` only

### Threading / ANR (`06-coroutines-flow`, `review-performance`)

- [ ] No disk/network/heavy map on Main
- [ ] Injected dispatchers where project uses them
- [ ] Large lists: ListAdapter + DiffUtil

### UI (`09-resources-xml`, `19-base-ui`)

- [ ] View Binding only — no findViewById / Data Binding
- [ ] Material widgets; portrait + landscape (per project settings)
- [ ] Clickable icons → `ButtonStyle.IconButton` (`mb`, `app:icon`) — not clickable `siv`
- [ ] `MaterialButton` solid+stroke → tint/stroke/`cornerRadius` — not `bg_shape_*` + `background` override
- [ ] Filled/text `MaterialButton` → `wrap_content` height — no fixed height + inset 0dp hacks
- [ ] Clickable language/chip selectors → `MaterialButton` + Material bg + end `app:icon` — not MTV + `bg_shape_*` / `drawableEnd`
- [ ] Programmatic images via Glide `siv.loadImage(...)` — not `setImageResource` / raw Glide in adapters
- [ ] Strings in single `:core-ui` file
- [ ] Static `layoutManager` / orientations / `spanCount` in XML — not Kotlin unless dynamic

### Security / logging (`14-security-secrets`, `16-logging`)

- [ ] No secrets in code/commits
- [ ] `Constants.TAG*` log format
- [ ] No PII in logs

### Errors (`18-errors-result`)

- [ ] Typed failures — not raw exceptions in State
- [ ] CancellationException handled correctly

### Data patterns (`26-data-persistence`)

- [ ] Retrofit/Room/prefs follow reference patterns when present
- [ ] No DataSource dispatchers; repository owns `withContext`

## Report format

Number every actionable finding. Follow [fix-selection.md](../fix-selection.md) after the report.

```markdown
## Summary

One-line verdict: Pass / Pass with notes / Fail

## Fix list

1. [Critical] …
2. [Warning] …
3. [Suggestion] …

## Rules referenced

- rule files that applied
```

Severity: boundary violations and Main-thread heavy work = **Critical**.

After the report: **do not fix yet** — ask which numbers to fix per `fix-selection.md` (e.g. user replies `fix 1, 2, 4, 7`).
