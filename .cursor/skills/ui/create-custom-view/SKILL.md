---
name: create-custom-view
description: Create Android custom View or ViewGroup with XML layout and View Binding. Use when building custom UI controls, compound views, or reusable widgets beyond standard Material components.
---

# Create Custom View

Follow `.cursor/rules/09-resources-xml.mdc` + [reference/resources-xml.md](../../../rules/reference/resources-xml.md), `19-base-ui.mdc` + [reference/base-ui.md](../../../rules/reference/base-ui.md).

## When to use

- Reusable control used in multiple screens (chart, compass dial, custom slider)
- Compound view simpler than nested includes everywhere
- **Not** for one-off screen layout — use `ui/figma-to-xml`

## Structure

| Piece | Location |
|-------|----------|
| Layout | `:core-ui` if shared, else `:presentation` `layout/view_<name>.xml` |
| View class | matching module `ui/` or `core/ui/view/` |
| Attrs | `:core-ui` `res/values/attrs.xml` if configurable |

Naming: `view_<feature>_<purpose>.xml`, class `FeatureCustomView` or `CompassDialView`.

## Implementation

```kotlin
class FeatureCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewFeatureCustomBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun setTitle(text: String) {
        binding.mtvTitleCustom.text = text
    }
}
```

## Rules

Obey XML invariants in `09` + [reference/resources-xml.md](../../../rules/reference/resources-xml.md) and base UI in `19` + [reference/base-ui.md](../../../rules/reference/base-ui.md).

**Skill-specific (custom view):**

- Inflate via View Binding — not findViewById
- Material children inside custom view
- No business logic — expose setters/callbacks; ViewModel drives state via Fragment
- Custom drawing: `onDraw` work lightweight; heavy prep off Main
- Portrait + landscape — avoid fixed pixel sizes; use `dp` + constraints
- Strings via attrs or setter with `@StringRes` from Fragment — not hardcoded in view

## XML usage

```xml
<com.example.presentation.ui.FeatureCustomView
    android:id="@+id/cvFeatureWidget"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

## DI

- Custom views generally **not** in Koin — configure from Fragment with data from State

## Forbidden

- Context leaks (holding Activity beyond view lifecycle)
- Network/disk in custom view
- Data Binding
