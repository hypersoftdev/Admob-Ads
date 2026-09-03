---
name: test-complete
description: Run all unit/integration/E2E tests plus emulator walkthrough; report results; on failures ask consent before fixing production code and retest. Use for full test pass. Requires emulator or physical device for connected tests and walkthrough.
---

# Complete Test Pass

## Start banner

First user-visible sentence when this skill runs (verbatim):

> We are going to run the full test pass and an emulator walkthrough — an emulator or physical device is required for connected/`androidTest` and the walkthrough (or we proceed with the device already attached). On failures we will ask before fixing production code.

If no device is attached: say so after the banner, run JVM `test` where possible, mark connected/`androidTest` and walkthrough as blocked — do not claim they passed.

Follow `.cursor/rules/11-testing.mdc`.

## Step 1 — Run existing automated suites

From project root (Gradle):

```bash
./gradlew test
./gradlew connectedDebugAndroidTest
```

On Windows PowerShell, use `.\gradlew.bat` equivalents.

- Report pass/fail per module
- Integration tests usually live under `src/test` (covered by `test`); use `connectedDebugAndroidTest` for E2E / androidTest integration
- If `connectedDebugAndroidTest` cannot run (no device), note blocker and continue with JVM results

## Step 2 — Align with test skills (coverage gaps)

- Gaps in UseCase/ViewModel/Flow-with-fakes → suggest or invoke `test-unit` (that skill owns writing + its own run/fix loop)
- Gaps in Room/MockWebServer multi-layer wiring → `test-integration`
- Critical UX / device flow gaps → `test-e2e`
- Honor `writeTestsWithFeatures` in `.cursor/project-settings.json`

## Step 3 — Emulator walkthrough checklist

On an emulator (or device) matching minSdk / target as practical:

- [ ] Cold start → Entrance (or start destination) loads
- [ ] Primary nav path(s) reachable without crash
- [ ] Orientation: rotate if `orientation` is `both` or `landscape`
- [ ] Theme: day/night if `themeModes` includes both
- [ ] Permission flows (if any) — deny then grant
- [ ] Offline / no-network graceful path if feature uses network
- [ ] Back stack / predictive back does not break primary flows
- [ ] No obvious ANR / frozen UI on list-heavy screens

Document: device/API, build variant, what was skipped.

## Step 4 — Failures: consent → fix → retest

After Gradle and/or walkthrough failures:

1. **Stop** — present failing tests / walkthrough items + suspected cause
2. **Ask consent** before changing production/app code
3. **On consent** — prefer fixing app code toward production-ready behavior; fix tests only if incorrect/brittle (say so when asking). Then **retest** (repeat Step 1, and Step 3 if walkthrough was affected) and report again
4. **If user declines** — leave failures listed; do not change production code
5. Do **not** silently weaken assertions to go green

## Output

```markdown
## Automated
- unit: Pass/Fail (details)
- integration: Pass/Fail / N/A (details)
- e2e (androidTest): Pass/Fail / Skipped (reason)

## Walkthrough
- ✅/❌ per checklist item

## Blockers
- …

## Suggested follow-ups
- test-unit / test-integration / test-e2e items to add

## Fix consent
- Asked / Approved / Declined (if failures)
```

## Limits

- Do not invent production secrets or hit real paid APIs
- Do not claim full coverage if suites were skipped
- Emulator automation beyond Gradle connected tests is best-effort checklist unless the project already has UI automators approved
- Do not fix production code without explicit user consent after failures
