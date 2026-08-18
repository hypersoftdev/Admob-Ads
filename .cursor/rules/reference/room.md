# Room Cache (reference pattern)

Follow `.cursor/rules/08-gradle.mdc`, `13-libraries-stack.mdc`, `06-coroutines-flow.mdc`, `03-android-architecture.mdc`, `26-data-persistence.mdc`. **Add Room only with human approval** — add to `libs.versions.toml` first.

## Layers

| Layer | Contents |
|-------|----------|
| Data | `@Entity`, `@Dao`, `RoomDatabase`, `*DataSource` |
| Domain | Entities (pure Kotlin) + repository interface |
| Data | `*RepositoryImpl` maps Entity ↔ domain |

- Room types stay in `:data` — not in domain or presentation

## Setup

1. Catalog: `room-runtime`, `room-ktx`, `ksp` room compiler
2. `:data` `build.gradle.kts`: ksp + dependencies
3. Database class — version migrations planned from day one

```kotlin
@Database(entities = [ItemEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
```

## DAO rules

- Suspend functions or `Flow` for queries — no blocking on Main
- Large list reads: map off Main in repository (`flowOn(IO)`)
- Pagination: `LIMIT`/`OFFSET` or Paging 3 if approved

## Repository

- Orchestrate DAO + network DataSource (cache-first / network-first per feature)
- **Inject `ioDispatcher` in Repository** — wrap all DataSource/DAO calls with `withContext(ioDispatcher)`
- DataSource classes: no dispatcher parameter
- DTO → entity mapping in data layer
- Expose `Flow<List<DomainEntity>>` or suspend APIs to domain

## DI (`lazyModule` only)

```kotlin
val dataModule = lazyModule {

    //// DataSources
    single { provideDatabase(get()) }
    single { get<AppDatabase>().itemDao() }

    //// Repositories
    single<ItemRepository> { ItemRepositoryImpl(get(), get(), get()) }
}
```

- `ItemRepository` interface + UseCases in `:domain` (`useCaseModule`)
- Never put interfaces/UseCases in `:data`

## ProGuard

- Add keep rules for entities if release minify breaks Room

## Forbidden

- Room in `:presentation` or `:domain` modules
- `notifyDataSetChanged` for huge cached lists — use ListAdapter + DiffUtil
- Schema changes without migration strategy
