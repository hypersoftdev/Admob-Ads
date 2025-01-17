package com.hypersoft.admobads.ads.banner.domain.useCases

import android.content.Context
import android.util.Log
import com.hypersoft.admobads.R
import com.hypersoft.admobads.ads.banner.data.entities.ItemBannerAd
import com.hypersoft.admobads.ads.banner.data.repositories.RepositoryBannerImpl
import com.hypersoft.admobads.ads.banner.presentation.enums.BannerAdKey
import com.hypersoft.admobads.utilities.manager.InternetManager
import com.hypersoft.admobads.utilities.manager.SharedPreferencesUtils
import com.hypersoft.admobads.utilities.utils.Constants.TAG_ADS

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class UseCaseBanner(
    private val repositoryBannerImpl: RepositoryBannerImpl,
    private val sharedPreferenceUtils: SharedPreferencesUtils,
    private val internetManager: InternetManager,
    private val context: Context
) {

    @Volatile
    private var isAdLoading = false

    private fun checkRemoteConfig(bannerAdKey: BannerAdKey): Boolean {
        return when (bannerAdKey) {
            BannerAdKey.HOME -> sharedPreferenceUtils.rcBannerHome != 0
        }
    }

    private fun getAdId(bannerAdKey: BannerAdKey): String {
        return when (bannerAdKey) {
            BannerAdKey.HOME -> context.getString(R.string.admob_banner_home_id).trim()
        }
    }

    fun loadBannerAd(bannerAdKey: BannerAdKey, callback: (ItemBannerAd?) -> Unit) {
        validateAndLoadAd(bannerAdKey, callback) { adId ->
            isAdLoading = true
            repositoryBannerImpl.fetchBannerAd(adKey = bannerAdKey.value, adId = adId) {
                isAdLoading = false
                callback.invoke(it)
            }
        }
    }

    private fun validateAndLoadAd(bannerAdKey: BannerAdKey, callback: (ItemBannerAd?) -> Unit, loadAdAction: (adId: String) -> Unit) {
        val isRemoteEnable = checkRemoteConfig(bannerAdKey)
        val adId = getAdId(bannerAdKey)

        when {
            sharedPreferenceUtils.isAppPurchased -> {
                Log.e(TAG_ADS, "${bannerAdKey.value} -> loadBanner: Premium user")
                callback.invoke(null)
            }

            isRemoteEnable.not() -> {
                Log.e(TAG_ADS, "${bannerAdKey.value} -> loadBanner: Remote config is off")
                callback.invoke(null)
            }

            internetManager.isInternetConnected.not() -> {
                Log.e(TAG_ADS, "${bannerAdKey.value} -> loadBanner: Internet is not connected")
                callback.invoke(null)
            }

            adId.isEmpty() -> {
                Log.e(TAG_ADS, "${bannerAdKey.value} -> loadBanner: Ad id is empty")
                callback.invoke(null)
            }

            isAdLoading -> {
                Log.e(TAG_ADS, "${bannerAdKey.value} -> loadBanner: Ad is already loading")
                callback.invoke(null)
            }

            else -> {
                loadAdAction(adId)
            }
        }
    }

    fun destroyBanner(bannerAdKey: BannerAdKey): Boolean {
        val isDestroyed = repositoryBannerImpl.destroyBanner(bannerAdKey.value)
        if (isDestroyed)
            Log.e(TAG_ADS, "${bannerAdKey.value} -> destroyBanner: destroyed")
        return isDestroyed
    }
}