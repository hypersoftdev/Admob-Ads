---
name: test-e2e
description: Write missing E2E androidTest/Espresso tests for critical flows, run on device, report results; on failures ask consent before fixing production code and retest. Requires emulator or physical device. No new libraries without approval. Part of test-* suite.
---

# E2E / Instrumentation Tests

## Start banner

First user-visible sentence when this skill runs (verbatim):

> We are going to write missing E2E instrumentation tests, run them on device, and report results — an emulator or physical device is required (or we proceed with the device already attached). On failures we will ask before fixing production code.

If no device is attached: say so after the banner, still write tests where useful, do not claim E2E ran on device, and leave a clear blocker for the run step.

Follow `.cursor/rules/11-testing.mdc`.

Obey `.cursor/project-settings.json` (`orientation`, `writeTestsWithFeatures`).

## Workflow (mandatory)

1. **Discover gaps** — missing critical user-visible flows (entrance → primary feature, paywall, permission UX)
2. **Write missing tests** — follow Scope/Rules below
3. **Execute** (device required; Windows: `.\gradlew.bat`):

   ```bash
   ./gradlew connectedDebugAndroidTest
   ```

4. **Report** — clear Pass/Fail summary
5. **On failures** — **stop**. Ask user consent before changing production/app code. Show failing tests + suspected cause. Do **not** silently weaken assertions or add flaky sleeps
6. **On consent** — prefer fixing **app/UI code** when the test is correct; fix the **test** only if wrong/brittle (say so when asking). Then **retest** and report again
7. **If user declines** — leave failures listed; do not change production code

## Scope

- Critical user-visible flows only (entrance → primary feature, paywall, permission deny/grant UX)
- Prefer existing project `androidTest` stack — **do not** add Espresso/Compose/UI libs without approval

## Rules

- Use project's `testInstrumentationRunner`
- Keep tests independent — no shared mutable state between tests
- Assert user-visible behavior, not private ViewModel fields
- Match orientation policy from `project-settings.json` when relevant
- Strings via resources — avoid brittle hardcoded copy when `@string/` ids are stable

## Location

`src/androidTest/java` mirroring packages where practical.

## Forbidden

- Instrumentation for pure business logic → `test-unit`
- Multi-layer data wiring without UI → `test-integration`
- New UI-test dependencies without human approval
- Flaky sleeps — prefer IdlingResource / Espresso sync already in project
- Fixing production code without explicit user consent after failures
- Inventing secrets or hitting real paid APIs
