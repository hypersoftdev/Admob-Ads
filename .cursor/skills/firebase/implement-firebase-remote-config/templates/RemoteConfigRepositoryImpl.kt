package YOUR.PACKAGE.data.remoteConfig.repository

import android.util.Log
import YOUR.PACKAGE.core.common.Constants.TAG_REMOTE_CONFIG
import YOUR.PACKAGE.core.platform.firebase.PlatformFirebase.recordException
import YOUR.PACKAGE.core.platform.network.InternetManager
import YOUR.PACKAGE.data.remoteConfig.dataSource.RemoteConfigDataSource
import YOUR.PACKAGE.data.sharedPreferences.dataSource.SharedPrefManager
import YOUR.PACKAGE.domain.repository.remoteConfig.RemoteConfigRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Template — copy to `:data` …/remoteConfig/repository/
 * Replace `YOUR.PACKAGE`. Fill [saveValues] from the keys confirmed in Step 0.
 * Cache only when fetch activates; last prefs remain if fetch fails or offline.
 */
class RemoteConfigRepositoryImpl(
    private val remoteConfigDataSource: RemoteConfigDataSource,
    private val sharedPrefManager: SharedPrefManager,
    private val internetManager: InternetManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RemoteConfigRepository {

    private var listenerRegistered: Boolean = false

    override suspend fun fetchAndCache(): Boolean = withContext(ioDispatcher) {
        if (!internetManager.isInternetAvailable()) {
            Log.w(TAG_REMOTE_CONFIG, "RemoteConfigRepositoryImpl: fetchAndCache: Failed: no network")
            return@withContext false
        }

        val activated = remoteConfigDataSource.fetchAndActivate()
        if (activated) {
            saveValues()
            registerListenerIfNeeded()
            Log.d(TAG_REMOTE_CONFIG, "RemoteConfigRepositoryImpl: fetchAndCache: Success: fetched")
        }
        activated
    }

    private fun registerListenerIfNeeded() {
        if (listenerRegistered) return
        listenerRegistered = true
        remoteConfigDataSource.addConfigUpdateListener { saveValues() }
    }

    private fun saveValues() {
        sharedPrefManager.apply {
            try {
                // rcExampleFlag = remoteConfigDataSource.getInt(exampleFlagKey)
                Log.d(TAG_REMOTE_CONFIG, "RemoteConfigRepositoryImpl: saveValues: Success")
            } catch (ex: Exception) {
                Log.e(TAG_REMOTE_CONFIG, "RemoteConfigRepositoryImpl: saveValues: Failed: ${ex.message}")
                ex.recordException("RemoteConfigRepositoryImpl: saveValues")
            }
        }
    }
}
