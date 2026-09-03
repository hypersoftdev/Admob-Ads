package com.hypersoft.ads.practice.data.remoteConfig.repository

import android.util.Log
import com.hypersoft.ads.practice.core.Constants.TAG_REMOTE_CONFIG
import com.hypersoft.ads.practice.core.platform.InternetManager
import com.hypersoft.ads.practice.data.remoteConfig.dataSource.RemoteConfigDataSource
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteConfigRepositoryImpl(
    private val remoteConfigDataSource: RemoteConfigDataSource,
    private val sharedPrefManager: SharedPrefManager,
    private val internetManager: InternetManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RemoteConfigRepository {

    private var listenerRegistered: Boolean = false

    override suspend fun fetchAndCache(): Boolean = withContext(ioDispatcher) {
        if (!internetManager.isInternetConnected) {
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
                rcAppOpen = remoteConfigDataSource.getInt(appOpen)
                rcAppOpenEntrance = remoteConfigDataSource.getInt(appOpenEntrance)

                rcBannerLanguage = remoteConfigDataSource.getInt(bannerLanguage)
                rcBannerOnBoarding = remoteConfigDataSource.getInt(bannerOnBoarding)
                rcBannerDashboard = remoteConfigDataSource.getInt(bannerDashboard)
                rcBannerFeatureOne = remoteConfigDataSource.getInt(bannerFeatureOne)
                rcBannerFeatureTwo = remoteConfigDataSource.getInt(bannerFeatureTwo)

                rcInterEntrance = remoteConfigDataSource.getInt(interEntrance)
                rcInterOnboarding = remoteConfigDataSource.getInt(interOnboarding)
                rcInterHome = remoteConfigDataSource.getInt(interHome)
                rcInterBottomNavigation = remoteConfigDataSource.getInt(interBottomNavigation)
                rcInterBackPress = remoteConfigDataSource.getInt(interBackPress)
                rcInterExit = remoteConfigDataSource.getInt(interExit)

                rcRewardedHome = remoteConfigDataSource.getInt(rewardedHome)
                rcRewardedInterHome = remoteConfigDataSource.getInt(rewardedInterHome)

                rcNativeLanguage = remoteConfigDataSource.getInt(nativeLanguage)
                rcNativeOnBoarding = remoteConfigDataSource.getInt(nativeOnBoarding)
                rcNativeMenu = remoteConfigDataSource.getInt(nativeMenu)
                rcNativeHome = remoteConfigDataSource.getInt(nativeHome)
                rcNativeTrending = remoteConfigDataSource.getInt(nativeTrending)
                rcNativeSetting = remoteConfigDataSource.getInt(nativeSetting)
                rcNativeFeatureOne = remoteConfigDataSource.getInt(nativeFeatureOne)
                rcNativeFeatureTwo = remoteConfigDataSource.getInt(nativeFeatureTwo)

                rcCounterInterHome = remoteConfigDataSource.getInt(counterInterHome)
                rcCounterInterBottomNavigation = remoteConfigDataSource.getInt(counterInterBottomNavigation)
                rcCounterInterBackPress = remoteConfigDataSource.getInt(counterInterBackPress)

                rcShowPremiumFirstTime = remoteConfigDataSource.getInt(showPremiumFirstTime)
                rcShowPremiumSecondTime = remoteConfigDataSource.getInt(showPremiumSecondTime)

                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcAppOpen -> ${remoteConfigDataSource.getInt(appOpen)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcAppOpenEntrance -> ${remoteConfigDataSource.getInt(appOpenEntrance)}")

                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcBannerLanguage -> ${remoteConfigDataSource.getInt(bannerLanguage)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcBannerOnBoarding -> ${remoteConfigDataSource.getInt(bannerOnBoarding)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcBannerDashboard -> ${remoteConfigDataSource.getInt(bannerDashboard)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcBannerFeatureOne -> ${remoteConfigDataSource.getInt(bannerFeatureOne)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcBannerFeatureTwo -> ${remoteConfigDataSource.getInt(bannerFeatureTwo)}")

                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcInterEntrance -> ${remoteConfigDataSource.getInt(interEntrance)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcInterOnBoarding -> ${remoteConfigDataSource.getInt(interOnboarding)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcInterHome -> ${remoteConfigDataSource.getInt(interHome)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcInterBottomNavigation -> ${remoteConfigDataSource.getInt(interBottomNavigation)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcInterBackPress -> ${remoteConfigDataSource.getInt(interBackPress)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcInterExit -> ${remoteConfigDataSource.getInt(interExit)}")

                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcRewardedHome -> ${remoteConfigDataSource.getInt(rewardedHome)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcRewardedInterHome -> ${remoteConfigDataSource.getInt(rewardedInterHome)}")

                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcNativeLanguage -> ${remoteConfigDataSource.getInt(nativeLanguage)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcNativeOnBoarding -> ${remoteConfigDataSource.getInt(nativeOnBoarding)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcNativeMenu -> ${remoteConfigDataSource.getInt(nativeMenu)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcNativeHome -> ${remoteConfigDataSource.getInt(nativeHome)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcNativeTrending -> ${remoteConfigDataSource.getInt(nativeTrending)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcNativeSetting -> ${remoteConfigDataSource.getInt(nativeSetting)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcNativeFeatureOne -> ${remoteConfigDataSource.getInt(nativeFeatureOne)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcNativeFeatureTwo -> ${remoteConfigDataSource.getInt(nativeFeatureTwo)}")

                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcCounterInterHome -> ${remoteConfigDataSource.getInt(counterInterHome)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcCounterInterBottomNavigation -> ${remoteConfigDataSource.getInt(counterInterBottomNavigation)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcCounterInterBackPress -> ${remoteConfigDataSource.getInt(counterInterBackPress)}")

                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcShowPremiumFirstTime -> ${remoteConfigDataSource.getInt(showPremiumFirstTime)}")
                Log.i(TAG_REMOTE_CONFIG, "RemoteConfiguration: rcShowPremiumSecondTime -> ${remoteConfigDataSource.getInt(showPremiumSecondTime)}")
            } catch (ex: Exception) {
                Log.e(TAG_REMOTE_CONFIG, "RemoteConfigRepositoryImpl: saveValues: Failed: ${ex.message}")
            }
        }
    }
}