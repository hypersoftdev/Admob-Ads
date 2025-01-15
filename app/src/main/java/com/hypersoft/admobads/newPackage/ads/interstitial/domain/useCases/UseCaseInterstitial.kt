package com.hypersoft.admobads.newPackage.ads.interstitial.domain.useCases

import android.content.res.Resources
import android.util.Log
import com.hypersoft.admobads.R
import com.hypersoft.admobads.newPackage.ads.interstitial.data.entities.ItemInterstitialAd
import com.hypersoft.admobads.newPackage.ads.interstitial.data.repositories.RepositoryInterstitialImpl
import com.hypersoft.admobads.newPackage.ads.interstitial.presentation.enums.InterAdKey
import com.hypersoft.admobads.newPackage.ads.natives.data.entities.ItemNativeAd
import com.hypersoft.admobads.newPackage.utilities.manager.InternetManager
import com.hypersoft.admobads.newPackage.utilities.manager.SharedPreferencesUtils
import com.hypersoft.admobads.newPackage.utilities.utils.Constants.TAG_ADS

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class UseCaseInterstitial(
    private val repositoryInterstitialImpl: RepositoryInterstitialImpl,
    private val sharedPreferenceUtils: SharedPreferencesUtils,
    private val internetManager: InternetManager,
    private val resources: Resources
) {

    private val isAdLoading = false

    private fun checkRemoteConfig(interAdKey: InterAdKey): Boolean {
        return when (interAdKey) {
            InterAdKey.SPLASH -> sharedPreferenceUtils.rcInterSplash != 0
            InterAdKey.ON_BOARDING -> sharedPreferenceUtils.rcInterOnBoarding != 0
        }
    }

    private fun getAdId(interAdKey: InterAdKey): String {
        return when (interAdKey) {
            InterAdKey.SPLASH -> resources.getString(R.string.admob_inter_splash_id).trim()
            InterAdKey.ON_BOARDING -> resources.getString(R.string.admob_inter_on_boarding_id).trim()
        }
    }

    fun loadInterAd(interAdKey: InterAdKey, callback: (ItemInterstitialAd?) -> Unit) {
        validateAndLoadAd(interAdKey, callback) { adId ->
            repositoryInterstitialImpl.fetchInterAd(adKey = interAdKey.value, adId = adId, callback = callback)
        }
    }

    private fun loadInterAd(interAdKey: InterAdKey) {
        repositoryInterstitialImpl.loadInterAd(interAdKey)
    }


    private fun validateAndLoadAd(interAdKey: InterAdKey, callback: (ItemInterstitialAd?) -> Unit, loadAdAction: (adId: String) -> Unit) {
        val isRemoteEnable = checkRemoteConfig(interAdKey)
        val adId = getAdId(interAdKey)

        when {
            sharedPreferenceUtils.isAppPurchased -> {
                Log.e(TAG_ADS, "${interAdKey.value} -> loadInterAd: Premium user")
                callback.invoke(null)
            }

            isRemoteEnable.not() -> {
                Log.e(TAG_ADS, "${interAdKey.value} -> loadInterAd: Remote config is off")
                callback.invoke(null)
            }

            internetManager.isInternetConnected.not() -> {
                Log.e(TAG_ADS, "${interAdKey.value} -> loadInterAd: Internet is not connected")
                callback.invoke(null)
            }

            adId.isEmpty() -> {
                Log.e(TAG_ADS, "${interAdKey.value} -> loadInterAd: Ad id is empty")
                callback.invoke(null)
            }

            isAdLoading -> {
                Log.e(TAG_ADS, "${interAdKey.value} -> loadInterAd: Ad is already loading")
                callback.invoke(null)
            }

            else -> {
                loadAdAction(adId)
            }
        }
    }
}