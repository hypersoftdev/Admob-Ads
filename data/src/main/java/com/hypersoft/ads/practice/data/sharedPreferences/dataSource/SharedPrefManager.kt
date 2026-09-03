package com.hypersoft.ads.practice.data.sharedPreferences.dataSource

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPrefManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isFirstTimeUser: Boolean
        get() = sharedPreferences.getBoolean(KEY_FIRST_TIME_USER, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_FIRST_TIME_USER, value) }

    var isLanguageSelected: Boolean
        get() = sharedPreferences.getBoolean(KEY_LANGUAGE_SELECTED, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_LANGUAGE_SELECTED, value) }

    var isOnboardingCompleted: Boolean
        get() = sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_ONBOARDING_COMPLETED, value) }

    var themeMode: String
        get() = sharedPreferences.getString(KEY_THEME_MODE, DEFAULT_THEME_MODE) ?: DEFAULT_THEME_MODE
        set(value) = sharedPreferences.edit { putString(KEY_THEME_MODE, value) }

    /* ------------------------------------------- Premium ------------------------------------------- */

    var isAppPurchased: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_APP_PURCHASED, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_IS_APP_PURCHASED, value) }

    /* ---------------------------------------- Ads ---------------------------------------- */

    val appOpen = "appOpen"
    val appOpenEntrance = "appOpenEntrance"

    val bannerLanguage = "bannerLanguage"
    val bannerOnBoarding = "bannerOnBoarding"
    val bannerDashboard = "bannerDashboard"
    val bannerFeatureOne = "bannerFeatureOne"
    val bannerFeatureTwo = "bannerFeatureTwo"

    val interEntrance = "interEntrance"
    val interOnboarding = "interOnboarding"
    val interHome = "interHome"
    val interBottomNavigation = "interBottomNavigation"
    val interBackPress = "interBackPress"
    val interExit = "interExit"

    val rewardedHome = "rewardedHome"
    val rewardedInterHome = "rewardedInterHome"

    val nativeLanguage = "nativeLanguage"
    val nativeOnBoarding = "nativeOnBoarding"
    val nativeMenu = "nativeMenu"
    val nativeHome = "nativeHome"
    val nativeTrending = "nativeTrending"
    val nativeSetting = "nativeSetting"
    val nativeFeatureOne = "nativeFeatureOne"
    val nativeFeatureTwo = "nativeFeatureTwo"

    val counterInterHome = "counterInterHome"
    val counterInterBottomNavigation = "counterInterBottomNavigation"
    val counterInterBackPress = "counterInterBackPress"

    val showPremiumFirstTime = "showPremiumFirstTime"
    val showPremiumSecondTime = "showPremiumSecondTime"

    /* ----- App Open Ads ----- */

    var rcAppOpen: Int
        get() = sharedPreferences.getInt(appOpen, 1)
        set(value) = sharedPreferences.edit { putInt(appOpen, value) }

    var rcAppOpenEntrance: Int
        get() = sharedPreferences.getInt(appOpenEntrance, 1)
        set(value) = sharedPreferences.edit { putInt(appOpenEntrance, value) }

    /* ----- Banner Ads ----- */

    var rcBannerLanguage: Int
        get() = sharedPreferences.getInt(bannerLanguage, 2)
        set(value) = sharedPreferences.edit { putInt(bannerLanguage, value) }

    var rcBannerOnBoarding: Int
        get() = sharedPreferences.getInt(bannerOnBoarding, 2)
        set(value) = sharedPreferences.edit { putInt(bannerOnBoarding, value) }

    var rcBannerDashboard: Int
        get() = sharedPreferences.getInt(bannerDashboard, 2)
        set(value) = sharedPreferences.edit { putInt(bannerDashboard, value) }

    var rcBannerFeatureOne: Int
        get() = sharedPreferences.getInt(bannerFeatureOne, 1)
        set(value) = sharedPreferences.edit { putInt(bannerFeatureOne, value) }

    var rcBannerFeatureTwo: Int
        get() = sharedPreferences.getInt(bannerFeatureTwo, 1)
        set(value) = sharedPreferences.edit { putInt(bannerFeatureTwo, value) }

    /* ----- Interstitial Ads ----- */

    var rcInterEntrance: Int
        get() = sharedPreferences.getInt(interEntrance, 1)
        set(value) = sharedPreferences.edit { putInt(interEntrance, value) }

    var rcInterOnboarding: Int
        get() = sharedPreferences.getInt(interOnboarding, 1)
        set(value) = sharedPreferences.edit { putInt(interOnboarding, value) }

    var rcInterHome: Int
        get() = sharedPreferences.getInt(interHome, 1)
        set(value) = sharedPreferences.edit { putInt(interHome, value) }

    var rcInterBottomNavigation: Int
        get() = sharedPreferences.getInt(interBottomNavigation, 1)
        set(value) = sharedPreferences.edit { putInt(interBottomNavigation, value) }

    var rcInterBackPress: Int
        get() = sharedPreferences.getInt(interBackPress, 1)
        set(value) = sharedPreferences.edit { putInt(interBackPress, value) }

    var rcInterExit: Int
        get() = sharedPreferences.getInt(interExit, 1)
        set(value) = sharedPreferences.edit { putInt(interExit, value) }

    /* ----- Rewarded Ads ----- */

    var rcRewardedHome: Int
        get() = sharedPreferences.getInt(rewardedHome, 1)
        set(value) = sharedPreferences.edit { putInt(rewardedHome, value) }

    var rcRewardedInterHome: Int
        get() = sharedPreferences.getInt(rewardedInterHome, 1)
        set(value) = sharedPreferences.edit { putInt(rewardedInterHome, value) }

    /* ----- Native Ads ----- */

    var rcNativeLanguage: Int
        get() = sharedPreferences.getInt(nativeLanguage, 1)
        set(value) = sharedPreferences.edit { putInt(nativeLanguage, value) }

    var rcNativeOnBoarding: Int
        get() = sharedPreferences.getInt(nativeOnBoarding, 1)
        set(value) = sharedPreferences.edit { putInt(nativeOnBoarding, value) }

    var rcNativeMenu: Int
        get() = sharedPreferences.getInt(nativeMenu, 1)
        set(value) = sharedPreferences.edit { putInt(nativeMenu, value) }

    var rcNativeHome: Int
        get() = sharedPreferences.getInt(nativeHome, 1)
        set(value) = sharedPreferences.edit { putInt(nativeHome, value) }

    var rcNativeTrending: Int
        get() = sharedPreferences.getInt(nativeTrending, 1)
        set(value) = sharedPreferences.edit { putInt(nativeTrending, value) }

    var rcNativeSetting: Int
        get() = sharedPreferences.getInt(nativeSetting, 1)
        set(value) = sharedPreferences.edit { putInt(nativeSetting, value) }

    var rcNativeFeatureOne: Int
        get() = sharedPreferences.getInt(nativeFeatureOne, 1)
        set(value) = sharedPreferences.edit { putInt(nativeFeatureOne, value) }

    var rcNativeFeatureTwo: Int
        get() = sharedPreferences.getInt(nativeFeatureTwo, 1)
        set(value) = sharedPreferences.edit { putInt(nativeFeatureTwo, value) }

    /* ----- Counter ----- */

    var rcCounterInterHome: Int
        get() = sharedPreferences.getInt(counterInterHome, 5)
        set(value) = sharedPreferences.edit { putInt(counterInterHome, value) }

    var rcCounterInterBottomNavigation: Int
        get() = sharedPreferences.getInt(counterInterBottomNavigation, 5)
        set(value) = sharedPreferences.edit { putInt(counterInterBottomNavigation, value) }

    var rcCounterInterBackPress: Int
        get() = sharedPreferences.getInt(counterInterBackPress, 5)
        set(value) = sharedPreferences.edit { putInt(counterInterBackPress, value) }

    var rcShowPremiumFirstTime: Int
        get() = sharedPreferences.getInt(showPremiumFirstTime, 0)
        set(value) = sharedPreferences.edit { putInt(showPremiumFirstTime, value) }

    var rcShowPremiumSecondTime: Int
        get() = sharedPreferences.getInt(showPremiumSecondTime, 0)
        set(value) = sharedPreferences.edit { putInt(showPremiumSecondTime, value) }

    private companion object {
        const val PREF_NAME = "ads_practice_prefs"

        const val KEY_FIRST_TIME_USER = "is_first_time_user"
        const val KEY_LANGUAGE_SELECTED = "is_language_selected"
        const val KEY_ONBOARDING_COMPLETED = "is_onboarding_completed"
        const val KEY_IS_APP_PURCHASED = "is_app_purchased"

        const val KEY_THEME_MODE = "theme_mode"
        const val DEFAULT_THEME_MODE = "SYSTEM"
    }
}