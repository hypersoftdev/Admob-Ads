package com.hypersoft.ads.practice.gmaAds.appOpen

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdActivity
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.gmaAds.appOpen.model.shouldNavigate
import com.hypersoft.ads.practice.gmaAds.common.FullscreenAdGate
import com.hypersoft.ads.practice.gmaAds.common.extensions.isSafeForAd
import com.hypersoft.ads.practice.gmaAds.common.isSettled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Shows [AppOpenAdKey.LIFECYCLE] when the process returns to the foreground.
 * Between-screen app open (Entrance) goes through [AppOpenAds] load/show, not this observer.
 */
internal class AppOpenLifecycle(
    application: Application,
    private val appOpenAds: AppOpenAds,
    private val fullscreenAdGate: FullscreenAdGate,
) : Application.ActivityLifecycleCallbacks {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentActivity: Activity? = null

    private val processObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            Log.d(TAG_ADS, "lifecycle -> appOpen -> process onStart")
            mainHandler.post { onProcessStarted() }
        }
    }

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(processObserver)
        Log.d(TAG_ADS, "lifecycle -> appOpen -> registered")
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    private fun onProcessStarted() {
        if (!canShowLifecycle()) return
        if (AppOpenAdConfig.LOAD_LIFECYCLE_WITH_LOADING_SCREEN) {
            showLoadingActivity()
        } else {
            showLifecycleAd()
        }
    }

    private fun canShowLifecycle(): Boolean {
        if (appOpenAds.isAppPurchased) {
            Log.d(TAG_ADS, "AppOpenLifecycle: canShowLifecycle: Blocked: premium")
            return false
        }
        if (!appOpenAds.isInternetConnected) {
            Log.d(TAG_ADS, "AppOpenLifecycle: canShowLifecycle: Blocked: no internet")
            return false
        }
        if (appOpenAds.shouldBlock) {
            Log.e(TAG_ADS, "lifecycle -> appOpen -> blocked (splash/premium)")
            return false
        }
        if (fullscreenAdGate.isShowing) {
            Log.e(TAG_ADS, "lifecycle -> appOpen -> skipped, another fullscreen ad")
            return false
        }
        val activity = currentActivity
        if (activity == null || !activity.isSafeForAd()) {
            Log.e(TAG_ADS, "lifecycle -> appOpen -> current activity unavailable")
            return false
        }
        if (activity is AdActivity || activity is AppOpenLoadingActivity) {
            Log.e(TAG_ADS, "lifecycle -> appOpen -> skipped, ad/loading activity")
            return false
        }
        return true
    }

    private fun showLoadingActivity() {
        val activity = currentActivity ?: return
        activity.startActivity(Intent(activity, AppOpenLoadingActivity::class.java))
    }

    private fun showLifecycleAd() {
        val activity = currentActivity ?: return
        scope.launch {
            appOpenAds.show(activity, AppOpenAdKey.LIFECYCLE)
                .first { it.shouldNavigate(AppOpenAdConfig[AppOpenAdKey.LIFECYCLE].navigateOn) }
            appOpenAds.load(AppOpenAdKey.LIFECYCLE).firstOrNull { it.isSettled }
        }
    }
}