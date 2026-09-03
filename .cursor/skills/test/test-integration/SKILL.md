---
name: test-integration
description: Write missing multi-layer integration tests (Room in-memory / MockWebServer), run them, report results; on failures ask consent before fixing production code and retest. Not for fakes-only unit or Espresso E2E. Part of test-* suite.
---

# Integration Tests

## Start banner

First user-visible sentence when this skill runs (verbatim):

> We are going to write missing multi-layer integration tests, run them, and report results — no device if JVM-only; if `androidTest` is required we need an emulator or physical device attached. On failures we will ask before fixing production code.

If the work must use `androidTest` and no device is attached: say so after the banner, continue with any JVM-safe parts, and mark device steps blocked.

Follow `.cursor/rules/11-testing.mdc`, `26-data-persistence.mdc`, and matching `rules/reference/` (Room / Retrofit) when relevant.

Obey `.cursor/project-settings.json` (`writeTestsWithFeatures`).

## Workflow (mandatory)

1. **Discover gaps** — missing Repository / DAO / API multi-layer coverage (real Room in-memory, MockWebServer, etc.)
2. **Write missing tests** — follow Scope below; skip fakes-only single-class tests (`test-unit`) and UI (`test-e2e`)
3. **Execute** — JVM (Windows: `.\gradlew.bat`):

   ```bash
   ./gradlew test
   ```

   If tests live under `androidTest`, also:

   ```bash
   ./gradlew connectedDebugAndroidTest
   ```

   Prefer module-scoped tasks when the scope is clear.
4. **Report** — clear Pass/Fail summary
5. **On failures** — **stop**. Ask user consent before changing production/app code. Show failing tests + suspected cause. Do **not** silently weaken assertions
6. **On consent** — prefer fixing **app/data code** when the test is correct; fix the **test** only if wrong/brittle (say so when asking). Then **retest** and report again
7. **If user declines** — leave failures listed; do not change production code

## Scope

- Repository (or similar) wired to **real** lower layers:
  - Room **in-memory** database + DAOs
  - Retrofit + **MockWebServer** (or project-approved equivalent already in catalog)
- DAO / migration smoke when the change touches schema
- Assert cross-layer behavior (mapping, persistence, error mapping), not UI

Prefer `src/test` JVM. Use `src/androidTest` **only** when the layer requires Android runtime — then restate that an emulator or physical device is required (or proceed with one already attached).

## Naming

```
<Unit>_when_<condition>_then_<result>
```

## Approach

1. Build the smallest real stack needed (e.g. in-memory Room + repository)
2. Drive through the repository (or UseCase that only exists to orchestrate data) — not Fragments
3. Tear down DB / server after each test; keep tests independent
4. No production secrets, no real paid APIs
5. Add libraries only if already in the version catalog **and** approved — do not invent new deps

## Forbidden

- Fakes-only single-class tests → `test-unit`
- Espresso / user-visible UI flows → `test-e2e`
- Hitting real production backends or committing secrets
- Flaky sleeps instead of deterministic server/DB setup
- Fixing production code without explicit user consent after failures
