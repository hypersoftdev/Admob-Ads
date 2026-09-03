# Retrofit API (reference pattern)

Follow `.cursor/rules/08-gradle.mdc`, `13-libraries-stack.mdc`, `06-coroutines-flow.mdc`, `18-errors-result.mdc`, `22-platform-firebase.mdc`, `26-data-persistence.mdc`. **Add Retrofit/OkHttp/Moshi only with approval** — catalog first.

Prefer existing `InternetManager` (`:core-platform`) for connectivity checks before calls.

## Layers

| Layer | Contents |
|-------|----------|
| Data | `ApiService` (Retrofit), DTOs, `RemoteDataSource`, `*RepositoryImpl` |
| Domain | Domain models + repository interface — no Retrofit types |
| Core-platform | OkHttp client, interceptors, base URL config |

## Setup

1. `libs.versions.toml`: retrofit, okhttp, converter (moshi/gson)
2. `:core-platform` or `:data`: OkHttp + Retrofit `single` in DI
3. Base URL from build config / `local.properties` — not hardcoded secrets

```kotlin
interface UserApi {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserDto
}
```

## Error handling

- Map HTTP/IO errors at data boundary to typed `DataError` / `Outcome` (`18-errors-result`)
- Never leak status codes or raw JSON bodies into UI State
- Log: `Constants.TAG` — `UserRepositoryImpl: getUser: Failed: ${e.message}`

## Dispatchers

- All network on `IO` in **Repository** (`withContext`); DataSource stays thin
- Repository returns domain models or typed failures

## DI (`lazyModule` only)

```kotlin
val dataModule = lazyModule {

    //// DataSources
    single { provideRetrofit(get()) }
    single { get<Retrofit>().create(UserApi::class.java) }

    //// Repositories
    single<UserRepository> { UserRepositoryImpl(get(), get(), get()) }
}
```

- `UserRepository` interface + UseCases in `:domain` (`useCaseModule`)
- Never put interfaces/UseCases in `:data`

## Forbidden

- Retrofit interfaces in domain/presentation
- Blocking calls on Main
- Logging auth tokens or full PII
- New networking stack if project already has one — extend existing client
