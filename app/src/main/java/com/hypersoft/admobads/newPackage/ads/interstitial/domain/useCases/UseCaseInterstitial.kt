package com.hypersoft.admobads.newPackage.ads.interstitial.domain.useCases

import android.content.res.Resources
import android.util.Log
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.hypersoft.admobads.R
import com.hypersoft.admobads.newPackage.ads.interstitial.data.repositories.RepositoryInterstitialImpl
import com.hypersoft.admobads.newPackage.ads.interstitial.domain.sealed.InterResponse
import com.hypersoft.admobads.newPackage.ads.interstitial.presentation.enums.InterAdKey
import com.hypersoft.admobads.newPackage.utilities.manager.InternetManager
import com.hypersoft.admobads.newPackage.utilities.manager.SharedPreferencesUtils
import com.hypersoft.admobads.newPackage.utilities.utils.Constants.TAG_ADS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

    private var interstitialAd: InterstitialAd? = null
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

    fun loadInterAd(interAdKey: InterAdKey): Flow<InterResponse> = callbackFlow {
        val isRemoteEnable = checkRemoteConfig(interAdKey)
        val adId = getAdId(interAdKey)

        when {
            sharedPreferenceUtils.isAppPurchased -> {
                val errorMessage = "${interAdKey.value} -> loadInterAd: Premium user"
                Log.e(TAG_ADS, errorMessage)
                trySend(InterResponse.FAILURE(errorMessage))
                close()
            }

            isRemoteEnable.not() -> {
                val errorMessage = "${interAdKey.value} -> loadInterAd: Remote config is off"
                Log.e(TAG_ADS, errorMessage)
                trySend(InterResponse.FAILURE(errorMessage))
                close()
            }

            interstitialAd != null -> {
                val errorMessage = "${interAdKey.value} -> loadInterAd: Ad already available"
                Log.d(TAG_ADS, errorMessage)
                trySend(InterResponse.SUCCESS)
                close()
            }

            internetManager.isInternetConnected.not() -> {
                val errorMessage = "${interAdKey.value} -> loadInterAd: Internet is not connected"
                Log.e(TAG_ADS, errorMessage)
                trySend(InterResponse.FAILURE(errorMessage))
                close()
            }

            adId.isEmpty() -> {
                val errorMessage = "${interAdKey.value} -> loadInterAd: Ad id is empty"
                Log.e(TAG_ADS, errorMessage)
                trySend(InterResponse.FAILURE(errorMessage))
                close()
            }

            isAdLoading -> {
                val errorMessage = "${interAdKey.value} -> loadInterAd: Ad is already loading"
                Log.d(TAG_ADS, errorMessage)
                trySend(InterResponse.LOADING)
            }

            else -> {
                repositoryInterstitialImpl.fetchInterAd(interAdKey.value, adId)
                    .collect {
                        interstitialAd = it
                        when (it == null) {
                            true -> {
                                val errorMessage = "${interAdKey.value} -> loadInterAd: Ad failed to load"
                                trySend(InterResponse.FAILURE(errorMessage))
                            }

                            false -> {
                                trySend(InterResponse.SUCCESS)
                            }
                        }
                    }
                close()
            }
        }
    }

    private fun showInterAd() {
        interstitialAd?.let {
            repositoryInterstitialImpl.showInterAd(it)
        }
    }
}