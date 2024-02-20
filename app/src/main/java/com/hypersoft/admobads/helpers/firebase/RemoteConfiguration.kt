package com.hypersoft.admobads.helpers.firebase

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.hypersoft.admobads.R
import com.hypersoft.admobads.helpers.firebase.FirebaseUtils.recordException
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.APP_OPEN_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.BANNER_AD_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.COUNTER_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.INTERSTITIAL_AD_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.NATIVE_AD_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.REWARDED_AD_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.REWARDED_INTER_AD_KEY
import com.hypersoft.admobads.helpers.managers.InternetManager

class RemoteConfiguration(private val internetManager: InternetManager, private val sharedPreferences: SharedPreferences) {

    private val configTag = "TAG_REMOTE_CONFIG"

    fun checkRemoteConfig(callback: (fetchSuccessfully: Boolean) -> Unit) {
        if (internetManager.isInternetConnected) {
            val remoteConfig = Firebase.remoteConfig
            val configSettings = com.google.firebase.remoteconfig.remoteConfigSettings {
                minimumFetchIntervalInSeconds = 2
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            fetchRemoteValues(callback)
        } else {
            Log.d(configTag, "checkRemoteConfig: Internet Not Found!")
            callback.invoke(false)
        }
    }

    private fun fetchRemoteValues(callback: (fetchSuccessfully: Boolean) -> Unit) {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                try {
                    updateRemoteValues(callback)
                } catch (ex: Exception) {
                    ex.recordException("RemoteConfiguration > fetchRemoteValues")
                    Log.d(configTag, "fetchRemoteValues: ${it.exception}")
                    callback.invoke(false)
                }
            } else {
                Log.d(configTag, "fetchRemoteValues: ${it.exception}")
                callback.invoke(false)
            }
        }.addOnFailureListener {
            Log.d(configTag, "fetchRemoteValues: ${it.message}")
            callback.invoke(false)
        }
    }

    @Throws(Exception::class)
    private fun updateRemoteValues(callback: (fetchSuccessfully: Boolean) -> Unit) {
        val remoteConfig = Firebase.remoteConfig

        setPrefRemoteValues(remoteConfig)
        getPrefRemoteValues()
        Log.d(configTag, "checkRemoteConfig: Fetched Successfully")
        callback.invoke(true)
    }

    fun getPrefRemoteValues() {
        /**
         * Interstitial
         */
        RemoteConstants.rcvInterAd = sharedPreferences.getInt(INTERSTITIAL_AD_KEY, 1)

        /**
         * Rewarded
         */
        RemoteConstants.rcvRewardAd = sharedPreferences.getInt(REWARDED_AD_KEY, 1)
        RemoteConstants.rcvRewardInterAd = sharedPreferences.getInt(REWARDED_INTER_AD_KEY, 1)

        /**
         * Native
         */
        RemoteConstants.rcvNativeAd = sharedPreferences.getInt(NATIVE_AD_KEY, 1)

        /**
         * Banner
         */
        RemoteConstants.rcvBannerAd = sharedPreferences.getInt(BANNER_AD_KEY, 1)

        /**
         * OpenApp
         */
        RemoteConstants.rcvAppOpen = sharedPreferences.getInt(APP_OPEN_KEY, 1)

        /**
         * Others
         */
        RemoteConstants.rcvRemoteCounter = sharedPreferences.getInt(COUNTER_KEY, 3)

        RemoteConstants.totalCount = RemoteConstants.rcvRemoteCounter

    }

    @Throws(Exception::class)
    private fun setPrefRemoteValues(remoteConfig: FirebaseRemoteConfig) {
        sharedPreferences.edit().apply {
            /**
             * Interstitial Remote Config
             */
            putInt(INTERSTITIAL_AD_KEY, remoteConfig[INTERSTITIAL_AD_KEY].asLong().toInt())

            /**
             * Rewarded Remote Config
             */
            putInt(REWARDED_AD_KEY, remoteConfig[REWARDED_AD_KEY].asLong().toInt())
            putInt(REWARDED_INTER_AD_KEY, remoteConfig[REWARDED_INTER_AD_KEY].asLong().toInt())

            /**
             * Native Remote Config
             */
            putInt(NATIVE_AD_KEY, remoteConfig[NATIVE_AD_KEY].asLong().toInt())

            /**
             * Banner Remote Config
             */
            putInt(BANNER_AD_KEY, remoteConfig[BANNER_AD_KEY].asLong().toInt())

            /**
             * OpenApp Remote Config
             */
            putInt(APP_OPEN_KEY, remoteConfig[APP_OPEN_KEY].asLong().toInt())

            /**
             * Others Remote Config
             */
            putInt(COUNTER_KEY, remoteConfig[COUNTER_KEY].asLong().toInt())

            apply()
        }
    }
}