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
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.rcvOpenApp
import com.hypersoft.admobads.helpers.koin.DIComponent
import com.hypersoft.admobads.ui.activities.SplashActivity
import java.util.*

class AdmobOpenApp(private val myApplication: Application) : LifecycleObserver,
    ActivityLifecycleCallbacks {
    private var currentActivity: Activity? = null
    private var loadTime: Long = 0
    private val diComponent = DIComponent()
    private val AD_TAG = "AdsInformation"

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        try {
            if (!diComponent.sharedPreferenceUtils.isAppPurchased && rcvOpenApp != 0) {
                showAdIfAvailable()
            }
        } catch (ignored: Exception) {
        }
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
                Log.d(AD_TAG, "open is loaded")

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
                            Log.d(AD_TAG, "open is FailedToShow")
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
                Log.d(AD_TAG, "open Ad is FailedToLoad")
                isOpenAdLoading = false
                mAppOpenAd = null
            }
        }

        if (!diComponent.sharedPreferenceUtils.isAppPurchased && rcvOpenApp != 0) {
            if (mAppOpenAd == null && !isOpenAdLoading) {
                isOpenAdLoading = true
                try {
                    AppOpenAd.load(
                        myApplication,
                        myApplication.getString(R.string.admob_open_app_ids),
                        AdRequest.Builder().build(),
                        loadCallback
                    )
//            AppOpenAd.load(
//                myApplication,
//                myApplication.getString(R.string.admob_open_app_ids),
//                AdRequest.Builder().build(),
//                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
//                loadCallback
//            )
                } catch (ignored: Exception) {
                }
            }


        }
    }

    private fun showAdIfAvailable() {
        try {
            if (!diComponent.sharedPreferenceUtils.isAppPurchased && rcvOpenApp != 0) {
                if (currentActivity is SplashActivity || currentActivity is AdActivity)
                    return

                mAppOpenAd?.show(currentActivity!!)
            } else {
                fetchAd()
            }
        } catch (ignored: Exception) {
        }
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