package com.hypersoft.admobads.utilities.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.hypersoft.admobads.utilities.firebase.FirebaseUtils.recordException
import com.hypersoft.admobads.utilities.manager.InternetManager
import com.hypersoft.admobads.utilities.manager.SharedPreferencesUtils
import com.hypersoft.admobads.utilities.utils.Constants.TAG
import com.hypersoft.admobads.utilities.utils.Constants.TAG_REMOTE

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class RemoteConfiguration(private val internetManager: InternetManager, private val sharedPreferencesUtils: SharedPreferencesUtils) {

    private val remoteConfig: FirebaseRemoteConfig by lazy { Firebase.remoteConfig }

    init {
        // Set minimum fetch interval to 0 for real-time updates
        val configSettings = remoteConfigSettings {
            fetchTimeoutInSeconds = 10
            minimumFetchIntervalInSeconds = 0L
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    fun checkRemoteConfig(fetchCallback: (Boolean) -> Unit) {
        var callback: ((Boolean) -> Unit)? = fetchCallback
        if (!internetManager.isInternetConnected) {
            Log.e(TAG_REMOTE, "RemoteConfiguration: checkRemoteConfig: No Internet Found")
            fetchCallback.invoke(false)
            callback = null
        }

        addLiveUpdateListener()
        fetchRemoteValues(callback)
    }

    private fun addLiveUpdateListener() {
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.d(TAG_REMOTE, "RemoteConfiguration: addLiveUpdateListener: onUpdate: Fetched Successfully")
                remoteConfig.activate().addOnSuccessListener { isSuccessful ->
                    if (isSuccessful) updateRemoteValues()
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.e(TAG_REMOTE, "RemoteConfiguration: addLiveUpdateListener: Error: ", error)
            }
        })
    }

    private fun fetchRemoteValues(callback: ((Boolean) -> Unit)?) {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            when (task.isSuccessful) {
                true -> updateRemoteValues()
                false -> Log.e(TAG_REMOTE, "RemoteConfiguration: fetchRemoteValues: Failure: ", task.exception)
            }
            callback?.invoke(task.isSuccessful)
        }
    }

    private fun updateRemoteValues() {
        // Save this value anywhere
        sharedPreferencesUtils.apply {
            try {
                rcAppOpen = remoteConfig[appOpen].asLong().toInt()
                rcAppOpenSplash = remoteConfig[appOpenSplash].asLong().toInt()

                rcBannerHome = remoteConfig[bannerHome].asLong().toInt()

                rcInterOnBoarding = remoteConfig[interOnBoarding].asLong().toInt()
                rcInterFeature = remoteConfig[interFeature].asLong().toInt()

                rcNativeLanguage = remoteConfig[nativeLanguage].asLong().toInt()
                rcNativeOnBoarding = remoteConfig[nativeOnBoarding].asLong().toInt()
                rcNativeHome = remoteConfig[nativeHome].asLong().toInt()
                rcNativeFeature = remoteConfig[nativeFeature].asLong().toInt()
                rcNativeExit = remoteConfig[nativeExit].asLong().toInt()

                Log.i(TAG_REMOTE, "RemoteConfiguration: rcAppOpen -> ${remoteConfig[appOpen].asLong().toInt()}")
                Log.i(TAG_REMOTE, "RemoteConfiguration: rcAppOpenSplash -> ${remoteConfig[appOpenSplash].asLong().toInt()}")

                Log.i(TAG_REMOTE, "RemoteConfiguration: rcBannerHome -> ${remoteConfig[bannerHome].asLong().toInt()}")

                Log.i(TAG_REMOTE, "RemoteConfiguration: rcInterOnBoarding -> ${remoteConfig[interOnBoarding].asLong().toInt()}")
                Log.i(TAG_REMOTE, "RemoteConfiguration: rcInterFeature -> ${remoteConfig[interFeature].asLong().toInt()}")

                Log.i(TAG_REMOTE, "RemoteConfiguration: rcNativeLanguage -> ${remoteConfig[nativeLanguage].asLong().toInt()}")
                Log.i(TAG_REMOTE, "RemoteConfiguration: rcNativeOnBoarding -> ${remoteConfig[nativeOnBoarding].asLong().toInt()}")
                Log.i(TAG_REMOTE, "RemoteConfiguration: rcNativeHome -> ${remoteConfig[nativeHome].asLong().toInt()}")
                Log.i(TAG_REMOTE, "RemoteConfiguration: rcNativeFeature -> ${remoteConfig[nativeFeature].asLong().toInt()}")
                Log.i(TAG_REMOTE, "RemoteConfiguration: rcNativeExit -> ${remoteConfig[nativeExit].asLong().toInt()}")
            } catch (ex: Exception) {
                ex.recordException("RemoteConfiguration: updateRemoteValues")
            }
        }
        Log.d(TAG, "RemoteConfiguration: updateRemoteValues: Fetched Successfully")
    }
}