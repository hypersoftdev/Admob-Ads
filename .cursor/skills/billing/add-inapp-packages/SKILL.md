---
name: add-inapp-packages
description: Add one-time in-app products (non-consumable or consumable) to an app that already has billing. Audits existing products and asks for missing packages and IDs. Use when adding lifetime unlock, credits pack, or /add-inapp-packages.
---

# Add In-App Packages

Follow `.cursor/rules/21-ads-billing.mdc`, `00-global.mdc`, `04-mvi-presentation.mdc`, `16-logging.mdc`.

Full detail: [`.cursor/rules/reference/premium-billing.md`](../../rules/reference/premium-billing.md)

Obey `.cursor/project-settings.json` when present.

For greenfield billing → use `implement-in-app-billing`. For subscription tiers → use `add-subscription-packages`.

---

## Entry decision tree

| App state           | Action                                |
|---------------------|---------------------------------------|
| No billing stack    | Stop → **`implement-in-app-billing`** |
| Legacy v3 listeners | Migrate to v4 first (reference §7)    |
| Billing exists      | Continue below                        |

---

## Step 0 — Mandatory user confirmation (do NOT write code until done)

### 0.1 Audit code first

Read and document:

- `BillingProductIds` (or equivalent constants)
- `BillingDataSource` → `setNonConsumables(...)` and `setConsumables(...)` lists
- In-app purchase UI (Premium lifetime row, shop screen, etc.)
- Entitlement handling per product type

Build audit table:

| Product (label) | In code? | In UI? | Type           | Product ID |
|-----------------|----------|--------|----------------|------------|
| Lifetime unlock | yes/no   | yes/no | non-consumable | …          |
| Credits pack    | yes/no   | yes/no | consumable     | …          |
| …               | …        | …      | …              | …          |

### 0.2 Q1 — What in-app product(s) to add?

Use **`AskQuestion`** based on audit — offer **only missing** products.

Examples:

- No in-app yet → "Add lifetime unlock", "Add consumable pack", "Other"
- Lifetime exists → "Add 2nd consumable", "Other"
- All requested products already wired → inform user; only proceed if changing IDs (warn)

### 0.3 Q2 — Product ID(s)

| Option            | Action                                                  |
|-------------------|---------------------------------------------------------|
| Suggested default | `{applicationId}` for lifetime, or `basic_inapp_{name}` |
| Custom            | User enters ID per product                              |

**Default suggestions** (offer every time):

| Type                      | Default product ID                             |
|---------------------------|------------------------------------------------|
| Lifetime / unlock premium | `{applicationId}` from `project-settings.json` |
| Generic unlock            | `basic_inapp_unlock`                           |
| Credits pack              | `basic_inapp_credits_100` (adjust suffix)      |

Also confirm per product: **non-consumable** vs **consumable**.

### 0.4 ID validation

**Product ID:**

- Starts with number or lowercase letter
- Only `[a-z0-9_.]`, max 40 chars
- Warn and re-ask on violation

Example:
> `Lifetime_Unlock` is invalid — use `basic_inapp_unlock` or `{applicationId}`.

### 0.5 Confirm summary

Print: new Console products, type (non-consumable/consumable), code + UI delta. User OK before coding.

---

## Step 1 — Play Console

For each new product:

1. Monetize → In-app products → create managed product
2. Use exact product ID from confirmation
3. Set type: non-consumable (lifetime) or consumable
4. Activate on internal track

---

## Step 2 — Domain

Add constants to `BillingProductIds`:

```kotlin
const val LIFETIME_PRODUCT = "…"  // confirmed ID

val nonConsumableProductIds: List<String> = listOf(/* existing + new */)
val consumableProductIds: List<String> = listOf(/* existing + new */)
```

Extend `InAppProduct` entity / enum if the project uses one.

---

## Step 3 — Data

Append new IDs to `setNonConsumables` or `setConsumables` in `BillingDataSource`.

Extend `BillingRepositoryImpl` to map new product prices from `productsState`.

Do not create a second `BillingManager`.

---

## Step 4 — Presentation

Add purchase entry for new product(s):

- Row/button on Premium screen (lifetime pattern) or dedicated shop screen
- MVI: `LaunchInAppPurchase(productId, offerId?)`
- Fragment: `billingManager.purchaseInApp(activity, productId, offerId)`
- Map `PurchaseOutcome` → domain result

Gate buy button until offer/price loaded from repository.

Portrait **and** landscape. Strings in `:core-ui`.

---

## Step 5 — Entitlement

| Type               | Behavior                                                                                                          |
|--------------------|-------------------------------------------------------------------------------------------------------------------|
| **Non-consumable** | `isAppPurchased = true` on Success + `purchasesState` sync                                                        |
| **Consumable**     | Grant credits/balance via separate pref or UseCase; do not assume `isAppPurchased` unless product unlocks premium |

After Success, ads/UI react via existing premium flag or new balance observer.

---

## Step 6 — Tests

When `writeTestsWithFeatures: true`:

- ViewModel test: click in-app product → `LaunchInAppPurchase` with confirmed `productId`
- Non-consumable success → entitlement UseCase invoked

---

## Step 7 — QA

- [ ] New product price loads from Play
- [ ] Purchase sheet opens for confirmed product ID
- [ ] Non-consumable: `isAppPurchased == true`, ads off
- [ ] Consumable: balance updates; can repurchase
- [ ] Restore/reinstall: non-consumable still owned
- [ ] Cancel: no error toast

---

## Do not

- Add subscription tiers here (use `add-subscription-packages`)
- Greenfield billing setup (use `implement-in-app-billing`)
- Duplicate product IDs already in `setNonConsumables` / `setConsumables`
- Hardcode prices — always from `productsState` mapper
- Log purchase tokens or PII
