---
name: test-unit
description: Write missing JVM unit tests (UseCases, ViewModels, Flow with fakes), run them, report results; on failures ask consent before fixing production code and retest. Use for unit/MVI/fake-repo tests. Part of test-* suite.
---

# Unit Tests

## Start banner

First user-visible sentence when this skill runs (verbatim):

> We are going to write missing JVM unit tests, run them, and report results — no emulator or device required. On failures we will ask before fixing production code.

Follow `.cursor/rules/11-testing.mdc`, `06-coroutines-flow.mdc`, `18-errors-result.mdc`.

Obey `.cursor/project-settings.json`: if `writeTestsWithFeatures` is `false`, only write tests when the user explicitly asks.

## Workflow (mandatory)

1. **Discover gaps** — missing UseCase / ViewModel / mapper / Flow-with-fakes coverage for the scoped feature or modules
2. **Write missing tests** — follow Priority and conventions below; skip trivial getter-only tests
3. **Execute** — from project root (Windows: `.\gradlew.bat`):

   ```bash
   ./gradlew test
   ```

   Prefer a module-scoped task when the scope is clear (e.g. `:domain:test`, `:presentation:test`).
4. **Report** — clear Pass/Fail summary (modules, failing test names, short reason)
5. **On failures** — **stop**. Ask user consent before changing production/app code. Show failing tests + suspected cause. Do **not** silently weaken assertions to go green
6. **On consent** — prefer fixing **app/domain code** when the test correctly exposes a bug; fix the **test** only if it was wrong/brittle (say so when asking). Then **retest** and report again until green or user stops
7. **If user declines** — leave failures listed; do not change production code

## Priority

1. **UseCases** — fake domain repositories, JVM tests
2. **ViewModels** — Intents → State + Effects (**feature** screens). Skip converting `:gmaAds` / ads ViewModels to MVI tests unless the user **explicitly** asks
3. **Mappers** — pure Kotlin when non-trivial
4. **Flow / coroutine behavior** — UseCase or repository APIs that return `Flow` / use `withContext`, still with **fakes** (no real network/DB)

Skip trivial getter tests and template-only assertions.

## Naming

```
<Unit>_when_<condition>_then_<result>
```

## UseCase test

```kotlin
class GetUserUseCaseTest {
    private val repository = FakeUserRepository()
    private val useCase = GetUserUseCase(repository)

    @Test
    fun getUser_whenRepositoryReturnsUser_thenReturnsUser() = runTest {
        repository.user = User(id = "1", name = "Test")
        val result = useCase("1")
        assertEquals("Test", result?.name)
    }
}
```

- Fake implements domain interface in `src/test`
- `runTest` for suspend APIs

## ViewModel test

- Drive via `handleIntent`
- Assert `state.value` after emissions (Turbine only if already in catalog + approved)
- Collect effects in test scope for one-shot events
- Inject test dispatchers (`StandardTestDispatcher` / `UnconfinedTestDispatcher`)
- No real Android framework — mockK only if already approved in project

```kotlin
@Test
fun viewModel_whenLoadFails_thenEmitsShowError() = runTest {
        fakeRepo.shouldFail = true
        viewModel.handleIntent(FeatureIntent.Load)
        advanceUntilIdle()
        // assert state error flag or effect
    }
```

## Flow / coroutine tests (with fakes)

- Assert emission order, completion, and failure mapping
- Prefer fakes for DataSources — no real network/DB
- Use `runTest` + `StandardTestDispatcher` / `UnconfinedTestDispatcher`
- Collect with Turbine **only if** already in catalog + approved; otherwise `first()` / `toList()` on finite flows
- Advance virtual time for delays/debounce
- Do not swallow `CancellationException` incorrectly
- Do not collect unbounded hot flows without a timeout

```kotlin
@Test
fun repository_whenDataSourceEmits_thenMapsToDomain() = runTest {
        fakeDataSource.items = listOf(dto)
        val emissions = repository.observeItems().take(1).toList()
        assertEquals(expectedDomain, emissions.first().first())
    }
```

## Location

Mirror package under `src/test/java`.

## Forbidden

- Instrumentation / Espresso for pure business logic → use `test-e2e` only for user-visible device flows
- Real Room / Retrofit / network / DB → use `test-integration`
- Tests that only assert constants
- Blocking `runBlocking` outside `runTest` helpers
- Fixing production code without explicit user consent after failures
- Inventing secrets or hitting real paid APIs
