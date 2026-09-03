package com.hypersoft.ads.practice.data.remoteConfig.dataSource

import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.hypersoft.ads.practice.core.Constants.TAG_REMOTE_CONFIG
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class RemoteConfigDataSource {

    private val fetchMutex = Mutex()

    private val remoteConfig: FirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    suspend fun fetchAndActivate(): Boolean = fetchMutex.withLock {
        return try {
            val settings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0L)
                .build()
            remoteConfig.setConfigSettingsAsync(settings).await()
            remoteConfig.setDefaultsAsync(DEFAULTS).await()
            val activated = remoteConfig.fetchAndActivate().await()
            Log.d(TAG_REMOTE_CONFIG, "RemoteConfigDataSource: fetchAndActivate: Success: activated=$activated")
            activated
        } catch (error: Exception) {
            Log.e(TAG_REMOTE_CONFIG, "RemoteConfigDataSource: fetchAndActivate: Failed: ${error.message}")
            false
        }
    }

    fun addConfigUpdateListener(onUpdated: () -> Unit) {
        remoteConfig.addOnConfigUpdateListener(
            object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    remoteConfig.activate().addOnCompleteListener {
                        Log.d(TAG_REMOTE_CONFIG, "RemoteConfigDataSource: onUpdate: Success: updated")
                        onUpdated()
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    Log.e(TAG_REMOTE_CONFIG, "RemoteConfigDataSource: onError: Failed: ${error.message}")
                }
            },
        )
    }

    fun getLong(key: String, default: Long): Long = runCatching { remoteConfig.getLong(key) }.getOrDefault(default)

    fun getInt(key: String): Int = runCatching { remoteConfig.getLong(key).toInt() }.getOrDefault(0)

    fun getBoolean(key: String, default: Boolean): Boolean = runCatching { remoteConfig.getBoolean(key) }.getOrDefault(default)

    fun getString(key: String, default: String): String = runCatching { remoteConfig.getString(key) }.getOrDefault(default).ifEmpty { default }

    private companion object {
        val DEFAULTS = mapOf(
            "bannerLanguage" to 2,
            "bannerOnBoarding" to 2,
            "bannerDashboard" to 2,
            "bannerFeatureOne" to 1,
            "bannerFeatureTwo" to 1,

            "interEntrance" to 1,
            "interOnboarding" to 1,
            "interHome" to 1,
            "interBottomNavigation" to 1,
            "interBackPress" to 1,
            "interExit" to 1,

            "rewardedHome" to 1,

            "rewardedInterHome" to 1,

            "nativeLanguage" to 1,
            "nativeOnBoarding" to 1,
            "nativeMenu" to 1,
            "nativeHome" to 1,
            "nativeTrending" to 1,
            "nativeSetting" to 1,
            "nativeFeatureOne" to 1,
            "nativeFeatureTwo" to 1,

            "counterInterHome" to 5,
            "counterInterBottomNavigation" to 5,
            "counterInterBackPress" to 5,

            "showPremiumFirstTime" to 0,
            "showPremiumSecondTime" to 0,
        )
    }
}