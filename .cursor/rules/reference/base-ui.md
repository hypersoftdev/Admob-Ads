# Base UI classes, ViewBinding lifecycle, and Flow collection

Full detail for `19-base-ui.mdc`. Do not delete lines from this file — edit here and keep the rule stub in sync.

## Base hierarchy

```
:core-ui
  ParentActivity / ParentFragment     â† ViewBinding + lifecycle hooks
  ParentDialogDismissal â†’ ParentDialog
  ParentSheetDismissal  â†’ ParentSheet   â† BottomSheetDialogFragment

:presentation (optional product layer)
  BasePermissionFragment â†’ BaseFragment â†’ Feature Fragment
  BaseActivity / BaseDialog / BaseSheet
```

**Templates (copy + replace `YOUR.PACKAGE`):**  
`.cursor/skills/project/setup-new-project/templates/base/`  
See that folderâ€™s `README.md`. Same shape as the company template; `ParentSheet` is null-safe (not `!!`).

### ParentFragment
- `bindingFactory: (LayoutInflater) -> T`
- Clear `_binding` in `onDestroyView`; throw if accessed outside view lifecycle
- `initObservers()`, `onViewCreated(Bundle?)`, abstract `onViewCreated()`

### ParentActivity
- Edge-to-edge + insets padding flags (`includeTopPadding` / `includeBottomPadding` / `enableKeyboardInsets`)
- `installSplashTheme()` â†’ SplashScreen API
- `enableMaterialDynamicTheme()` â€” `DynamicColors.applyToActivityIfAvailable(this)`; no `GlobalContext` checks (Koin starts in Application before any Activity â€” see `07`, `23`)
- Abstract `onCreated()`; `onPreCreated()` before `super.onCreate`

### ParentDialog
- Dismissal base: `onDismissCallback` + `safeShow` / `safeDismiss`
- Dialog: `MaterialAlertDialogBuilder` + ViewBinding; null-safe binding; clear on destroy

### ParentSheet
- Dismissal base: `BottomSheetDialogFragment` + `dismissCallback` + `safeShow` / `safeDismiss`
- Sheet: ViewBinding via factory; **null-safe** `_binding` (same as Fragment â€” never `!!`)
- `onSheetCreated()`, optional `initObservers()`
- Prefer expanded/skip-collapsed helpers in `onStart` when needed â€” keep Binding lifecycle strict

### Base* (presentation)
- Thin wrappers over Parent* for shared product helpers
- Put ads/billing injects on `BaseFragment` / `BaseActivity` only when the app has those modules â€” keep Parent* free of ads
- Permission helpers live on `BasePermissionFragment`; dialog copy must use `:core-ui` strings before release

## Flow collection (`:core-ui` extensions)

- Fragment helpers: `templates/base/FragmentExtensions.kt` â†’ `â€¦core.ui.extensions.FragmentExtensions.kt`  
  Uses **`viewLifecycleOwner`** for `collectWhen*` / `repeatWhen` / `launchWhen*` (survives only with the view â€” avoids duplicate collectors after navigate away / back)  
  Safe nav: `navigateTo(fragmentId, action)` / `popFrom(fragmentId)` â€” only when `isAdded` and current destination matches
- Activity helpers: `templates/base/ActivityExtensions.kt` â†’ `â€¦core.ui.extensions.ActivityExtensions.kt`
- Context helpers: `templates/base/ContextExtensions.kt` â†’ `â€¦core.ui.extensions.ContextExtensions.kt`  
  `context?.showToast("â€¦")` / `context?.showToast(R.string.x)` â€” prefer `@StringRes` for user-facing copy
- Image helpers: `templates/base/ImageViewExtensions.kt` â†’ `â€¦core.ui.extensions.ImageViewExtensions.kt`  
  Always `siv.loadImage(source)` (Glide) for programmatic loads â€” never `setImageResource` / scattered `Glide.with` in UI
- State: `collectWhenStarted` / `repeatOnLifecycle(STARTED)`
- Effects: `collectWhenCreated` (or STARTED with SharedFlow that must not drop â€” follow project helper)
- Reuse extensions â€” do not copy-paste collectors
- Naming: `<Receiver>Extensions.kt` only â€” never a shared `FlowCollectionExtensions.kt` / `LifecycleFlowExtensions.kt`
- Prefer `navigateTo` / `popFrom` over raw `findNavController().navigate` / `popBackStack` in Fragments

## Feature Fragment responsibilities

- Extend `BaseFragment` (or `ParentFragment` if no Base layer yet)
- `initObservers()` / collect state + effects
- Dispatch Intents only â€” no business logic
- Permissions via Effects â†’ base helpers
- Navigate only in UI layer

## Feature Fragment member order (mandatory)

Keep class members in this order. Omit unused lifecycle overrides.

```kotlin
class FeatureFragment : BaseFragment<FragmentFeatureBinding>(FragmentFeatureBinding::inflate) {

    private val viewModel: FeatureViewModel by viewModel()
    // other properties / lazy adaptersâ€¦

    override fun onViewCreated() {
        screenStarted()

        // Click listeners inline here â€” do NOT extract a setupClicks() / setupListeners() helper
        binding.mbAction.setOnClickListener { viewModel.handleIntent(FeatureIntent.ActionClicked) }
    }

    // override fun onStart() { â€¦ }   // only if needed
    // override fun onResume() { â€¦ }  // only if needed

    private fun screenStarted() {
        viewModel.handleIntent(FeatureIntent.ScreenStarted)
    }

    override fun initObservers() {
        collectWhenStarted(viewModel.state, ::renderState)
        collectWhenCreated(viewModel.effect, ::handleEffect)
    }

    private fun renderState(state: FeatureState) { /* bind UI */ }

    private fun handleEffect(effect: FeatureEffect) { /* navigate / one-shots */ }

    // override fun onPause() { â€¦ }        // only if needed
    // override fun onStop() { â€¦ }         // only if needed
    // override fun onDestroyView() { â€¦ }  // only if needed (unregister listeners, then super)
}
```

1. Properties / ViewModels / adapters
2. `onViewCreated()` â€” `screenStarted()` (or other start intents) then **inline** `setOnClickListener` â†’ `handleIntent`; optional non-click UI helpers only when non-trivial
3. `onStart` / `onResume` â€” only when required
4. Private helper method implementations (`screenStarted`, camera/setup helpers, â€¦) â€” **never** a dedicated `setupClicks()` / `setupListeners()` that only wires clicks
5. `initObservers()` â€” `collectWhenStarted` / `collectWhenCreated`
6. `renderState` then `handleEffect`
7. `onPause` / `onStop` / `onDestroyView` â€” only when required (`onDestroyView` last among them)

## Forbidden

- Holding View / Binding / Fragment in ViewModel
- `findViewById` / Data Binding
- Skipping Parent* for ad-hoc Binding lifecycle
- Accessing dialog/sheet binding after `onDestroyView`
- Hardcoding ads SDK into Parent* classes
- Collecting on Fragment `lifecycleScope` instead of `viewLifecycleOwner`
- Shared `FlowCollectionExtensions.kt` â€” use `FragmentExtensions` + `ActivityExtensions`
- `setImageResource` / raw `Glide.with` for dynamic/list/remote images â€” use `loadImage`
- Extracting click wiring into `setupClicks()` / `setupListeners()` â€” keep `setOnClickListener` inline in `onViewCreated()`
