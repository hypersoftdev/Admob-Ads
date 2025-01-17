package com.hypersoft.admobads.ads.appOpen.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.hypersoft.admobads.R
import com.hypersoft.admobads.utilities.manager.InternetManager
import com.hypersoft.admobads.utilities.manager.SharedPreferenceUtils
import com.hypersoft.admobads.utilities.utils.Constants.TAG_ADS
import java.util.Date

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class AppOpenAdManager(private val application: Application, private val internetManager: InternetManager, private val sharedPreferenceUtils: SharedPreferenceUtils) : Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null
    private var appOpenAd: AppOpenAd? = null

    private var loadTime = 0L

    private var isInterGoingToShow = false
    private var isLoadingAd = false
    private var isShowingAd = false
    var isSplash = true

    /* --------------------------------------- Manage --------------------------------------- */

    private val defaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            Log.d(TAG_ADS, "AppOpen -> defaultLifecycleObserver: onStart: Called")
            Handler(Looper.getMainLooper()).post { showAppOpen() }
        }
    }

    init {
        Log.d(TAG_ADS, "AppOpen -> init: initializing")
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(defaultLifecycleObserver)
    }

    /* --------------------------------------- Activity LifeCycle --------------------------------------- */

    override fun onActivityStarted(activity: Activity) {
        Log.d(TAG_ADS, "AppOpen -> onActivityStarted: called")
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    /* --------------------------------------- Load & Show --------------------------------------- */

    private val appOpenId by lazy { application.getString(R.string.admob_app_open_id) }

    fun loadAppOpen() {
        if (isAdAvailable()) {
            Log.e(TAG_ADS, "AppOpen -> loadAppOpen: Ad already available")
            return
        }

        if (sharedPreferenceUtils.isAppPurchased) {
            Log.e(TAG_ADS, "AppOpen -> loadAppOpen: User has premium access")
            return
        }

        if (isLoadingAd) {
            Log.e(TAG_ADS, "AppOpen -> loadAppOpen: Ad is already getting load")
            return
        }

        if (appOpenId.trim().isEmpty()) {
            Log.e(TAG_ADS, "AppOpen -> loadAppOpen: Ad Id should not be empty: $appOpenId")
            return
        }

        if (sharedPreferenceUtils.rcAppOpen == 0) {
            Log.e(TAG_ADS, "AppOpen -> loadAppOpen: Remote Configuration: Ad is off")
            return
        }

        if (internetManager.isInternetConnected.not()) {
            Log.e(TAG_ADS, "AppOpen -> loadAppOpen: No Internet connection")
            return
        }

        isLoadingAd = true

        val request = AdRequest.Builder().build()
        AppOpenAd.load(application, appOpenId.trim(), request, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                Log.i(TAG_ADS, "AppOpen -> loadAppOpen: onAdLoaded: loaded")
                appOpenAd = ad
                loadTime = Date().time
                isLoadingAd = false
            }

            override fun onAdFailedToLoad(loadAppOpenError: LoadAdError) {
                Log.e(TAG_ADS, "AppOpen -> loadAppOpen: onAdFailedToLoad: ", Exception(loadAppOpenError.message))
                isLoadingAd = false
            }
        })
    }

    fun showAppOpen() {
        Log.d(TAG_ADS, "AppOpen -> showAppOpen: called")
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Log.e(TAG_ADS, "AppOpen -> showAppOpen: Ad is already showing")
            return
        }

        if (isInterGoingToShow) {
            Log.e(TAG_ADS, "AppOpen -> showAppOpen: Can't show ad, interstitial is about to show")
            return
        }

        if (sharedPreferenceUtils.isAppPurchased) {
            Log.e(TAG_ADS, "AppOpen -> showAppOpen: Premium User")
            return
        }

        if (currentActivity == null) {
            Log.e(TAG_ADS, "AppOpen -> showAppOpen: CurrentActivity is Null")
            return
        }

        if (currentActivity is AdActivity || isPangleAdActivity(currentActivity) || isMintegralAdActivity(currentActivity)) {
            Log.e(TAG_ADS, "AppOpen -> showAppOpen: Another Ad is showing")
            return
        }

        if (isSplash) {
            Log.e(TAG_ADS, "AppOpen -> showAppOpen: Cannot show on Splash")
            return
        }

        if (!isAdAvailable()) {
            Log.e(TAG_ADS, "AppOpen -> showAppOpen: Ad is not available")
            appOpenAd = null
            loadAppOpen()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG_ADS, "AppOpen -> showAppOpen: onAdDismissedFullScreenContent: dismissed")
                isShowingAd = false
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG_ADS, "AppOpen -> showAppOpen: onAdFailedToShowFullScreenContent: ", Exception(adError.message))
                isShowingAd = false
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG_ADS, "AppOpen -> showAppOpen: onAdShowedFullScreenContent: shown")
            }

            override fun onAdImpression() {
                Log.v(TAG_ADS, "AppOpen -> showAppOpen: onAdImpression: called")
                appOpenAd = null
                loadAppOpen()
            }
        }
        isShowingAd = true
        currentActivity?.let { appOpenAd?.show(it) }
    }

    private fun isAdAvailable() = appOpenAd != null && !wasAdExpired()

    private fun wasAdExpired(): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        val isExpired = dateDifference > numMilliSecondsPerHour * 4
        if (isExpired) {
            Log.e(TAG_ADS, "AppOpen -> isAdAvailable: wasAdExpired: ", IllegalStateException("Ad is expired!"))
            appOpenAd = null
        }
        return isExpired
    }

    /**
     *  Uncomment following method for `Pangle Mediation`
     */
    private fun isPangleAdActivity(currentActivity: Activity?): Boolean {
        return false
        /*// If currentActivity is null, return false
        if (currentActivity == null) return false

        val activityClasses = listOf(
            TTFullScreenExpressVideoActivity::class.java,
            TTFullScreenVideoActivity::class.java,
            TTInterstitialActivity::class.java,
            TTInterstitialExpressActivity::class.java,
            TTRewardExpressVideoActivity::class.java,
            TTVideoLandingPageActivity::class.java,
            TTVideoLandingPageLink2Activity::class.java,
            TTWebsiteActivity::class.java,
            TTLandingPageActivity::class.java,
            TTPlayableLandingPageActivity::class.java,
            TTDelegateActivity::class.java,
            TTBaseActivity::class.java,
            TTBaseVideoActivity::class.java
        )

        return activityClasses.any { it.isInstance(currentActivity) }*/
    }

    /**
     *  Uncomment following method for `Mintegral Mediation`
     */
    private fun isMintegralAdActivity(currentActivity: Activity?): Boolean {
        return false
        /*// If currentActivity is null, return false
        if (currentActivity == null) return false
        val activityClasses = listOf(
            MintegralAppOpenAd::class.java,
            MintegralInterstitialAd::class.java,
            MintegralRewardedAd::class.java
        )
        return activityClasses.any { it.isInstance(currentActivity) }*/
    }

    fun resetAppOpen() {
        currentActivity = null
        isSplash = true
    }

    fun destroyAppOpen() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(defaultLifecycleObserver)
        application.unregisterActivityLifecycleCallbacks(this)
        currentActivity = null
        appOpenAd = null
        isSplash = true
        Log.e(TAG_ADS, "AppOpen -> destroyed ad")
    }
}