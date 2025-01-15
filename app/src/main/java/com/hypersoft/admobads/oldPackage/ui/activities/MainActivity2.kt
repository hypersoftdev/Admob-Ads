package com.hypersoft.admobads.oldPackage.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.ActivityMain2Binding
import com.hypersoft.admobads.oldPackage.adsconfig.interstitial.AdmobInterstitial
import com.hypersoft.admobads.oldPackage.adsconfig.interstitial.callbacks.InterstitialOnLoadCallBack
import com.hypersoft.admobads.oldPackage.adsconfig.interstitial.callbacks.InterstitialOnShowCallBack
import com.hypersoft.admobads.oldPackage.helpers.firebase.RemoteConstants.rcvInterAd
import com.hypersoft.admobads.oldPackage.helpers.firebase.RemoteConstants.rcvRemoteCounter
import com.hypersoft.admobads.oldPackage.helpers.firebase.RemoteConstants.totalCount
import com.hypersoft.admobads.oldPackage.helpers.utils.CleanMemory

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class MainActivity2 : BaseActivity<ActivityMain2Binding>(R.layout.activity_main_2) {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val admobInterstitial by lazy { AdmobInterstitial() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.materialToolbar)
        initNavController()
    }


    private fun initNavController() {
        navController =
            (supportFragmentManager.findFragmentById(binding.navHostFragmentContainer.id) as NavHostFragment).navController
        appBarConfiguration = AppBarConfiguration(setOf(R.id.fragmentHome))
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun checkCounter() {
        try {
            if (admobInterstitial.isInterstitialLoaded()) {
                showInterstitialAd()
                totalCount += 1
            } else {
                if (totalCount >= rcvRemoteCounter) {
                    totalCount = 1
                    loadInterstitialAd()
                } else {
                    totalCount += 1
                }
            }
        } catch (e: Exception) {
            Log.d("AdsInformation", "${e.message}")
        }
    }


    fun loadInterstitialAd() {
        admobInterstitial.loadInterstitialAd(
            this,
            getString(R.string.admob_inter_ids),
            rcvInterAd,
            diComponent.sharedPreferenceUtils.isAppPurchased,
            diComponent.internetManager.isInternetConnected,
            object : InterstitialOnLoadCallBack {
                override fun onAdFailedToLoad(adError: String) {}
                override fun onAdLoaded() {}
                override fun onPreloaded() {}
            }
        )
    }

    fun showInterstitialAd() {
        admobInterstitial.showInterstitialAd(
            this,
            object : InterstitialOnShowCallBack {
                override fun onAdDismissedFullScreenContent() {}
                override fun onAdFailedToShowFullScreenContent() {}
                override fun onAdShowedFullScreenContent() {}
                override fun onAdImpression() {}
            }
        )
    }


    /**
     *  Call 'CleanMemory.clean()' to avoid memory leaks.
     *  This destroys all the resources
     */

    override fun onDestroy() {
        CleanMemory.clean()
        super.onDestroy()
    }
}