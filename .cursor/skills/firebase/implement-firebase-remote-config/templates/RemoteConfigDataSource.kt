package YOUR.PACKAGE.data.remoteConfig.dataSource

import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import YOUR.PACKAGE.core.common.Constants.TAG_REMOTE_CONFIG
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/**
 * Template — copy to `:data` …/remoteConfig/dataSource/
 * Replace `YOUR.PACKAGE` with the applicationId root.
 * Requires `kotlinx-coroutines-play-services` on `:data`.
 */
class RemoteConfigDataSource {

    private val fetchMutex = Mutex()

    private val remoteConfig: FirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    suspend fun fetchAndActivate(): Boolean = fetchMutex.withLock {
        return try {
            val settings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0L)
                .build()
            remoteConfig.setConfigSettingsAsync(settings).await()
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

    fun getString(key: String, default: String): String =
        runCatching { remoteConfig.getString(key) }.getOrDefault(default).ifEmpty { default }
}
