---
name: review-security
description: Review Android code for secrets, exported components, permissions, logging leaks, and ProGuard. Use when security review, release prep, or auditing sensitive data handling. Prefer review-complete for a full multi-check pass.
---

# Security Review

Follow `.cursor/rules/14-security-secrets.mdc`, `10-manifest.mdc`, `21-ads-billing.mdc`, `16-logging.mdc`.

## Secrets
- [ ] No API keys, tokens, keystore passwords in source, Gradle, or docs
- [ ] `local.properties` / CI env for secrets
- [ ] Debug uses sample ad/billing IDs — not production
- [ ] No secrets in logs (`16-logging`)

## Manifest
- [ ] `exported="false"` default for non-launcher components
- [ ] Intent-filters always have explicit `exported`
- [ ] Custom actions namespaced with `applicationId`
- [ ] Minimum permissions declared

## Data
- [ ] HTTPS for remote endpoints
- [ ] No PII/tokens in Analytics or Log calls
- [ ] External input validated before use (deep links, nav args)

## Storage
- [ ] Sensitive prefs encrypted if required by product
- [ ] No world-readable files

## Build
- [ ] Release minify/R8 enabled on app
- [ ] Signing credentials not hardcoded in `build.gradle.kts`
- [ ] ProGuard keeps only what’s needed

## Report

Number every actionable finding. Follow [fix-selection.md](../fix-selection.md) after the report.

```markdown
## Fix list
1. [Critical] leaked secret / exported receiver / logged token
2. [High] missing runtime permission UX / cleartext traffic
3. [Medium] over-broad permissions
```

Never paste actual secrets into the review report — reference file/line only.

After the report: **do not fix yet** — ask which numbers to fix per `fix-selection.md` (e.g. user replies `fix 1, 2, 4, 7`).
