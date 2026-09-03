package com.hypersoft.ads.practice.data.sharedPreferences.repository

interface SharedPrefRepository {
    suspend fun isFirstTimeUser(): Boolean
    suspend fun setFirstTimeUser(isFirstTime: Boolean)
    suspend fun isLanguageSelected(): Boolean
    suspend fun setLanguageSelected(selected: Boolean)
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun getThemeMode(): String
    suspend fun setThemeMode(themeMode: String)

    suspend fun isAppPurchased(): Boolean
    suspend fun setAppPurchased(purchased: Boolean)
    suspend fun isFirstTimePremiumEnabled(): Boolean
    suspend fun isSecondTimePremiumEnabled(): Boolean
    suspend fun remoteCounterBottomNavigation(): Int
}