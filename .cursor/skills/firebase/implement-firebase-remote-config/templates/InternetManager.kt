package YOUR.PACKAGE.core.platform.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import YOUR.PACKAGE.core.common.Constants.TAG

/**
 * Template — copy to `:core-platform` …/network/ if missing.
 * Replace `YOUR.PACKAGE` with the applicationId root.
 */
class InternetManager(
    private val context: Context,
) {
    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            Log.w(TAG, "InternetManager: isInternetAvailable: Failed: ConnectivityManager null")
            return false
        }
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        val available = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        Log.d(TAG, "InternetManager: isInternetAvailable: Success: available=$available")
        return available
    }
}
