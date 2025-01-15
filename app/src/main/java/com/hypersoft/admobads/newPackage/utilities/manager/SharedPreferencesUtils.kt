package com.hypersoft.admobads.newPackage.utilities.manager

import android.content.SharedPreferences

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class SharedPreferencesUtils(private val sharedPreferences: SharedPreferences) {

    private val billingRequireKey = "isAppPurchased"
    private val isShowFirstScreenKey = "showFirstScreen"

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

    /* ---------------------------------------- Ads ---------------------------------------- */

    val nativeLanguage = "nativeLanguage"
    val nativeOnBoarding = "nativeOnBoarding"
    val nativeFeature = "nativeFeature"
    val nativeHome = "nativeHome"
    val nativeExit = "nativeExit"

    var rcNativeLanguage: Int
        get() = sharedPreferences.getInt(nativeLanguage, 1)
        set(value) {
            sharedPreferences.edit().apply {
                putInt(nativeLanguage, value)
                apply()
            }
        }

    var rcNativeOnBoarding: Int
        get() = sharedPreferences.getInt(nativeOnBoarding, 0)
        set(value) {
            sharedPreferences.edit().apply {
                putInt(nativeOnBoarding, value)
                apply()
            }
        }

    var rcNativeHome: Int
        get() = sharedPreferences.getInt(nativeHome, 0)
        set(value) {
            sharedPreferences.edit().apply {
                putInt(nativeHome, value)
                apply()
            }
        }

    var rcNativeFeature: Int
        get() = sharedPreferences.getInt(nativeFeature, 0)
        set(value) {
            sharedPreferences.edit().apply {
                putInt(nativeFeature, value)
                apply()
            }
        }

    var rcNativeExit: Int
        get() = sharedPreferences.getInt(nativeExit, 0)
        set(value) {
            sharedPreferences.edit().apply {
                putInt(nativeExit, value)
                apply()
            }
        }
}