package com.hypersoft.admobads.ads.natives.domain.useCases

import android.content.Context
import android.util.Log
import com.hypersoft.admobads.R
import com.hypersoft.admobads.ads.natives.data.entities.ItemNativeAd
import com.hypersoft.admobads.ads.natives.data.repositories.RepositoryNativeImpl
import com.hypersoft.admobads.ads.natives.presentation.enums.NativeAdKey
import com.hypersoft.admobads.utilities.manager.InternetManager
import com.hypersoft.admobads.utilities.manager.SharedPreferencesUtils
import com.hypersoft.admobads.utilities.utils.Constants.TAG_ADS

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class UseCaseNative(
    private val repositoryNativeImpl: RepositoryNativeImpl,
    private val sharedPreferenceUtils: SharedPreferencesUtils,
    private val internetManager: InternetManager,
    private val context: Context
) {

    @Volatile
    private var isAdLoading = false

    private fun checkRemoteConfig(nativeAdKey: NativeAdKey): Boolean {
        return when (nativeAdKey) {
            NativeAdKey.LANGUAGE -> sharedPreferenceUtils.rcNativeLanguage != 0
            NativeAdKey.ON_BOARDING -> sharedPreferenceUtils.rcNativeOnBoarding != 0
            NativeAdKey.HOME -> sharedPreferenceUtils.rcNativeHome != 0
            NativeAdKey.FEATURE -> sharedPreferenceUtils.rcNativeFeature != 0
            NativeAdKey.Settings -> sharedPreferenceUtils.rcNativeExit != 0
        }
    }

    private fun getAdId(nativeAdKey: NativeAdKey): String {
        return when (nativeAdKey) {
            NativeAdKey.LANGUAGE -> context.getString(R.string.admob_native_language_id).trim()
            NativeAdKey.ON_BOARDING -> context.getString(R.string.admob_native_on_boarding_id).trim()
            NativeAdKey.HOME -> context.getString(R.string.admob_native_home_id).trim()
            NativeAdKey.FEATURE -> context.getString(R.string.admob_native_full_screen_id).trim()
            NativeAdKey.Settings -> context.getString(R.string.admob_native_settings_id).trim()
        }
    }

    fun loadNativeAd(nativeAdKey: NativeAdKey, callback: (ItemNativeAd?) -> Unit) {
        validateAndLoadAd(nativeAdKey, callback) { adId ->
            isAdLoading = true
            repositoryNativeImpl.fetchNativeAd(adKey = nativeAdKey.value, adId = adId) {
                isAdLoading = false
                callback.invoke(it)
            }
        }
    }

    private fun validateAndLoadAd(nativeAdKey: NativeAdKey, callback: (ItemNativeAd?) -> Unit, loadAdAction: (adId: String) -> Unit) {
        val isRemoteEnable = checkRemoteConfig(nativeAdKey)
        val adId = getAdId(nativeAdKey)

        when {
            sharedPreferenceUtils.isAppPurchased -> {
                Log.e(TAG_ADS, "${nativeAdKey.value} -> loadNative: Premium user")
                callback.invoke(null)
            }

            isRemoteEnable.not() -> {
                Log.e(TAG_ADS, "${nativeAdKey.value} -> loadNative: Remote config is off")
                callback.invoke(null)
            }

            internetManager.isInternetConnected.not() -> {
                Log.e(TAG_ADS, "${nativeAdKey.value} -> loadNative: Internet is not connected")
                callback.invoke(null)
            }

            adId.isEmpty() -> {
                Log.e(TAG_ADS, "${nativeAdKey.value} -> loadNative: Ad id is empty")
                callback.invoke(null)
            }

            isAdLoading -> {
                Log.e(TAG_ADS, "${nativeAdKey.value} -> loadNative: Ad is already loading")
                callback.invoke(null)
            }

            else -> {
                loadAdAction(adId)
            }
        }
    }

    fun destroyNative(nativeAdKey: NativeAdKey): Boolean {
        val isDestroyed = repositoryNativeImpl.destroyNative(nativeAdKey.value)
        if (isDestroyed)
            Log.e(TAG_ADS, "${nativeAdKey.value} -> destroyNative: destroyed")
        return isDestroyed
    }
}