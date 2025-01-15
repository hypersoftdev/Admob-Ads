package com.hypersoft.admobads.oldPackage.ui.fragments.splash

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentSplashStartBinding
import com.hypersoft.admobads.oldPackage.adsconfig.interstitial.AdmobInterstitial
import com.hypersoft.admobads.oldPackage.adsconfig.interstitial.callbacks.InterstitialOnLoadCallBack
import com.hypersoft.admobads.oldPackage.adsconfig.natives.AdmobNativePreload
import com.hypersoft.admobads.oldPackage.adsconfig.natives.callbacks.NativeCallBack
import com.hypersoft.admobads.oldPackage.helpers.firebase.RemoteConstants.rcvInterAd
import com.hypersoft.admobads.oldPackage.helpers.firebase.RemoteConstants.rcvNativeAd
import com.hypersoft.admobads.oldPackage.ui.fragments.base.BaseFragment

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class FragmentSplashStart : BaseFragment<FragmentSplashStartBinding>(R.layout.fragment_splash_start) {

    private val admobInterstitial by lazy { AdmobInterstitial() }
    private val admobNativePreload by lazy { AdmobNativePreload() }

    private val mHandler = Handler(Looper.getMainLooper())
    private val adsRunner = Runnable { checkAdvertisement() }
    private var isInterLoadOrFailed = false
    private var isNativeLoadedOrFailed = false
    private var mCounter: Int = 0

    private var startTime = 0L

    override fun onViewCreatedOneTime() {
        fetchRemoteConfiguration()
    }

    override fun onViewCreatedEverytime() {}

    private fun fetchRemoteConfiguration() {
        diComponent.remoteConfiguration.checkRemoteConfig { fetchSuccessfully ->
            Log.d("REMOTE_CONFIG", "fetchSuccessfully")
            if (isAdded) {
                if (fetchSuccessfully) {
                    mCounter = 0
                    loadAds()
                } else {
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
                                val loadingTime: Int = ((endTime - startTime) / 1000).toInt()
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

            when (rcvNativeAd) {
                0 -> {
                    isNativeLoadedOrFailed = true
                }

                1 -> {
                    Log.d("AdsInformation", "Call Admob Splash Native")
                    admobNativePreload.loadNativeAds(
                        activity,
                        getResString(R.string.admob_native_ids),
                        rcvNativeAd,
                        diComponent.sharedPreferenceUtils.isAppPurchased,
                        diComponent.internetManager.isInternetConnected,
                        object : NativeCallBack {
                            override fun onAdFailedToLoad(adError: String) {
                                isNativeLoadedOrFailed = true
                            }

                            override fun onAdLoaded() {
                                isNativeLoadedOrFailed = true
                                val endTime = System.currentTimeMillis()
                                val loadingTime: Int = ((endTime - startTime) / 1000).toInt()
                                Log.d("AdsInformation", "NativeLoadingTime: ${loadingTime}s")
                            }
                        })
                }

                else -> {
                    isNativeLoadedOrFailed = true
                }
            }
        }


    }

    private fun checkAdvertisement() {
        if (diComponent.internetManager.isInternetConnected) {
            if (mCounter < 16) {
                try {
                    mCounter++
                    if (isInterLoadOrFailed && isNativeLoadedOrFailed) {
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

    private fun moveNext(timeMilli: Long = 500) {
        withDelay(timeMilli) {
            lifecycleScope.launchWhenResumed {
                if (isAdded) {
                    navigateTo(R.id.fragmentSplashStart, R.id.action_fragmentStart_to_fragmentLanguage)
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