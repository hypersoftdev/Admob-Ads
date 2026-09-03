# Setup Design System — extraction and files

Used by [SKILL.md](SKILL.md). XML naming, strings, Material widgets: `09-resources-xml` + `reference/resources-xml.md`. Library Gradle: `08-gradle` + `reference/gradle.md`. Do not duplicate those tables here.

## If `:core-ui` is missing — create it, then continue

Do this in the **same turn**. Match existing `:app` AGP / `compileSdk` style.

1. Version catalog: add `android-library` plugin if absent. Root `build.gradle.kts`: `alias(libs.plugins.android.library) apply false`.
2. `settings.gradle.kts`: `include(":core-ui")`.
3. `core-ui/build.gradle.kts`: library shape from `reference/gradle.md` (no `signingConfigs` / `bundle` / `base`). `namespace` = `{applicationId}.core.ui` when `applicationId` in `project-settings.json` is non-empty; else `{ :app namespace }.core.ui`. View Binding on. `minSdk` matches `:app`.
4. `core-ui/.gitignore` containing `/build` (`02-project-structure`).
5. `core-ui/src/main/AndroidManifest.xml` — empty `<manifest />` (library).
6. `:app` `implementation(project(":core-ui"))`.
7. If `:app` still has `res/values/` themes / strings / colors, **move** them into `:core-ui` (app keeps mipmap / xml backup only). Point the app manifest `android:theme` at the `:core-ui` `Theme.*`.
8. Do **not** add `:presentation`, `:domain`, `:data`, `:core-common`, `:core-platform`, Entrance, or `Parent*` from this skill.

Then write Figma tokens into that module.

## Figma reads (targeted)

Parse `figma.com/design/:fileKey/:name?node-id=A-B` → `fileKey`, `nodeId` `A:B`.

| Need | How |
|------|-----|
| Pages | `get_metadata` without `nodeId`, or `use_figma` listing `figma.root.children` |
| Primitive palette | `get_variable_defs` / `get_design_context` on Foundation **Colors** (not the whole file) |
| Semantic light/dark | Tokens page rows (`Light value` / `Dark value`), or `use_figma` on `_Color token base` instances |
| Type ramp | `figma.getLocalTextStyles()` — family, size, weight, lineHeight, letterSpacing |
| Spacing / radius | Foundation Spacing table + radius variables (document as inline `dp` comments; **no `dimens.xml`**) |
| Shadows | Effect styles → Material elevation only if useful; do not invent `dimens` |

Skip bulk `getLocalVariablesAsync` dumps of the entire file.

## `:core-ui` files

| File | Contents |
|------|----------|
| `values/colors.xml` | App `md_theme_*` → General primitives → semantic tokens. Section headers per `09`. |
| `values-night/colors.xml` | Semantic + `md_theme_*` only (when `themeModes` is `night` or `both`). Primitives stay in `values/`. |
| `values/attrs.xml` | Extra semantic attrs (`colorTextTertiary`, `colorTextLink`, …) if Figma has them |
| `values/themes.xml` | `Base.Theme.*` + `Theme.*` + `TextStyle.*` + `ButtonStyle.*` + `ShapeAppearance.App.*` |
| `values-night/themes.xml` | Dark `Base.Theme.*` (window/status bar light flags false) |
| `res/font/` | OFL font files for the Figma family + `font-family` XML. Keep license (`OFL.txt` next to the module, not in `res/font`). |
| `res/color/selector_*.xml` | Button/icon enabled/disabled |
| `values/strings.xml` | Only add missing `cd_*` / actions. Do not invent feature screens. |

All user-facing strings stay in the **one** `:core-ui` `strings.xml`.

## Theme items (required)

```xml
<item name="android:windowBackground">@color/md_theme_background</item>
<item name="android:colorBackground">@color/md_theme_background</item>
<item name="colorSurface">@color/md_theme_surface</item>
<item name="android:statusBarColor">@color/md_theme_surface</item>
<item name="android:navigationBarColor">@color/md_theme_surface</item>
```

Also map Figma → Material: `colorPrimary`, `colorOnPrimary`, containers, error, outline, `colorSurfaceContainer*`, `fontFamily`, `materialButtonStyle` → `ButtonStyle.Primary`, `materialButtonOutlinedStyle` → `ButtonStyle.Outline`, `borderlessButtonStyle` → `ButtonStyle.Ghost`, `textAppearanceHeadline*` / `Title*` / `Body*` / `Label*` → `TextStyle.*`.

Night: `android:windowLightStatusBar` / `windowLightNavigationBar` false (`tools:targetApi="27"` on nav bar).

Do not set unknown Material attrs that fail resource linking (verify `assembleDebug`).

## Type and buttons

- `TextStyle.Heading.H1` … `H3` (+ Medium/Semibold/Bold); `TextStyle.Title.T1` … `T3`; `TextStyle.Body.B1` … `B3`. Sizes from Figma; prefer 4sp multiples when rounding.
- `ButtonStyle.Primary` / `Secondary` / `Tonal` / `Outline` / `Ghost` / `IconButton` (`Widget.Material3.Button.IconButton`, `iconSize` `0dp`). Height `wrap_content`; fill/stroke via tint/stroke/cornerRadius — no `bg_shape_*` for that.
- `ShapeAppearance.App.Small` / `Medium` / `Large` from Figma radius tokens (typical 8 / 16 / 24).

## Layouts

Default roots inherit `windowBackground`. **Forbidden** on default screens:

```xml
android:background="?attr/colorSurface"
```

Set `android:background` only for a **different** surface (card, overlay, tinted band). Text/icons still use `?attr/colorOnSurface` / `?attr/colorTextTertiary` / etc.

## Out of scope

Full icon library, every Figma component frame, product screens, `dimens.xml`, modules other than `:core-ui`.
