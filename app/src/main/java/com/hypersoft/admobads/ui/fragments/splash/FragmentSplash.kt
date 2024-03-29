package com.hypersoft.admobads.ui.fragments.splash

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.hypersoft.admobads.R
import com.hypersoft.admobads.adsconfig.AdmobInterstitial
import com.hypersoft.admobads.adsconfig.callbacks.InterstitialOnLoadCallBack
import com.hypersoft.admobads.adsconfig.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.databinding.FragmentSplashBinding
import com.hypersoft.admobads.helpers.firebase.RemoteConstants.rcvInterAd
import com.hypersoft.admobads.ui.activities.SplashActivity
import com.hypersoft.admobads.ui.fragments.base.BaseFragment

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class FragmentSplash : BaseFragment<FragmentSplashBinding>(R.layout.fragment_splash) {

    private val admobInterstitial by lazy { AdmobInterstitial() }

    private val mHandler = Handler(Looper.getMainLooper())
    private val adsRunner = Runnable { checkAdvertisement() }
    private var isInterLoadOrFailed = false
    private var mCounter: Int = 0

    private var startTime = 0L

    override fun onViewCreatedOneTime() {
        fetchRemoteConfiguration()
    }

    override fun onViewCreatedEverytime() {}


    private fun fetchRemoteConfiguration() {
        diComponent.remoteConfiguration.checkRemoteConfig { fetchSuccessfully ->
            if (isAdded){
                if (fetchSuccessfully) {
                    mCounter = 0
                    loadAds()
                }else{
                    mHandler.removeCallbacks { adsRunner }
                    moveNext(2000)
                }
            }
        }
    }

    private fun loadAds() {
        if (isAdded) {
            startTime = System.currentTimeMillis()
            when (rcvInterAd) {
                0 -> {
                    isInterLoadOrFailed = true
                }
                1 -> {
                    Log.d("AdsInformation", "Call Admob Splash Interstitial")
                    admobInterstitial.loadInterstitialAd(
                        activity,
                        getResString(R.string.admob_inter_ids),
                        rcvInterAd,
                        diComponent.sharedPreferenceUtils.isAppPurchased,
                        diComponent.internetManager.isInternetConnected,
                        object : InterstitialOnLoadCallBack {
                            override fun onAdFailedToLoad(adError: String) {
                                isInterLoadOrFailed = true
                            }

                            override fun onAdLoaded() {
                                isInterLoadOrFailed = true
                                val endTime = System.currentTimeMillis()
                                val loadingTime:Int = ((endTime - startTime)/1000).toInt()
                                Log.d("AdsInformation", "InterLoadingTime: ${loadingTime}s")
                            }

                            override fun onPreloaded() {
                                isInterLoadOrFailed = true
                            }

                        })
                }
                else -> {
                    isInterLoadOrFailed = true
                }
            }
        }
    }

    private fun checkAdvertisement() {
        if (diComponent.internetManager.isInternetConnected) {
            if (mCounter < 16) {
                try {
                    mCounter++
                    if (isInterLoadOrFailed) {
                        moveNext()
                        mHandler.removeCallbacks { adsRunner }
                    } else {
                        mHandler.removeCallbacks { adsRunner }
                        mHandler.postDelayed(
                            adsRunner,
                            (1000)
                        )
                    }

                } catch (e: Exception) {
                    Log.e("checkAdvertisementTAG", "${e.message}")
                }
            } else {
                moveNext()
                mHandler.removeCallbacks { adsRunner }
            }
        } else {
            moveNext(3000)
        }

    }

    private fun  moveNext(timeMilli:Long = 500) {
        withDelay(timeMilli) {
            lifecycleScope.launchWhenResumed {
                if (isAdded){
                    (activity as SplashActivity).nextActivity()
                    admobInterstitial.showInterstitialAd(activity,object : InterstitialOnShowCallBack {
                        override fun onAdDismissedFullScreenContent() {}
                        override fun onAdFailedToShowFullScreenContent() {}
                        override fun onAdShowedFullScreenContent() {}
                        override fun onAdImpression() {}

                    })
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopHandler()
    }

    override fun onResume() {
        super.onResume()
        resumeHandler()
    }

    private fun stopHandler() {
        mHandler.removeCallbacks(adsRunner)
    }

    private fun resumeHandler() {
        mHandler.post(adsRunner)
    }

    override fun navIconBackPressed() {}

    override fun onBackPressed() {}

}