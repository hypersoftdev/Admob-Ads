package com.hypersoft.ads.practice.gmaAds.interstitial.model

import com.hypersoft.ads.practice.gmaAds.common.AdShowResult

enum class InterstitialNavigateOn {
    IMPRESSION,
    DISMISS,
}

fun AdShowResult.shouldNavigate(navigateOn: InterstitialNavigateOn): Boolean {
    return when (this) {
        is AdShowResult.Failed,
        is AdShowResult.NotAvailable,
            -> true

        is AdShowResult.Impression -> navigateOn == InterstitialNavigateOn.IMPRESSION
        is AdShowResult.Dismissed -> navigateOn == InterstitialNavigateOn.DISMISS
    }
}
