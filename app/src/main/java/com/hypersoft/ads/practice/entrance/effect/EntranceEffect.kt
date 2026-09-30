package com.hypersoft.ads.practice.entrance.effect

sealed class EntranceEffect {
    data object InitializeConsent : EntranceEffect()
    data object LoadAds : EntranceEffect()
    data object NavigateToLanguage : EntranceEffect()
    data object NavigateToOnboarding : EntranceEffect()
    data object NavigateToMenu : EntranceEffect()
    data object NavigateToDashboard : EntranceEffect()
}