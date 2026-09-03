package com.hypersoft.ads.practice.gmaAds.consent

interface ConsentListener {
    fun onConsentFormReady()
    fun onAdsAllowed(canRequestAds: Boolean)
    fun onPrivacyOptionsRequired(required: Boolean) {}
}
