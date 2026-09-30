package com.hypersoft.ads.practice.entrance.state

data class EntranceState(
    val isLoading: Boolean = true,
    val isConsentTimerRunning: Boolean = false,
    val isAdsTimerRunning: Boolean = false,
    val hasResolvedDestination: Boolean = false,
    val skipAds: Boolean = false,
) {
    val showLoading: Boolean = isLoading
}