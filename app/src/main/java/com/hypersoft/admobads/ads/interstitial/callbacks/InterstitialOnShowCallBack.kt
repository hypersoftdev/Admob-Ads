package com.hypersoft.admobads.ads.interstitial.callbacks

/**
 * Created by: Sohaib Ahmed
 * Date: 1/16/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

interface InterstitialOnShowCallBack {
    fun onAdDismissedFullScreenContent() {}
    fun onAdFailedToShow()
    fun onAdShowedFullScreenContent() {}
    fun onAdImpression() {}
    fun onAdClicked() {}
    fun onAdImpressionDelayed() {}
}