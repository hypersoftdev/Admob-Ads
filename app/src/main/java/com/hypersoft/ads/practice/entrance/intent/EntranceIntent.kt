package com.hypersoft.ads.practice.entrance.intent

sealed class EntranceIntent {
    data object ScreenStarted : EntranceIntent()
    data object ConsentFormShown : EntranceIntent()
    data object AdsCanBeLoaded : EntranceIntent()
    data object AdsLoaded : EntranceIntent()
    data object ConsentTimerExpired : EntranceIntent()
    data object AdsTimerExpired : EntranceIntent()
}