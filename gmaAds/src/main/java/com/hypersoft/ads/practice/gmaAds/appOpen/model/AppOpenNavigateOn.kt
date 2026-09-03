package com.hypersoft.ads.practice.gmaAds.appOpen.model

import com.hypersoft.ads.practice.gmaAds.common.AdShowResult

enum class AppOpenNavigateOn {
    IMPRESSION,
    DISMISS,
}

fun AdShowResult.shouldNavigate(navigateOn: AppOpenNavigateOn): Boolean {
    return when (this) {
        is AdShowResult.Failed,
        is AdShowResult.NotAvailable,
            -> true

        is AdShowResult.Impression -> navigateOn == AppOpenNavigateOn.IMPRESSION
        is AdShowResult.Dismissed -> navigateOn == AppOpenNavigateOn.DISMISS
    }
}