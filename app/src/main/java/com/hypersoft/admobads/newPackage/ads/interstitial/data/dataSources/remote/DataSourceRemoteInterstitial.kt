package com.hypersoft.admobads.newPackage.ads.interstitial.data.dataSources.remote

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.hypersoft.admobads.newPackage.utilities.utils.Constants.TAG_ADS
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class DataSourceRemoteInterstitial(private val context: Context) {

    fun fetchInterAd(adKey: String, adId: String) = callbackFlow<InterstitialAd?> {
        InterstitialAd.load(context, adId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.i(TAG_ADS, "$adKey -> loadInterstitial: onAdLoaded")
                trySend(interstitialAd)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG_ADS, "$adKey -> loadInterstitial: onAdFailedToLoad: ${adError.message}")
                trySend(null)
            }
        })

        awaitClose()
    }
}