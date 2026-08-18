---
name: create-clean-architecture
description: Add domain, data, and core pieces for a feature (UseCases, repository interfaces/impls, DataSources, DI). No Fragment/XML. Use when a screen needs new business/data capability, or after create-mvi when domain is missing. Follows Retrofit/Room/SharedPreferences rules references.
---

# Create Clean Architecture Layers

Follow `.cursor/rules/` — especially `02-project-structure`, `03-android-architecture`, `06-coroutines-flow`, `07-dependency-injection`, `18-errors-result`, `26-data-persistence`, and:

- `.cursor/rules/reference/retrofit.md`
- `.cursor/rules/reference/room.md`
- `.cursor/rules/reference/shared-preferences.md`

Obey `.cursor/project-settings.json` when present (`writeTestsWithFeatures`).

**Does not** create Fragment / layout XML / Intent-State-Effect — use `figma-to-xml` + `create-mvi` for those.

## Preconditions

- Confirm feature / area name (e.g. `user`, `premium`, `remoteConfig`)
- Confirm which pieces are needed: remote API / Room cache / SharedPreferences / pure domain UseCase over existing repo
- Optional: screen or Figma refs for context only (do not implement UI here)
- **Add Retrofit / Room / new libs only with human approval** — catalog first
- Do **not** rebuild `:gmaAds` as a new domain/data MVI feature unless the user **explicitly** asks. Ads keep their existing module structure (`21-ads-billing`)

## Create only what’s missing

```
domain/
  repository/<area>/<Name>Repository.kt     # interface ONLY
  usecase/<area>/<Name>UseCase.kt           # UseCase ONLY
  entity/…                                  # pure Kotlin domain models
  di/UseCaseModule.kt                       # lazyModule { factory { … } }

data/
  <area>/dataSource/…                       # DataSource / manager / Api / Dao wrapper
  <area>/repository/<Name>RepositoryImpl.kt # impl ONLY
  <area>/dto/…                              # DTOs / Room entities (data only)
  di/DataModule.kt                          # lazyModule { //// DataSources … //// Repositories … }

core-* (only if required)
  e.g. OkHttp/Retrofit provider in :core-platform; shared Constants in :core-common
```

- **Never** create UseCase or repository interface under `:data`
- Presentation must **not** depend on `:data`
- DataSources: **no** dispatcher parameter; Repository wraps with `withContext(ioDispatcher)`

## Pattern selection

| Need                            | Follow                                                     |
|---------------------------------|------------------------------------------------------------|
| REST / HTTP                     | `reference/retrofit.md` + `26-data-persistence`            |
| Local DB / cache                | `reference/room.md` + `26-data-persistence`                |
| Flags / settings / RC cache     | `reference/shared-preferences.md` + `22-platform-firebase` |
| Existing repo, new UseCase only | Domain UseCase + `useCaseModule` factory                   |

## DI (`lazyModule` only)

```kotlin
val useCaseModule = lazyModule {

    //// Feature
    factory { GetFeatureUseCase(get()) }
}

val dataModule = lazyModule {

    //// DataSources
    single { FeatureDataSource() }

    //// Repositories
    single<FeatureRepository> { FeatureRepositoryImpl(get(), get()) }
}
```

Register **all** new modules/vals in app composition root (`KoinModules`). Never `module { }`. Use `//// Section` headers (`07-dependency-injection`).

## Errors & logging

- Map failures at data boundary to typed `DataError` / `Outcome` (`18-errors-result`)
- Log in repository: `Constants.TAG*` — `ClassName: functionName: State: details`
- No tokens / PII in logs

## Tests

- If `writeTestsWithFeatures` is `true`: UseCase unit test with `Fake*Repository` (`test/test-unit`)
- If `false`: skip unless user asks

## Verify

- [ ] Interfaces + UseCases only in `:domain`
- [ ] Impls + DataSources only in `:data`
- [ ] `dataModule`: `//// DataSources` then `//// Repositories`
- [ ] No presentation → data dependency introduced
- [ ] Dispatchers only in repository impls
- [ ] New libraries approved + in version catalog
- [ ] Tell user to wire presentation via `create-mvi` if UI not done yet
