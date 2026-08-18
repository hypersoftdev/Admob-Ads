---
name: add-subscription-packages
description: Add or extend subscription packages in an app that already has billing (e.g. weekly-only to add monthly and yearly). Audits existing tiers, asks for missing plans and IDs. Use when expanding paywall subscription tiers or /add-subscription-packages.
---

# Add Subscription Packages

Follow `.cursor/rules/21-ads-billing.mdc`, `00-global.mdc`, `04-mvi-presentation.mdc`, `16-logging.mdc`.

Full detail: [`.cursor/rules/reference/premium-billing.md`](../../rules/reference/premium-billing.md)

Obey `.cursor/project-settings.json` when present.

For greenfield billing → use `implement-in-app-billing`. For in-app products → use `add-inapp-packages`.

---

## Entry decision tree

| App state                                                  | Action                                |
|------------------------------------------------------------|---------------------------------------|
| No `BillingDataSource` / `BillingRepository` / paywall     | Stop → **`implement-in-app-billing`** |
| Legacy v3 (`BillingPurchaseListener`, `startConnection()`) | Migrate to v4 first (reference §7)    |
| Billing exists                                             | Continue below                        |

---

## Step 0 — Mandatory user confirmation (do NOT write code until done)

### 0.1 Audit code first

Read and document:

- `BillingProductIds` (or hardcoded strings in repository)
- `PremiumPlan` enum / sealed class
- `BillingDataSource` / `BillingRepositoryImpl` → `setSubscriptions(...)` list
- Paywall UI — which tiers are shown

Build audit table:

| Tier    | In code? | In UI? | Product ID | Plan ID |
|---------|----------|--------|------------|---------|
| Weekly  | yes/no   | yes/no | …          | …       |
| Monthly | yes/no   | yes/no | …          | …       |
| Yearly  | yes/no   | yes/no | …          | …       |

### 0.2 Q1 — What subscription(s) to add?

Use **`AskQuestion`** with **only missing tiers** as options.

Examples:

- Weekly exists → offer Monthly, Yearly, Both, Other (custom tier)
- Weekly + monthly exist → offer Yearly only
- All three exist → inform user; ask if they want to change IDs (warn about Console / subscriber impact)

### 0.3 Q2 — Product & plan IDs for each selected tier

| Option       | Action                                      |
|--------------|---------------------------------------------|
| Use defaults | Suggest defaults for **missing tiers only** |
| Custom       | User enters product + plan ID per tier      |

**Default IDs** (suggest every time):

| Tier    | Product ID              | Plan ID              |
|---------|-------------------------|----------------------|
| Weekly  | `basic_product_weekly`  | `basic-plan-weekly`  |
| Monthly | `basic_product_monthly` | `basic-plan-monthly` |
| Yearly  | `basic_product_yearly`  | `basic-plan-yearly`  |

If tier **already in code** and user wants different IDs → warn: Play Console mismatch, existing subscribers affected. Require explicit confirm.

### 0.4 ID validation

Same rules as `implement-in-app-billing`:

**Product ID:** lowercase start, `[a-z0-9_.]`, max 40 chars — warn and re-ask on violation.

**Plan ID:** prefer hyphens (`basic-plan-weekly`), `[a-z0-9._-]`, max 63 chars.

Example warnings:
> `Basic_Product_Monthly` is invalid — use `basic_product_monthly`.
> `basic_plan_monthly` works; our convention uses `basic-plan-monthly`. Continue?

### 0.5 Confirm summary

Print delta: new Console products, new constants, UI changes. User OK before coding.

---

## Step 1 — Play Console

For each **new** tier:

1. Create subscription product with confirmed product ID
2. Add base plan with confirmed plan ID (Option 1)
3. Optional trial offer on new tier — offer IDs remain dynamic
4. Activate; verify license testers can see products on internal track

Do not remove or rename existing Console products without migration plan.

---

## Step 2 — Domain

Extend `BillingProductIds` with new constants only.

Extend `PremiumPlan` enum with new values.

No repository interface change unless observe API is missing.

---

## Step 3 — Data

Append new product IDs to `allSubscriptionProductIds` / `setSubscriptions(...)`.

Verify `BillingRepositoryImpl` mapper picks new plans from `productsState` (prefer-trial logic unchanged).

Do not create a second `BillingManager`.

---

## Step 4 — Presentation

Add UI for new tiers on existing paywall(s):

- Plan picker row/card per new tier
- `PlanClicked` → `LaunchSubsPurchase(productId, planId, offerId)`
- CTA copy from mapped `SubscriptionOffer` (trial vs paid)
- Landscape layout for new controls

Do not duplicate IDs in Fragment — read from State/Effect only.

---

## Step 5 — Existing subscribers

- Entitlement logic unchanged — weekly subscriber stays premium
- `purchasesState` restore must still set `isAppPurchased`
- Plan changes (`updateSubs`) only if product explicitly requires — not default for this skill

---

## Step 6 — Ads & navigation

Premium gating unchanged. New tiers must not bypass `isAppPurchased`.

---

## Step 7 — Tests

When `writeTestsWithFeatures: true`:

- Extend ViewModel tests: fake repo emits offers for all tiers including new ones
- Assert new plan click emits correct `LaunchSubsPurchase` with `offerId`

---

## Step 8 — QA

- [ ] New tier visible with correct price from Play
- [ ] Trial sheet shows trial when eligible (`offerId` passed)
- [ ] Existing subscriber: still premium, no ads
- [ ] New purchase on added tier: `isAppPurchased == true`
- [ ] Cancel purchase: no error toast

---

## Do not

- Re-scaffold billing from scratch (use `implement-in-app-billing`)
- Add in-app products here (use `add-inapp-packages`)
- Change existing product IDs without user confirm + Console alignment
- Hardcode offer IDs
