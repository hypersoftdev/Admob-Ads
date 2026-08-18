# Parent* / Base* templates

Copy into the new app and replace `YOUR.PACKAGE` with the real applicationId root.

| File                                           | Target module   | Target package                |
|------------------------------------------------|-----------------|-------------------------------|
| `ParentActivity.kt`                            | `:core-ui`      | `…core.ui.base.activity`      |
| `ParentFragment.kt`                            | `:core-ui`      | `…core.ui.base.fragment`      |
| `ParentDialogDismissal.kt` + `ParentDialog.kt` | `:core-ui`      | `…core.ui.base.dialog`        |
| `ParentSheetDismissal.kt` + `ParentSheet.kt`   | `:core-ui`      | `…core.ui.base.sheet`         |
| `FragmentExtensions.kt`                        | `:core-ui`      | `…core.ui.extensions`         |
| `ActivityExtensions.kt`                        | `:core-ui`      | `…core.ui.extensions`         |
| `ContextExtensions.kt`                         | `:core-ui`      | `…core.ui.extensions`         |
| `ImageViewExtensions.kt`                       | `:core-ui`      | `…core.ui.extensions`         |
| `BaseActivity.kt`                              | `:presentation` | `…presentation.base.activity` |
| `BaseFragment.kt`                              | `:presentation` | `…presentation.base.fragment` |
| `BasePermissionFragment.kt`                    | `:presentation` | `…presentation.base.fragment` |
| `BaseDialog.kt` / `BaseSheet.kt`               | `:presentation` | `…presentation.base.sheets`   |

Hierarchy (company template):

```
core-ui:    ParentActivity / ParentFragment / ParentDialog* / ParentSheet*
presentation: BasePermissionFragment → BaseFragment → Feature
              BaseActivity / BaseDialog / BaseSheet
```

Notes:
- `ParentSheet` uses **null-safe** binding (avoid `!!` on binding)
- Theme: apply after `startKoin` in Application; no `GlobalContext` probes (`07`, `23`)
- `FragmentExtensions` collectors use **`viewLifecycleOwner`** (avoids duplicate collectors after navigate away / back)
- Prefer `navigateTo` / `popFrom` over raw `findNavController()` calls
- `ContextExtensions.showToast(String)` / `showToast(@StringRes)` — call as `context?.showToast(...)`
- `ImageViewExtensions.loadImage(...)` — Glide for all programmatic image loads (`siv.loadImage(...)`)
- Naming: `<Receiver>Extensions.kt` only — never a shared `FlowCollectionExtensions.kt` / `LifecycleFlowExtensions.kt`
- `BaseFragment` is intentionally thin — add ads/billing injects only when the product needs them
- Permission dialog copy must move to `:core-ui` `strings.xml` (`cd_*` / screen strings) before release
- Logging uses `Constants.TAG` — wire package to `:core-common`
