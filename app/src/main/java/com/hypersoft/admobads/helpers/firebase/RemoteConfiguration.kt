package com.hypersoft.admobads.helpers.firebase

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.hypersoft.admobads.R
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.BANNER_COLLAPSIBLE_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.BANNER_HOME_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.INTER_MAIN_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.INTER_SPLASH_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.NATIVE_HOME_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.NATIVE_SAMPLE_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.NATIVE_SPLASH_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.OPEN_APP_AD_KEY
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.REMOTE_COUNTER_KEY
import com.hypersoft.admobads.helpers.managers.InternetManager
import com.hypersoft.admobads.helpers.firebase.FirebaseUtils.recordException

class RemoteConfiguration(private val internetManager: InternetManager) {

    private val configTag = "REMOTE_CONFIG"
    private val remoteConfig = Firebase.remoteConfig

    fun checkRemoteConfig(callback: (fetchSuccessfully: Boolean) -> Unit) {
        if (internetManager.isInternetConnected) {
            val configSettings = remoteConfigSettings { minimumFetchIntervalInSeconds = 2 }
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            fetchRemoteValues(callback)
        } else {
            callback.invoke(false)
        }
    }

    private fun fetchRemoteValues(callback: (fetchSuccessfully: Boolean) -> Unit) {
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
        }
    }

    @Throws(Exception::class)
    private fun updateRemoteValues(callback: (fetchSuccessfully: Boolean) -> Unit) {
        RemoteConstants.rcvInterSplash = remoteConfig[INTER_SPLASH_KEY].asLong().toInt()
        RemoteConstants.rcvInterMain = remoteConfig[INTER_MAIN_KEY].asLong().toInt()
        RemoteConstants.rcvNativeSplash = remoteConfig[NATIVE_SPLASH_KEY].asLong().toInt()
        RemoteConstants.rcvNativeHome = remoteConfig[NATIVE_HOME_KEY].asLong().toInt()
        RemoteConstants.rcvNativeSample = remoteConfig[NATIVE_SAMPLE_KEY].asLong().toInt()
        RemoteConstants.rcvBannerHome = remoteConfig[BANNER_HOME_KEY].asLong().toInt()
        RemoteConstants.rcvBannerCollapsible = remoteConfig[BANNER_COLLAPSIBLE_KEY].asLong().toInt()
        RemoteConstants.rcvOpenApp = remoteConfig[OPEN_APP_AD_KEY].asLong().toInt()
        RemoteConstants.rcvRemoteCounter = remoteConfig[REMOTE_COUNTER_KEY].asLong().toInt()

        Log.d(configTag, "checkRemoteConfig: Fetched Successfully")
        callback.invoke(true)
    }
}