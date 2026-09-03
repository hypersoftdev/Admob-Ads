# MVI pattern — Intent, State, Effect, ViewModel

Full detail for `04-mvi-presentation.mdc`. Do not delete lines from this file — edit here and keep the rule stub in sync.

**Out of scope: ads.** This pattern is for **feature screens** in `:presentation`. Do not convert `:gmaAds` / AdMob managers / existing ad ViewModels to Intent / State / Effect unless the user **explicitly** asks. Keep the project’s existing ads architecture (`21-ads-billing`).

## Intent (user/system actions)

```kotlin
sealed class FeatureIntent {
    object LoadProfile : FeatureIntent()
    data class SaveClicked(val name: String) : FeatureIntent()
}
```

- UI dispatches intents via `viewModel.handleIntent(...)` only
- No business branching in Fragments beyond rendering

## State (continuous UI data)

```kotlin
data class FeatureState(
    val isLoading: Boolean = false,
    val title: String? = null,
) {
    val showTitle: Boolean = title != null
}
```

- Default values on all properties
- Derived flags as computed properties
- Update via `_state.update { it.copy(...) }`

## Effect (one-shot events)

```kotlin
sealed class FeatureEffect {
    object NavigateBack : FeatureEffect()
    data class ShowErrorRes(@StringRes val messageResId: Int) : FeatureEffect()
}
```

- Navigation, toasts, permission prompts, dialogs
- Prefer `@StringRes` for user-facing messages

## ViewModel structure (mandatory)

```kotlin
class FeatureViewModel(
    private val getFeatureUseCase: GetFeatureUseCase,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _state = MutableStateFlow(FeatureState())
    val state: StateFlow<FeatureState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<FeatureEffect>()
    val effect: SharedFlow<FeatureEffect> = _effect.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch { handleError(throwable) }
    }

    fun handleIntent(intent: FeatureIntent) = viewModelScope.launch(exceptionHandler) {
        when (intent) {
            FeatureIntent.ScreenStarted -> onScreenStarted()
            is FeatureIntent.ItemClicked -> onItemClicked(intent.id)
            FeatureIntent.ConfirmClicked -> onConfirmClicked()
        }
    }

    private suspend fun onScreenStarted() {
        EventsProvider.SCREEN_FEATURE.postFirebaseEvent()
        val data = getFeatureUseCase()
        val uiItems = withContext(defaultDispatcher) { FeatureUiMapper.toUi(data) }
        _state.update { it.copy(items = uiItems, isLoading = false) }
    }

    private suspend fun onItemClicked(id: String) {
        // update state / call use cases
    }

    private suspend fun onConfirmClicked() {
        _effect.emit(FeatureEffect.NavigateNext)
    }

    private fun handleError(throwable: Throwable) {
        val errorLog = "FeatureViewModel: handleError: Failed: ${throwable.message}"
        Log.e(TAG, errorLog)
        throwable.recordException(errorLog)
        _state.update { it.copy(isLoading = false) }
    }
}
```

### Structure rules

1. **Single launch at the edge** â€” `handleIntent` = `viewModelScope.launch(exceptionHandler) { when â€¦ }`
2. **Handlers are `suspend`** â€” `private suspend fun onX()`; do **not** nest another `viewModelScope.launch` per intent
3. **`exceptionHandler`** â†’ `viewModelScope.launch { handleError(throwable) }` (not inline logging/state updates in the handler lambda)
4. **`handleError` last** in the class â€” log + Crashlytics + clear loading / emit error Effect as needed
5. **No per-method `launch(exceptionHandler)`** inside each `onX`

### Logging in ViewModels

- Prefer **Repository** (and domain when useful) for flow logs â€” not every ViewModel step
- Do **not** log `Started` / `Success` on every intent handler
- ViewModel: **zero or one** meaningful log when helpful; always log failures in `handleError`
- `Log.w` for empty/guard paths when useful (e.g. confirm with no selection)
- If the screen has **no** repo/use-case path (UI-only logic), a short ViewModel log is OK

## Fragment / Activity / Adapter rules

- Collect state with lifecycle-aware collectors (`collectWhenStarted` on **`viewLifecycleOwner`** via `FragmentExtensions`)
- Collect effects separately from state (`collectWhenCreated`)
- Navigate with `navigateTo` / `popFrom` â€” not raw `findNavController()` when helpers exist
- Toasts: `context?.showToast(R.string.x)` / `context?.showToast("â€¦")` via `ContextExtensions` â€” prefer `@StringRes`
- Extend project `Parent*` / `Base*` Fragment classes when available
- View Binding only â€” never `findViewById` / Data Binding
- **Render only** â€” no DTO/domain mapping, filtering, or sorting in Fragment / Activity / Adapter
- Adapters bind pre-mapped UI models (`*UiItem`) only
- Log screen analytics via shared `EventsProvider` (or equivalent) when the project uses Firebase events
- **Fragment member order** (see `19-base-ui`): `onViewCreated` (`screenStarted` + **inline** clicks â€” no `setupClicks()`) â†’ `onStart`/`onResume` (if any) â†’ helper implementations â†’ `initObservers` â†’ `renderState` â†’ `handleEffect` â†’ `onPause`/`onStop`/`onDestroyView` (if any)

## Mapping (`toUi`)

- Prefer heavy mapping in **Repository** or **UseCase** (wherever the concern belongs)
- Domain â†’ UI (`FeatureUiMapper.toUi(...)`) may run in **ViewModel** when needed
- If mapping looks heavy (large lists), use a dispatcher (`withContext(defaultDispatcher)`) before updating State
- Never map inside Fragment, Activity, Adapter, or XML

## Lists and large data in UI

- Assume lists may be thousands of items â€” use `ListAdapter` + `DiffUtil`
- Map/filter/sort large collections in Repo / UseCase / ViewModel (off Main) before State / `submitList`
- Keep State lean; do not dump entire raw datasets into UI state when unnecessary
- Prefer pagination / windowed loading when the feature loads open-ended data
- **`DiffUtil.ItemCallback`:** keep simple `areItemsTheSame` / `areContentsTheSame` as **one-liners** (same line as `=`) â€” do not break the expression body onto the next line

```kotlin
// BAD
override fun areItemsTheSame(oldItem: ConversationUiItem, newItem: ConversationUiItem): Boolean =
    oldItem.id == newItem.id

// GOOD
override fun areItemsTheSame(oldItem: ConversationUiItem, newItem: ConversationUiItem): Boolean = oldItem.id == newItem.id
override fun areContentsTheSame(oldItem: ConversationUiItem, newItem: ConversationUiItem): Boolean = oldItem == newItem
```

- Same idea for other short single-expression overrides / properties â€” prefer one line when readable (~140 cols OK)

## Orientation

- Screens must work in **portrait and landscape** â€” layouts adapt; do not assume portrait-only
