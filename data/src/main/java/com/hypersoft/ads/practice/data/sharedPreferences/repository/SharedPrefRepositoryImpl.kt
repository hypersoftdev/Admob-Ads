package com.hypersoft.ads.practice.data.sharedPreferences.repository

import android.util.Log
import com.hypersoft.ads.practice.core.Constants.TAG
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SharedPrefRepositoryImpl(
    private val sharedPrefManager: SharedPrefManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SharedPrefRepository {

    override suspend fun isFirstTimeUser(): Boolean = withContext(ioDispatcher) {
        sharedPrefManager.isFirstTimeUser
    }

    override suspend fun setFirstTimeUser(isFirstTime: Boolean) = withContext(ioDispatcher) {
        sharedPrefManager.isFirstTimeUser = isFirstTime
    }

    override suspend fun isLanguageSelected(): Boolean = withContext(ioDispatcher) {
        sharedPrefManager.isLanguageSelected
    }

    override suspend fun setLanguageSelected(selected: Boolean) = withContext(ioDispatcher) {
        sharedPrefManager.isLanguageSelected = selected
    }

    override suspend fun isOnboardingCompleted(): Boolean = withContext(ioDispatcher) {
        sharedPrefManager.isOnboardingCompleted
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) = withContext(ioDispatcher) {
        sharedPrefManager.isOnboardingCompleted = completed
        if (completed) {
            sharedPrefManager.isFirstTimeUser = false
        }
    }

    override suspend fun getThemeMode(): String = withContext(ioDispatcher) {
        sharedPrefManager.themeMode
    }

    override suspend fun setThemeMode(themeMode: String): Unit = withContext(ioDispatcher) {
        sharedPrefManager.themeMode = themeMode
        Log.d(TAG, "SharedPrefRepositoryImpl: setThemeMode: Success: mode=$themeMode")
    }

    override suspend fun isAppPurchased(): Boolean = withContext(ioDispatcher) {
        sharedPrefManager.isAppPurchased
    }

    override suspend fun setAppPurchased(purchased: Boolean): Unit = withContext(ioDispatcher) {
        sharedPrefManager.isAppPurchased = purchased
        Log.d(TAG, "SharedPrefRepositoryImpl: setAppPurchased: Success: purchased=$purchased")
    }

    override suspend fun isFirstTimePremiumEnabled(): Boolean = withContext(ioDispatcher) {
        sharedPrefManager.rcShowPremiumFirstTime != 0
    }

    override suspend fun isSecondTimePremiumEnabled(): Boolean = withContext(ioDispatcher) {
        sharedPrefManager.rcShowPremiumSecondTime != 0
    }

    override suspend fun remoteCounterBottomNavigation(): Int = withContext(ioDispatcher) {
        sharedPrefManager.rcCounterInterBottomNavigation
    }
}