# Dependency injection conventions

Full detail for `07-dependency-injection.mdc`. Do not delete lines from this file — edit here and keep the rule stub in sync.

## Framework

Use the DI framework already in the project. Default stack: **Koin**.

- Bootstrap in Application class â€” keep Application minimal
- Aggregate modules in a single composition root (e.g. `KoinModules`)
- Prefer constructor injection and `by inject()` / `by viewModel()` over service locators
- If the project already has a limited `DIComponent` / `KoinComponent` faÃ§ade on base Fragments, follow it â€” do not expand it for new features without need

## Mandatory: `lazyModule` everywhere

- **Every** Koin module is declared with `lazyModule { }` â€” never `module { }`
- Application loads them via `lazyModules(...)` â€” never `modules(...)` for these definitions
- Applies to `:app`, `:data`, `:domain`, `:presentation`, `:core-*`, ads, features
- Do **not** mix `module` + `lazyModule` in the same composition root

### Migrating an existing project

When converting from eager Koin modules:

1. Replace every `module { }` â†’ `lazyModule { }` (all layers)
2. Replace `modules(â€¦)` â†’ `lazyModules(â€¦)` in `startKoin`
3. Keep `//// Section` headers while converting
4. Register the same list in `KoinModules` â€” do not drop modules mid-migration
5. Apply any DI-backed or Dynamic Colors theme **after** `startKoin` finishes (see below) â€” never probe Koin via `GlobalContext`

### Bootstrap (`Application`)

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
        // Theme / Dynamic Colors that need a live process â€” only AFTER Koin start
        applyAppTheme()
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@App)
            lazyModules(KoinModules().getKoinModules())
        }
    }

    private fun applyAppTheme() {
        // e.g. DynamicColors.applyToActivitiesIfAvailable(this)
        // or read prefs via inject() â€” safe here because startKoin already returned
    }
}
```

### Theme apply â€” after Koin start (mandatory)

- **Do not** use `GlobalContext.getOrNull()` (or any GlobalContext probe) to gate theme / DI
- **Do** sequence work: `startKoin { lazyModules(...) }` first, then theme apply in the same `Application.onCreate`
- Activity helpers (`enableMaterialDynamicTheme`) may call `DynamicColors.applyToActivityIfAvailable(this)` directly â€” Application always starts Koin before any Activity
- Keep `startKoin` **only** in `Application` â€” never in Activity (theme change recreates Activity â†’ `KoinAppAlreadyStartedException`)
- If you must `get()` a dependency right after `lazyModules`, use `waitAllStartJobs()` / `awaitAllStartJobs()` â€” not GlobalContext checks

## Readable sections (mandatory)

Every `lazyModule` body must use **clear section headers** so bindings stay readable and separated by concern.

### Comment style

```kotlin
//// SectionName
```

- Exactly four slashes + space + PascalCase / Title-style name
- Blank line **before** each section (except the first)
- Blank line **after** the last binding of a section when another section follows
- One concern per section â€” do not mix DataSources with ViewModels in the same block
- Skip empty sections; add a section only when it has bindings
- Tiny modules (single `viewModel`) still get one section header for consistency

### Standard section names by module type

| Module                           | Section order (use only what applies)                                                                                                          |
|----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| `dataModule` / data area modules | `//// DataSources` â†’ `//// Repositories` (optional `//// Managers` / SDK wrappers **before** DataSources if needed)                          |
| `useCaseModule`                  | Group by domain area: `//// Entrance`, `//// Location`, `//// Prayer`, â€¦ â€” never dump all factories in one unsorted list when areas differ |
| `*PresentationModule`            | `//// ViewModels` (optional `//// Helpers` if feature-scoped singles)                                                                          |
| `corePlatformModule`             | `//// Dispatchers` â†’ `//// Managers` â†’ `//// Other`                                                                                        |
| `coreModule` / UI core           | `//// Observers` / `//// Providers` / concern-named sections                                                                                   |
| `appModule`                      | `//// App` (info providers, app-scoped singles)                                                                                                |
| Ads feature modules              | `//// DataSources` â†’ `//// Repositories` â†’ `//// UseCases` â†’ `//// ViewModels` â†’ `//// Managers` / `//// Config`                       |
| `KoinModules` list               | Group returned modules: `//// Core` â†’ `//// Data` â†’ `//// Domain` â†’ `//// Presentation` â†’ `//// Ads`                                   |

Do **not** invent cryptic headers (`//// stuff`, `//// temp`). Name the concern.

## Module organization

| Location                     | Contents                                                                                             |
|------------------------------|------------------------------------------------------------------------------------------------------|
| `app/di/`                    | App-level singles, **module aggregation** (`KoinModules`) only â€” not UseCases / repo interfaces    |
| `domain/di/`                 | UseCase **factories** (`useCaseModule` / `domainModule`)                                             |
| `data/di/`                   | DataSource + Repository **impl** bindings (`dataModule`)                                             |
| `presentation/<feature>/di/` | Feature ViewModels                                                                                   |
| `core-*/di/`                 | Platform and UI dependencies                                                                         |
| ads module `di/`             | Existing ad managers / ad ViewModels (named qualifiers per placement) — not MVI unless the user asks |

### Domain vs data ownership (DI + source)

| Create in `:domain`          | Create in `:data`                        | Never in `:data`          |
|------------------------------|------------------------------------------|---------------------------|
| Repository **interfaces**    | Repository **impls** (`*RepositoryImpl`) | UseCase classes           |
| UseCase classes (`*UseCase`) | DataSources / managers                   | Repository **interfaces** |
| `useCaseModule` factories    | `dataModule` bindings                    | â€”                       |

Presentation injects UseCases and repository **interfaces** â€” never `*RepositoryImpl` or DataSources from `:data`.

## Binding types

| Scope       | Use for                                          |
|-------------|--------------------------------------------------|
| `single`    | Repositories, DataSources, managers, dispatchers |
| `factory`   | UseCases (stateless, cheap to recreate)          |
| `viewModel` | ViewModels                                       |

## Examples

### `dataModule`

```kotlin
val dataModule = lazyModule {

    //// DataSources
    single { LanguageDataSource() }
    single { SharedPrefManager(androidContext()) }
    single { RemoteConfigDataSource() }

    //// Repositories
    single<LanguageRepository> { LanguageRepositoryImpl(get()) }
    single<SharedPrefRepository> { SharedPrefRepositoryImpl(get()) }
    single<RemoteConfigRepository> { RemoteConfigRepositoryImpl(get(), get(), get()) }
}
```

- DataSources before Repositories
- Do **not** register UseCases here
- If you split (e.g. `sharedPrefModule`), keep the same section order inside each `lazyModule`

### `useCaseModule` (domain)

```kotlin
val useCaseModule = lazyModule {

    //// Entrance
    factory { GetEntranceDestinationUseCase(get()) }

    //// Location
    factory { GetLocationAndAddressUseCase(get()) }

    //// Internet
    factory { CheckInternetUseCase(get()) }

    //// Prayer
    factory { GetPrayerSettingsUseCase(get()) }
    factory { SavePrayerSettingsUseCase(get()) }
    factory { GetTodayPrayerTimesUseCase(get()) }
}
```

- Lives under `:domain` â€” **not** `:data`
- Sort factories by area; keep related UseCases together

### Presentation

```kotlin
val homePresentationModule = lazyModule {

    //// ViewModels
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
}
```

### `corePlatformModule`

```kotlin
val corePlatformModule = lazyModule {

    //// Dispatchers
    single { Dispatchers.IO }
    single { Dispatchers.Default }

    //// Managers
    single { InternetManager(androidContext()) }
}
```

### Ads (existing architecture — not MVI)

Keep this ads DI shape. Do not replace ad ViewModels with Intent/State/Effect unless the user **explicitly** asks (`21-ads-billing`).

```kotlin
val bannerAdModule = lazyModule {

    //// DataSources
    single { DataSourceLocalBanner() }
    single { DataSourceRemoteBanner(context = get()) }

    //// Repositories
    single { RepositoryBannerImpl(get(), get()) }

    //// UseCases
    single { UseCaseBanner(get(), get(), get(), get()) }

    //// ViewModels
    viewModel { ViewModelBanner(get()) }
}
```

### Composition root grouping

```kotlin
fun getKoinModules() = listOf(
    //// Core
    appModule,
    coreModule,
    corePlatformModule,

    //// Data
    dataModule,

    //// Domain
    useCaseModule,

    //// Presentation
    entrancePresentationModule,
    homePresentationModule,

    //// Ads
    bannerAdModule,
    nativeAdModule,
)
```

## Other rules

- **Every new module must be registered** in the composition root list
- Bind interfaces to implementations: `single<XRepository> { XRepositoryImpl(get()) }`
- Dispatchers: `single { Dispatchers.IO }` and `single { Dispatchers.Default }` â€” **no** `named("io")` / `named("default")`; repos often use `ioDispatcher: CoroutineDispatcher = Dispatchers.IO`
- Use named qualifiers only for true parallel instances (e.g. multiple ad placements): `named("banner_home")`
