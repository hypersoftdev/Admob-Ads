package com.hypersoft.admobads.helpers.preferences

import android.content.SharedPreferences

private const val billingRequireKey = "isAppPurchased"
private const val isShowFirstScreenKey = "showFirstScreen"

class SharedPreferenceUtils(private val sharedPreferences: SharedPreferences) {

    /* ---------- Billing ---------- */

    var isAppPurchased: Boolean
        get() = sharedPreferences.getBoolean(billingRequireKey, false)
        set(value) {
            sharedPreferences.edit().apply {
                putBoolean(billingRequireKey, value)
                apply()
            }
        }

    /* ---------- UI ---------- */

    var showFirstScreen: Boolean
        get() = sharedPreferences.getBoolean(isShowFirstScreenKey, true)
        set(value) {
            sharedPreferences.edit().apply {
                putBoolean(isShowFirstScreenKey, value)
                apply()
            }
        }
}