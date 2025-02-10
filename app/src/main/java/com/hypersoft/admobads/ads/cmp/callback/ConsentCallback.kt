package com.hypersoft.admobads.ads.cmp.callback

/**
 * Created by: Sohaib Ahmed
 * Date: 2/10/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

interface ConsentCallback {
    fun onAdsLoad(canRequestAd: Boolean) {}
    fun onConsentFormLoaded() {}
    fun onConsentFormDismissed() {}
    fun onPolicyStatus(required: Boolean) {}
}