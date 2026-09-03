# XML layouts, drawables, strings, colors, and themes

Full detail for `09-resources-xml.mdc`. Do not delete lines from this file — edit here and keep the rule stub in sync.

## Layout naming (snake_case)

| Prefix | Usage |
|--------|-------|
| `fragment_` | Screen layouts |
| `activity_` | Activities |
| `item_` | RecyclerView rows |
| `dialog_` | Dialogs |
| `bottom_sheet_` | Bottom sheets |
| `layout_` | Reusable include blocks |

Example: `fragment_home.xml`, `item_language.xml`

## View ID naming (prefix + camelCase)

| Prefix | Widget |
|--------|--------|
| `mb` | MaterialButton |
| `mtv` | MaterialTextView |
| `siv` | ShapeableImageView |
| `mcv` | MaterialCardView |
| `cl` | ConstraintLayout |
| `rcv` | RecyclerView |
| `fcv` | FragmentContainerView |
| `bnv` | BottomNavigationView |
| `cpi` | CircularProgressIndicator |
| `lpi` | LinearProgressIndicator |

Pattern: `{prefix}{Role}{Context}` â†’ `mtvHeadingHome`, `sivLogoEntrance`, `mbContinueLogin`

## Drawable naming

| Prefix | Usage |
|--------|-------|
| `ic_svg_` | Vector icons |
| `ic_png_` | Raster icons |
| `bg_shape_` | Shape / selector backgrounds (XML shapes) |
| `bg_svg_` | Backgrounds imported as SVG/vector |
| `bg_` | Other backgrounds / selectors (when not shape/svg) |
| `fg_` | Foreground overlays / gradients |
| `img_svg_` / `img_png_` | Large illustrations |
| `flag_` | Language / country flags when used |

Examples: `bg_shape_rounded_primary.xml`, `bg_svg_onboarding_header.xml`, `fg_gradient_header.xml`

## Fonts and motion

- Fonts in shared UI module (`res/font/`) + `preloaded_fonts.xml` when using downloadable/preloaded fonts
- Transitions: `res/anim/` (and `anim-ldrtl/` for RTL flips)
- Nav transitions: mandatory `slide_in_right` / `slide_out_left` / `slide_in_left` / `slide_out_right` on actions â€” see `17-navigation`
- Night-specific assets: `drawable-night/`, `values-night/` â€” avoid duplicating full layouts for dark mode

## Spacing & text sizes (no dimens.xml)

- **Never create `dimens.xml`** (or other dimens resource files)
- Use inline `dp` / `sp` in layouts and styles
- All sizes must be **multiples of 4**: `4dp`, `8dp`, `12dp`, `16dp`, `20dp`, `24dp`, `32dp`, `40dp`, `48dp`, â€¦
- Text: prefer multiples of 4 (`12sp`, `14sp` only if design requires; prefer `12sp`/`16sp`/`20sp`/`24sp`/`28sp`)
- Prefer shared `TextStyle.*` / `ButtonStyle.*` over one-off sizes when possible

```xml
<!-- âœ… GOOD -->
android:layout_margin="16dp"
android:padding="8dp"
android:textSize="16sp"

<!-- âŒ BAD -->
android:layout_margin="@dimen/margin_default"
android:padding="10dp"
android:textSize="15sp"
```

## Resource file sections (mandatory order)

In `:core-ui` `strings.xml`, `colors.xml`, and `themes.xml`, organize with **XML comment section headers**. Always order:

1. **App-level** (top) â€” identity, Material theme tokens, base app theme
2. **General / common** â€” actions, toasts, dialogs, **content descriptions**, text/button styles, semantic colors
3. **Screen-wise** (bottom) â€” per feature/screen blocks

```xml
<!-- _____________________________ App ______________________________ -->
<!-- _____________________________ Actions ______________________________ -->
<!-- ________________________ Content Descriptions ________________________ -->
<!-- _______________________________ Home _______________________________ -->
```

When adding a string/color/style: put it in the correct section â€” do not append at the file end out of order. Mirror order in `values-night/` and `values-xx/` locale files.

## Strings (single source of truth)

- **All** user-facing strings live in **one** shared module file (`:core-ui` `values/strings.xml`)
- No hardcoded user-facing text in layouts or Kotlin
- Non-translatable config: `translatable="false"`

### Strings section order

| Order | Section | Examples |
|-------|---------|----------|
| 1 | App | `app_name` |
| 2 | General â€” Ads / Toast / Actions / Dialogs / Bottom sheets | `toast_*`, `action_*` |
| 3 | **Content Descriptions** (dedicated) | `cd_*` |
| 4 | Screen-wise UI | `<!-- Entrance -->`, `<!-- Home -->`, â€¦ |

Naming: `app_name`; `toast_*` / `action_*`; `cd_{what}`; `{screen}_title` / `{feature}_*`

### Content descriptions (mandatory section)

```xml
<!-- ________________________ Content Descriptions ________________________ -->
<string name="cd_back">Back</string>
<string name="cd_app_logo">App logo</string>
```

- Always set `android:contentDescription="@string/cd_â€¦"` on `ShapeableImageView`, icon `MaterialButton`s, and meaningful icons
- Decorative only: `android:contentDescription="@null"` + `android:importantForAccessibility="no"`
- Never hardcode contentDescription in XML/Kotlin

## Colors

File: `:core-ui` `values/colors.xml` (+ `values-night/colors.xml` when needed).

| Order | Section | Examples |
|-------|---------|----------|
| 1 | App â€” Material 3 tokens | `md_theme_primary`, â€¦ |
| 2 | General / common | `colorPrimary`, `colorTextBody`, â€¦ |
| 3 | Screen / feature-wise | `premiumGradientStart`, â€¦ |

- Prefer `?attr/colorOnSurface` (and other theme color attrs) for **text / icons**
- **Default screen fill belongs on the theme**, not the layout:
  - Set `android:windowBackground` (and `android:colorBackground` / `colorSurface`) in `:core-ui` `themes.xml` / `values-night/themes.xml`
  - Do **not** set `android:background="?attr/colorSurface"` (or `@color/md_theme_surface`) on default screen roots — the window already paints that color
  - Set `android:background` on a view **only** when that region is a **different** surface (card, overlay, tinted header)
- Light/dark via `values` + `values-night`

```xml
<!-- BAD — default root repeating the window color -->
<androidx.constraintlayout.widget.ConstraintLayout
    android:id="@+id/clRootHome"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?attr/colorSurface">

<!-- GOOD — inherit windowBackground; tint only a different region -->
<androidx.constraintlayout.widget.ConstraintLayout
    android:id="@+id/clRootHome"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.card.MaterialCardView
        android:id="@+id/mcvBannerHome"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:cardBackgroundColor="?attr/colorPrimaryContainer"
        … />
```

## Themes

| File | Contents |
|------|----------|
| `themes.xml` | App base theme + common widget styles |
| `splash.xml` | `Theme.App.Starting` only (`23-app-startup`) |

Order in `themes.xml`: App theme â†’ TextStyle/ButtonStyle (incl. `ButtonStyle.IconButton`) â†’ screen-specific styles only if needed.

Default window fill: `android:windowBackground` (+ `android:colorBackground` / `colorSurface`) on the app theme — see Colors above. Figma design-system import: `setup-design-system`.

## Orientation (mandatory)

- Support **portrait and landscape**
- ConstraintLayout / `layout-land/` when needed
- Do not lock orientation unless product requires it

## Layout rules

- Material widgets: `MaterialTextView`, `MaterialButton`, `MaterialCardView`, `ShapeableImageView`
- **Images (non-clickable):** always `ShapeableImageView` â€” never `ImageView` / `AppCompatImageView`; IDs use `siv` prefix
- **Programmatic image loads:** always Glide via `ImageView.loadImage(...)` (`:core-ui` `ImageViewExtensions`) â€” never `setImageResource` / `setImageBitmap` / raw `Glide.with` in adapters/Fragments for content images
  - Template: `setup-new-project/templates/base/ImageViewExtensions.kt`
  - Supports `@DrawableRes`, URL `String`, `Uri`, etc.; optional `placeholder` / `error` / `CachePolicy`
  - Static icons declared in XML with `app:srcCompat` / `app:icon` are fine; dynamic/list/remote/drawable-from-code â†’ `loadImage`
- **Clickable icons / icon-only actions:** always `MaterialButton` with `ButtonStyle.IconButton` â€” **not** `ShapeableImageView` + click listener
  - Project style: `style="@style/ButtonStyle.IconButton"` (parent `Widget.Material3.Button.IconButton` in `:core-ui` `themes.xml`)
  - Else fallback: `style="@style/Widget.Material3.Button.IconButton"`
  - ID prefix `mbâ€¦`; use `app:icon` (not `app:srcCompat`); keep `android:contentDescription="@string/cd_â€¦"`
  - Typical attrs: `android:padding="4dp"`, `app:iconSize` (`0dp`); set `app:iconTint` only when the icon should follow theme color
- **MaterialButton fill + stroke (glass / outlined / tinted surfaces):** set attrs **on the button** â€” **do not** create `bg_shape_*` oval/rect drawables just for solid + stroke
  - Fill: `app:backgroundTint`
  - Stroke: `app:strokeColor` + `app:strokeWidth`
  - Shape: `app:cornerRadius` (circle â‰ˆ half of width/height) or `app:shapeAppearance`
  - **Forbidden for this case:** `android:background="@drawable/bg_shape_â€¦"`, `app:backgroundTint="@null"`, and `insetTop`/`insetBottom` `0dp` only to force a custom drawable to fit
  - Reserve `bg_shape_*` for non-button views (roots, cards wrappers, RecyclerView items) or gradients / selectors Material attrs cannot express
- **MaterialButton height (filled / tonal / outlined text buttons):** use `android:layout_height="wrap_content"` â€” **do not** force a fixed height (e.g. `40dp`) with `android:insetTop` / `android:insetBottom` `0dp` to fake it
  - Let Material min height + padding/`textAppearance` define size; override via theme/`ButtonStyle.*` if product needs a shorter button globally
  - Icon-only `ButtonStyle.IconButton` stays `wrap_content` unless design needs a fixed tap target (still use tint/stroke/cornerRadius â€” no shape drawable)
- **Clickable chips / language selectors (text + trailing arrow):** use **`MaterialButton`** â€” **not** `MaterialTextView` + `drawableEnd`, and **not** `LinearLayout` + text + `ShapeableImageView`
  - ID prefix `mbâ€¦`; `android:layout_height="wrap_content"`; no `insetTop`/`insetBottom` hacks
  - Icon on the **right**: `app:icon` + `app:iconGravity="end"` (+ `app:iconPadding` as needed)
  - Surface via Material style + `backgroundTint` / stroke attrs â€” **do not** set `android:background="@drawable/bg_shape_â€¦"`
  - Non-clickable text + decorative icon only: compound drawables on `MaterialTextView` are OK; interactive selectors must be buttons
- **View Binding only** â€” never `findViewById`, never Data Binding
- Shallow ConstraintLayout trees â€” avoid extra wrappers
- **Static layout config in XML** â€” not Kotlin â€” unless it must change at runtime:
  - `RecyclerView`: set `app:layoutManager`, `android:orientation`, `app:spanCount` (grids) in XML
  - `LinearLayout` / `LinearLayoutCompat`: `android:orientation` in XML
  - Do **not** call `layoutManager = LinearLayoutManager(...)` / `setOrientation(...)` in Fragment/Activity for fixed lists
  - Code only when dynamic (e.g. swap horizontalâ†”vertical or span count from state/config)
- RecyclerView: `ListAdapter` + `DiffUtil`; declare manager in XML as above
- Inline `dp`/`sp` multiples of 4 â€” **no `dimens.xml`**
- **XML closing / EOF formatting (layouts):**
  - Put a **blank line between nested container closing tags** (e.g. after `</ConstraintLayout>` before `</MaterialCardView>`, and before the root close)
  - End the file on the **root closing tag** â€” **no** extra blank line after `</Root>` (single trailing newline for EOF only)
  - Do **not** jam nested closes back-to-back without the blank line between major containers

```xml
<!-- BAD â€” no blank between nested closes; extra blank after root -->
            </LinearLayout>

        </androidx.constraintlayout.widget.ConstraintLayout>
    </com.google.android.material.card.MaterialCardView>

</androidx.constraintlayout.widget.ConstraintLayout>

<!-- GOOD â€” blank line between nested container closes; no blank after root -->
            </LinearLayout>

        </androidx.constraintlayout.widget.ConstraintLayout>

    </com.google.android.material.card.MaterialCardView>

</androidx.constraintlayout.widget.ConstraintLayout>
```

### RecyclerView XML example

```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rcvLanguages"
    android:layout_width="0dp"
    android:layout_height="0dp"
    android:orientation="vertical"
    android:clipToPadding="false"
    app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
    tools:listitem="@layout/item_language" />

<!-- Grid -->
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rcvCompassStyles"
    android:layout_width="0dp"
    android:layout_height="0dp"
    app:layoutManager="androidx.recyclerview.widget.GridLayoutManager"
    app:spanCount="2"
    tools:listitem="@layout/item_compass_style" />
```


### Icon button conversion example

```xml
<!-- BAD â€” clickable image pretending to be a button -->
<com.google.android.material.imageview.ShapeableImageView
    android:id="@+id/sivMenuHome"
    android:layout_width="40dp"
    android:layout_height="40dp"
    android:contentDescription="@string/cd_menu"
    android:scaleType="centerInside"
    app:srcCompat="@drawable/ic_svg_menu_home" />

<!-- GOOD â€” ButtonStyle.IconButton -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbMenuHome"
    style="@style/ButtonStyle.IconButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:contentDescription="@string/cd_menu"
    app:icon="@drawable/ic_svg_menu_home"
    app:layout_constraintBottom_toBottomOf="@id/mtvTitleHome"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="@id/mtvTitleHome" />
```

If `ButtonStyle.IconButton` is missing, add it under General button styles in `:core-ui` `themes.xml` (parent `Widget.Material3.Button.IconButton`). Use `app:iconSize` as `0dp`, and use `app:iconTint` as `@null` when the drawable should keep its own colors.

### MaterialButton solid + stroke (no shape drawable)

```xml
<!-- BAD â€” extra oval drawable + background override + inset hacks -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbCloseOcr"
    style="@style/ButtonStyle.IconButton"
    android:layout_width="44dp"
    android:layout_height="44dp"
    android:background="@drawable/bg_shape_ocr_glass_circle"
    android:insetTop="0dp"
    android:insetBottom="0dp"
    android:padding="4dp"
    app:backgroundTint="@null"
    app:icon="@drawable/ic_svg_back"
    app:iconTint="@color/white"
    â€¦ />

<!-- BAD drawable â€” do not create for button solid+stroke -->
<!-- bg_shape_ocr_glass_circle.xml: oval + solid + stroke -->

<!-- GOOD â€” tint + stroke + cornerRadius on the button -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbCloseOcr"
    style="@style/ButtonStyle.IconButton"
    android:layout_width="44dp"
    android:layout_height="44dp"
    android:layout_marginStart="16dp"
    android:layout_marginTop="16dp"
    android:contentDescription="@string/cd_ocr_close"
    android:padding="4dp"
    app:backgroundTint="@color/color_ocr_glass"
    app:cornerRadius="22dp"
    app:icon="@drawable/ic_svg_back"
    app:iconTint="@color/white"
    app:strokeColor="@color/color_ocr_glass_stroke"
    app:strokeWidth="1dp"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

### MaterialButton height (filled / text actions)

```xml
<!-- BAD â€” fixed height + inset hacks -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbTranslateTranslate"
    style="@style/Widget.Material3.Button"
    android:layout_width="0dp"
    android:layout_height="40dp"
    android:insetTop="0dp"
    android:insetBottom="0dp"
    android:text="@string/translate_action_translate"
    â€¦ />

<!-- GOOD â€” wrap_content, no insets -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbTranslateTranslate"
    style="@style/Widget.Material3.Button"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:text="@string/translate_action_translate"
    android:textAppearance="@style/TextStyle.Heading.H6.Medium"
    â€¦ />
```

### Clickable language / chip selectors (text + end icon)

```xml
<!-- BAD â€” MaterialTextView + custom chip background + drawableEnd -->
<com.google.android.material.textview.MaterialTextView
    android:id="@+id/mtvFromLanguageTranslate"
    style="@style/TextStyle.Heading.H6.Medium"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:background="@drawable/bg_shape_translate_lang_chip"
    android:drawableEnd="@drawable/ic_svg_translate_arrow_down"
    android:ellipsize="end"
    android:maxLines="1"
    android:padding="12dp"
    android:textColor="?attr/colorOnSurface"
    app:layout_constraintHorizontal_weight="1"
    tools:text="English" />

<!-- ALSO BAD â€” LinearLayout + TextView + ImageView -->
<!-- Prefer neither wrapper nor MTV chip for tappable selectors -->

<!-- GOOD â€” MaterialButton + Material surface + icon end (no bg_shape_*) -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbFromLanguageTranslate"
    style="@style/Widget.Material3.Button.OutlinedButton"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:contentDescription="@string/cd_source_language"
    android:ellipsize="end"
    android:maxLines="1"
    android:textAppearance="@style/TextStyle.Heading.H6.Medium"
    app:icon="@drawable/ic_svg_translate_arrow_down"
    app:iconGravity="end"
    app:iconPadding="8dp"
    app:layout_constraintHorizontal_weight="1"
    tools:text="English" />
```

Use project `ButtonStyle.*` when available instead of raw `Widget.Material3.*`. Do **not** assign `android:background="@drawable/bg_shape_â€¦"` â€” delete those chip backgrounds for button selectors.
