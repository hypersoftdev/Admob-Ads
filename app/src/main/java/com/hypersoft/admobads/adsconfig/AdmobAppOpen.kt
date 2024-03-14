package com.hypersoft.admobads.adsconfig

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.hypersoft.admobads.R
import com.hypersoft.admobads.adsconfig.constants.AdsConstants.isOpenAdLoading
import com.hypersoft.admobads.adsconfig.constants.AdsConstants.mAppOpenAd
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.rcvAppOpen
import com.hypersoft.admobads.helpers.koin.DIComponent
import com.hypersoft.admobads.ui.activities.SplashActivity
import java.util.*

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class AdmobAppOpen(private val myApplication: Application) : LifecycleObserver,
    ActivityLifecycleCallbacks {
    private var currentActivity: Activity? = null
    private var loadTime: Long = 0
    protected val diComponent by lazy { DIComponent() }

    /**
     * 0 = Ads Off
     * 1 = Ads On
     */

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        try {
            if (!diComponent.sharedPreferenceUtils.isAppPurchased && rcvAppOpen != 0) {
                showAdIfAvailable()
            }
        } catch (ignored: Exception) {}
    }

    fun fetchAd() {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable) {
            return
        }
        val loadCallback: AppOpenAdLoadCallback = object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                super.onAdLoaded(appOpenAd)
                isOpenAdLoading = false
                mAppOpenAd = appOpenAd
                Log.d("AdsInformation", "open is loaded")

                mAppOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        mAppOpenAd = null
                        isShowingAd = false
                        fetchAd()
                        if (appOpenListener != null) {
                            appOpenListener?.onOpenAdClosed()
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        if (appOpenListener != null) {
                            appOpenListener?.onOpenAdClosed()
                            Log.d("AdsInformation", "open is FailedToShow")
                        }
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
                loadTime = Date().time
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.d("AdsInformation", "open Ad is FailedToLoad")
                isOpenAdLoading = false
                mAppOpenAd = null
            }
        }

        if (!diComponent.sharedPreferenceUtils.isAppPurchased && rcvAppOpen != 0) {
            if (mAppOpenAd == null && !isOpenAdLoading) {
                isOpenAdLoading = true
                try {
                    AppOpenAd.load(
                        myApplication,
                        myApplication.getString(R.string.admob_app_open_ids),
                        AdRequest.Builder().build(),
                        loadCallback
                    )
                } catch (ignored: Exception) {}
            }
        }
    }

    private fun showAdIfAvailable() {
        try {
            if (!diComponent.sharedPreferenceUtils.isAppPurchased && rcvAppOpen != 0) {
                if (currentActivity is SplashActivity || currentActivity is AdActivity)
                    return

                mAppOpenAd?.show(currentActivity!!)
            } else {
                fetchAd()
            }
        } catch (ignored: Exception) {}
    }

    private var appOpenListener: AppOpenListener? = null

    interface AppOpenListener {
        fun onOpenAdClosed()
    }

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * 4.toLong()
    }

    private val isAdAvailable: Boolean
        get() = mAppOpenAd != null && wasLoadTimeLessThanNHoursAgo()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    companion object {
        private var isShowingAd = false
    }

    init {
        this.myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

}