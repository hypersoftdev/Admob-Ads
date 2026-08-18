# Premium / In-App Billing — reference

Full playbook for subscriptions and one-time in-app products using [hypersoftdev/inappbilling](https://github.com/hypersoftdev/inappbilling) (`4.0.0+`, Play Billing Library 9.1.0).

**Skills:** `premium/implement-in-app-billing`, `premium/add-subscription-packages`, `premium/add-inapp-packages`.

Do not invent a parallel `BillingClient` wrapper — use the library + Clean Architecture below.

---

## 1. What we ship

| Piece                | Role                                                                                         |
|----------------------|----------------------------------------------------------------------------------------------|
| **Subscriptions**    | Weekly / monthly / yearly premium (ads-off + paid features)                                  |
| **One-time in-app**  | Non-consumable (lifetime unlock) or consumable (credits) via same `BillingManager`           |
| **Entitlement flag** | `isAppPurchased` in SharedPreferences — source of truth for ads/UI (subs + lifetime in-app)  |
| **Paywalls**         | Splash premium (optional) + in-app Premium screen                                            |
| **Library**          | `com.github.hypersoftdev:inappbilling` via JitPack — **not** raw `BillingClient` in features |

---

## 2. Library & Gradle

```toml
# gradle/libs.versions.toml — # Billing section
[versions]
inappbilling = "4.0.0"

[libraries]
hypersoft-inappbilling = { group = "com.github.hypersoftdev", name = "inappbilling", version.ref = "inappbilling" }
```

```kotlin
// :data and :presentation (purchase needs Activity)
implementation(libs.hypersoft.inappbilling)
```

JitPack in `settings.gradle.kts`:

```kotlin
maven(url = "https://jitpack.io")
```

**v4.0.0 breaking changes:** Listeners are gone. Use `suspend` + `Flow`. `BillingManager(context)` only — **no** external `CoroutineScope`. One app-lifetime singleton.

Add `const val TAG_BILLING = "TAG_BILLING"` in `:core-common` `Constants`. Internal library logs use tag `BillingManager`.

**Requires human approval** before adding the library if not already in the catalog (`13-libraries-stack.mdc`).

---

## 3. Default ID naming (suggest every time)

### Subscriptions — Option 1 (one product ↔ one plan)

| Tier    | Product ID              | Plan ID              |
|---------|-------------------------|----------------------|
| Weekly  | `basic_product_weekly`  | `basic-plan-weekly`  |
| Monthly | `basic_product_monthly` | `basic-plan-monthly` |
| Yearly  | `basic_product_yearly`  | `basic-plan-yearly`  |

- Product IDs: lowercase, underscores — `basic_product_{period}`
- Plan IDs: lowercase, **hyphens** — `basic-plan-{period}`

### One-time in-app

| Type                      | Default product ID                               |
|---------------------------|--------------------------------------------------|
| Lifetime / unlock premium | `{applicationId}` from `project-settings.json`   |
| Generic single pack       | `basic_inapp_unlock` or `basic_product_lifetime` |

### ID validation

**Product ID (subs + in-app):**

- Must start with a number or lowercase letter
- May contain `_` and `.`
- Max **40 characters**
- No uppercase, spaces, or other special characters

**Plan ID (subscriptions only):**

- Prefer hyphens (`basic-plan-weekly`)
- Allowed: `[a-z0-9._-]`
- Max **63 characters**

Warn and re-ask on violation. Store validated IDs in `BillingProductIds` only — never in Fragments.

---

## 4. Play Console — products, plans, offers

| ID                          | What it is                     | Example                     |
|-----------------------------|--------------------------------|-----------------------------|
| **Product ID**              | Subscription or in-app product | `basic_product_weekly`      |
| **Base plan ID** (`planId`) | Recurring period (subs only)   | `basic-plan-weekly`         |
| **Offer ID** (`offerId`)    | Trial / intro / promo option   | `freetrial` (Play-assigned) |

**Option 2** (one product, many plans) — avoid unless you persist product+plan on a backend.

### Offers (trials)

1. Base plan = default offer → library maps `offerId == ""` (paid, no trial).
2. Add offer on base plan for free trial → Play assigns Offer ID.
3. Play returns only offers the user is eligible for.

**Rule:** Pass `offerId` into `purchaseSubs` / `purchaseInApp` for trials/promos. Omitting buys the default paid offer.

```kotlin
// WRONG — skips trial
billingManager.purchaseSubs(activity, productId, planId)

// RIGHT
billingManager.purchaseSubs(activity, productId, planId, offerId = offer.offerId)
```

### Prefer-trial mapper

1. For each plan, find all `ProductDetail`s with matching `productId` + `planId`.
2. Prefer row with `RecurringMode.FREE` or non-empty `offerId`.
3. Else take default (empty `offerId`).
4. Store `offerId` on `SubscriptionOffer` — never hardcode offer IDs.

---

## 5. One-time in-app products

Register in `BillingDataSource.start()`:

```kotlin
billingManager
    .setSubscriptions(BillingProductIds.allSubscriptionProductIds)  // if any
    .setNonConsumables(BillingProductIds.nonConsumableProductIds)   // lifetime unlock
    .setConsumables(BillingProductIds.consumableProductIds)         // credits, etc.
```

Purchase from Fragment:

```kotlin
billingManager.purchaseInApp(activity, productId, offerId = offerId)
```

| Type                          | Entitlement                                                      |
|-------------------------------|------------------------------------------------------------------|
| **Non-consumable** (lifetime) | Sets `isAppPurchased = true` (same as subs)                      |
| **Consumable**                | Separate balance/flag unless product treats it as premium unlock |

Map prices from `productsState` — in-app rows have `productId` only (no `planId`).

---

## 6. Architecture

```
app (Application: start BillingDataSource)
  presentation  →  BillingManager.purchaseSubs / purchaseInApp (Activity-only)
  domain        →  BillingProductIds, BillingRepository, entities, use cases
  data          →  BillingDataSource + BillingRepositoryImpl
```

| Layer            | Owns                                                                    | Must not                       |
|------------------|-------------------------------------------------------------------------|--------------------------------|
| **Domain**       | `BillingProductIds`, entities, `BillingRepository`, use cases           | Import `com.hypersoft.billing` |
| **Data**         | Singleton `BillingManager`, `connect()`, mappers, sync `isAppPurchased` | Launch Play purchase UI        |
| **Presentation** | MVI paywalls, `LaunchSubsPurchase` / `LaunchInAppPurchase` effects      | Duplicate product IDs          |

### Domain files (typical)

```
domain/.../billing/BillingProductIds.kt
domain/.../entity/billing/PremiumPlan.kt
domain/.../entity/billing/SubscriptionOffer.kt
domain/.../entity/billing/InAppProduct.kt
domain/.../entity/billing/BillingPurchaseResult.kt
domain/.../repository/billing/BillingRepository.kt
domain/.../usecase/billing/ObserveSubscriptionOffersUseCase.kt
domain/.../usecase/billing/ObserveInAppProductsUseCase.kt
domain/.../usecase/sharedPref/IsAppPurchasedUseCase.kt
domain/.../usecase/sharedPref/SetAppPurchasedUseCase.kt
```

### BillingDataSource (data)

```kotlin
class BillingDataSource(
    context: Context,
    private val sharedPrefManager: SharedPrefManager,
) {
    val billingManager: BillingManager = BillingManager(context.applicationContext)

    fun start(scope: CoroutineScope) {
        billingManager
            .setSubscriptions(BillingProductIds.allSubscriptionProductIds)
            .setNonConsumables(BillingProductIds.nonConsumableProductIds)
            .setConsumables(BillingProductIds.consumableProductIds)

        scope.launch {
            val state = billingManager.connect()
            Log.d(TAG_BILLING, "BillingDataSource: start: Connection: $state")
            billingManager.purchasesState.collect { purchaseState ->
                when (purchaseState) {
                    is UiState.Success -> {
                        val hasPurchase = purchaseState.data.isNotEmpty()
                        sharedPrefManager.isAppPurchased = hasPurchase
                        Log.d(TAG_BILLING, "BillingDataSource: syncPurchases: Success: hasPurchase=$hasPurchase")
                    }
                    is UiState.Error ->
                        Log.e(TAG_BILLING, "BillingDataSource: syncPurchases: Failed: ${purchaseState.message}")
                    is UiState.Loading -> Unit
                }
            }
        }
    }
}
```

Koin:

```kotlin
single { BillingDataSource(androidContext(), get()) }
single { get<BillingDataSource>().billingManager }
single<BillingRepository> { BillingRepositoryImpl(billingDataSource = get()) }
```

Application after Koin:

```kotlin
koin.get<BillingDataSource>().start(applicationScope)
```

### Presentation MVI

- Subs: `LaunchSubsPurchase(productId, planId, offerId)`
- In-app: `LaunchInAppPurchase(productId, offerId?)`
- Fragment calls billing; maps `PurchaseOutcome` → `BillingPurchaseResult` → Intent
- DEBUG: fake success; RELEASE: real Play sheet

---

## 7. BillingManager v4 API

| API                                                  | Role                                  |
|------------------------------------------------------|---------------------------------------|
| `connectionState`                                    | DISCONNECTED / CONNECTING / CONNECTED |
| `productsState`                                      | Catalog + offers                      |
| `purchasesState`                                     | Owned purchases                       |
| `purchaseSubs(activity, productId, planId, offerId)` | Subscriptions                         |
| `purchaseInApp(activity, productId, offerId)`        | One-time products                     |
| `updateSubs(...)`                                    | Plan change                           |
| `refreshProducts()` / `refreshPurchases()`           | Force re-fetch                        |

`PurchaseOutcome`: `Success` / `AlreadyOwned` / `UserCancelled` / `Failed(message)`.

### 3.x → 4.0.0 migration

| 3.x                                        | 4.0.0                                      |
|--------------------------------------------|--------------------------------------------|
| `BillingManager(context, scope)`           | `BillingManager(context)`                  |
| `setListener(…).startConnection()`         | `connect()`; observe `connectionState`     |
| `fetchProductDetails(listener)`            | `productsState`                            |
| `fetchPurchaseHistory(listener)`           | `purchasesState`                           |
| `purchaseSubs(…, BillingPurchaseListener)` | `suspend purchaseSubs(…): PurchaseOutcome` |
| `com.hypersoft.billing.data.entities.*`    | `com.hypersoft.billing.model.*`            |

---

## 8. Pitfalls

| Symptom                         | Cause                                                       |
|---------------------------------|-------------------------------------------------------------|
| Sheet has no free trial         | `offerId` null/omitted                                      |
| “Product unavailable”           | ID mismatch, app not on tester track, `productsState` empty |
| Premium lost after reinstall    | Not collecting `purchasesState`                             |
| Ads after purchase              | Ads not reading `isAppPurchased`                            |
| Duplicate BillingClient crashes | Multiple `BillingManager` instances                         |
| In-app price missing            | Product not in `setNonConsumables` / `setConsumables`       |

Never log purchase tokens or PII.

---

## 9. Checklist

**Gradle:** JitPack, catalog `4.0.0+`, deps on `:data` + `:presentation`, `TAG_BILLING`

**Console:** Option 1 IDs, trial offers active, license testers, in-app products created

**Code:** `BillingProductIds` single source, singleton + Application connect, offerId in purchase calls, ads gated, fake purchase debug only, strings in `:core-ui`, portrait + landscape

**QA:** Trial sheet matches UI, restore works, cancel no error toast
