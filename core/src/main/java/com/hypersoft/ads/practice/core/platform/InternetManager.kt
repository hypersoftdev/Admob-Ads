package com.hypersoft.ads.practice.core.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.hypersoft.ads.practice.core.Constants.TAG

class InternetManager(
    context: Context,
) {
    private val appContext = context.applicationContext

    val isInternetConnected: Boolean
        get() {
            val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager == null) {
                Log.w(TAG, "InternetManager: isInternetConnected: Failed: ConnectivityManager null")
                return false
            }
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            val connected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            Log.d(TAG, "InternetManager: isInternetConnected: Success: connected=$connected")
            return connected
        }
}