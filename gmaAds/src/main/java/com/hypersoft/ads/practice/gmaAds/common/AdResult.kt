package com.hypersoft.ads.practice.gmaAds.common

sealed interface AdFailureReason {
    data object Premium : AdFailureReason
    data object RemoteDisabled : AdFailureReason
    data object NoInternet : AdFailureReason
    data object EmptyAdUnitId : AdFailureReason
    data object InvalidActivity : AdFailureReason
    data class Sdk(val code: Int, val message: String) : AdFailureReason
}

val AdFailureReason.logMessage: String
    get() = when (this) {
        AdFailureReason.Premium -> "Premium user"
        AdFailureReason.RemoteDisabled -> "Remote config disabled"
        AdFailureReason.NoInternet -> "No internet"
        AdFailureReason.EmptyAdUnitId -> "Ad ID is empty"
        AdFailureReason.InvalidActivity -> "Activity finishing/destroyed"
        is AdFailureReason.Sdk -> message.ifBlank { "SDK error $code" }
    }

sealed interface AdLoadResult {
    data object Loaded : AdLoadResult
    data object AlreadyLoading : AdLoadResult
    data object SkippedFallback : AdLoadResult
    data object SkippedCounter : AdLoadResult
    data class Failed(val reason: AdFailureReason) : AdLoadResult
}

val AdLoadResult.isSettled: Boolean
    get() = this is AdLoadResult.Loaded ||
            this is AdLoadResult.SkippedFallback ||
            this is AdLoadResult.SkippedCounter ||
            this is AdLoadResult.Failed

sealed interface AdShowResult {
    data object Impression : AdShowResult
    data object Dismissed : AdShowResult
    data object NotAvailable : AdShowResult
    data class Failed(val reason: AdFailureReason) : AdShowResult
}

val AdShowResult.didDisplay: Boolean
    get() = this is AdShowResult.Impression || this is AdShowResult.Dismissed

sealed interface RewardedShowResult {
    data object NotAvailable : RewardedShowResult
    data class Closed(val rewardGranted: Boolean) : RewardedShowResult
    data class Failed(val reason: AdFailureReason) : RewardedShowResult
}

sealed interface NativeShowResult {
    data object Rendered : NativeShowResult
    data object NotAvailable : NativeShowResult
    data class Failed(val reason: AdFailureReason) : NativeShowResult
}

sealed interface BannerShowResult {
    data object Rendered : BannerShowResult
    data object NotAvailable : BannerShowResult
    data class Failed(val reason: AdFailureReason) : BannerShowResult
}