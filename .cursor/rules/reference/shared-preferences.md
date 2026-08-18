# SharedPreferences (reference pattern)

Follow `.cursor/rules/03-android-architecture.mdc`, `07-dependency-injection.mdc`, `16-logging.mdc`, `18-errors-result.mdc`, `26-data-persistence.mdc`. Match reference layout:

```
data/sharedPreferences/
  dataSource/SharedPrefDataSource.kt   # class SharedPrefManager — sync only, no dispatcher
  repository/SharedPrefRepositoryImpl.kt
domain/repository/<area>/SharedPrefRepository.kt   # suspend API
```

## DataSource — `SharedPrefManager` (no dispatcher)

- **Only** `Context` in constructor — never inject `CoroutineDispatcher` here
- Single prefs file: `context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)`
- Private `KEY_*` constants at top of file (or file-level `private const`)
- Sync access only: properties with get/set + `sharedPreferences.edit { }`
- Enums: store `name`, read with `runCatching { Enum.valueOf(raw) }.getOrDefault(default)`
- Composite values: explicit functions (`readX()`, `writeX()`)
- RC / feature key strings as `val` when reused by Remote Config writer
- **No** `suspend`, **no** `withContext`, **no** domain types

```kotlin
class SharedPrefManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    var isFeatureEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_FEATURE, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_FEATURE, value) }
}
```

## Domain interface

- `suspend` methods returning domain types (`GeoLocation`, booleans, ints)
- Group by area (`premium`, `settings`) — avoid god interface when project grows
- No Android / SharedPreferences types

## Repository impl — dispatcher here only

```kotlin
class SharedPrefRepositoryImpl(
    private val dataSource: SharedPrefManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SharedPrefRepository {

    override suspend fun isFeatureEnabled(): Boolean = withContext(ioDispatcher) {
        dataSource.isFeatureEnabled
    }

    override suspend fun setFeatureEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        dataSource.isFeatureEnabled = enabled
    }
}
```

- Wrap **every** read/write in `withContext(ioDispatcher)`
- Map to domain entities here (`Pair` → `GeoLocation`, normalize strings, `buildSet`)
- Log in repository when meaningful: `SharedPrefRepositoryImpl: setFeatureEnabled: Success: enabled=$enabled`

Prefer injecting `ioDispatcher` from DI when project provides dispatchers in `core-platform`.

## DI (`lazyModule` only)

```kotlin
val dataModule = lazyModule {

    //// DataSources
    single { SharedPrefManager(androidContext()) }

    //// Repositories
    single<SharedPrefRepository> { SharedPrefRepositoryImpl(get()) }
}
```

- Interface `SharedPrefRepository` + any related UseCases live in **`:domain`**
- `factory { …UseCase }` in domain `useCaseModule` — not in `:data`
- Never `module { }` — always `lazyModule { }`

Register in `data/di/DataModule.kt` (or split module val).

## Remote Config cache pattern (optional)

`RemoteConfigRepositoryImpl` may write fetched RC ints into `SharedPrefManager` properties inside repository (on IO dispatcher), same as reference app. Extra keys on an existing cache → skill **`add-firebase-remote-config`**.

## Consumption

- Presentation / UseCases → **domain `SharedPrefRepository` interface**
- Avoid injecting `SharedPrefManager` in presentation. Reference `gmaAds` may inject it — **leave ads as-is** unless the user explicitly asks to refactor ads

## Forbidden

- `SharedPreferences` in Fragment / ViewModel
- Dispatcher on DataSource / `SharedPrefManager`
- UseCase or repository **interface** under `:data`
- `module { }` — use `lazyModule { }` only
- `suspend` on sync prefs data source class

## Tests

- `FakeSharedPrefRepository` implementing domain interface for UseCase / ViewModel tests
