package com.hypersoft.admobads.helpers.managers

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.hypersoft.admobads.helpers.firebase.FirebaseUtils.recordException

class InternetManager(private val connectivityManager: ConnectivityManager) {

    val isInternetConnected: Boolean
        get() {
            try {
                val network = connectivityManager.activeNetwork ?: return false
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                return when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } catch (ex: Exception) {
                ex.recordException("Internet Manager")
                return false
            }
        }
}