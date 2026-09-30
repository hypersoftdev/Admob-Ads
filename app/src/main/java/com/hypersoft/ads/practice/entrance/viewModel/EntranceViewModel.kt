package com.hypersoft.ads.practice.entrance.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypersoft.ads.practice.core.Constants.TAG
import com.hypersoft.ads.practice.data.remoteConfig.repository.RemoteConfigRepository
import com.hypersoft.ads.practice.data.sharedPreferences.repository.SharedPrefRepository
import com.hypersoft.ads.practice.entrance.effect.EntranceEffect
import com.hypersoft.ads.practice.entrance.intent.EntranceIntent
import com.hypersoft.ads.practice.entrance.state.EntranceState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class EntranceViewModel(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EntranceState())
    val state: StateFlow<EntranceState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EntranceEffect>(extraBufferCapacity = 2, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val effect: SharedFlow<EntranceEffect> = _effect.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch { handleError(throwable) }
    }

    private var consentTimerJob: Job? = null
    private var adsTimerJob: Job? = null
    private var purchaseWatchJob: Job? = null
    private var screenStarted: Boolean = false
    private var adsFlowStarted: Boolean = false

    fun handleIntent(intent: EntranceIntent) = viewModelScope.launch(exceptionHandler) {
        when (intent) {
            EntranceIntent.ScreenStarted -> onScreenStarted()
            EntranceIntent.ConsentFormShown -> onConsentFormShown()
            EntranceIntent.AdsCanBeLoaded -> onAdsCanBeLoaded()
            EntranceIntent.AdsLoaded -> onAdsLoaded()
            EntranceIntent.ConsentTimerExpired -> onConsentTimerExpired()
            EntranceIntent.AdsTimerExpired -> onAdsTimerExpired()
        }
    }

    private suspend fun onScreenStarted() {
        if (screenStarted) return
        screenStarted = true

        viewModelScope.launch(exceptionHandler) {
            remoteConfigRepository.fetchAndCache()
        }

        if (sharedPrefRepository.isAppPurchased()) {
            onPurchased()
            return
        }

        watchPurchase()
        _state.update { it.copy(isConsentTimerRunning = true) }
        _effect.emit(EntranceEffect.InitializeConsent)

        consentTimerJob = viewModelScope.launch {
            delay(CONSENT_TIMEOUT_MS.milliseconds)
            handleIntent(EntranceIntent.ConsentTimerExpired)
        }
    }

    private fun watchPurchase() {
        purchaseWatchJob = viewModelScope.launch(exceptionHandler) {
            while (!_state.value.hasResolvedDestination) {
                if (sharedPrefRepository.isAppPurchased()) {
                    onPurchased()
                    return@launch
                }
                delay(PURCHASE_POLL_MS.milliseconds)
            }
        }
    }

    private suspend fun onPurchased() {
        if (_state.value.hasResolvedDestination) return
        consentTimerJob?.cancel()
        adsTimerJob?.cancel()
        _state.update {
            it.copy(
                isConsentTimerRunning = false,
                isAdsTimerRunning = false,
                skipAds = true,
            )
        }
        Log.d(TAG, "EntranceViewModel: onPurchased: Success: skipAds")
        resolveDestination()
    }

    private fun onConsentFormShown() {
        consentTimerJob?.cancel()
        _state.update { it.copy(isConsentTimerRunning = false) }
    }

    private suspend fun onAdsCanBeLoaded() {
        if (_state.value.skipAds || _state.value.hasResolvedDestination) return
        if (adsFlowStarted) return
        adsFlowStarted = true

        consentTimerJob?.cancel()
        _state.update { it.copy(isConsentTimerRunning = false) }

        _effect.emit(EntranceEffect.LoadAds)
        startAdsTimer()
    }

    private fun startAdsTimer() {
        adsTimerJob?.cancel()
        _state.update { it.copy(isAdsTimerRunning = true) }
        adsTimerJob = viewModelScope.launch {
            delay(ADS_TIMEOUT_MS.milliseconds)
            handleIntent(EntranceIntent.AdsTimerExpired)
        }
    }

    private suspend fun onAdsLoaded() {
        adsTimerJob?.cancel()
        _state.update { it.copy(isAdsTimerRunning = false) }
        resolveDestination()
    }

    private suspend fun onConsentTimerExpired() {
        _state.update { it.copy(isConsentTimerRunning = false) }
        onAdsCanBeLoaded()
    }

    private suspend fun onAdsTimerExpired() {
        _state.update { it.copy(isAdsTimerRunning = false) }
        resolveDestination()
    }

    private suspend fun resolveDestination() {
        if (_state.value.hasResolvedDestination) return
        _state.update { it.copy(isLoading = false, hasResolvedDestination = true) }

        val purchased = _state.value.skipAds || sharedPrefRepository.isAppPurchased()
        if (purchased && !sharedPrefRepository.isFirstTimeUser()) {
            _effect.emit(EntranceEffect.NavigateToDashboard)
            return
        }

        if (!sharedPrefRepository.isFirstTimeUser()) {
            _effect.emit(EntranceEffect.NavigateToMenu)
            return
        }

        if (sharedPrefRepository.isLanguageSelected()) {
            _effect.emit(EntranceEffect.NavigateToOnboarding)
        } else {
            _effect.emit(EntranceEffect.NavigateToLanguage)
        }
    }

    private suspend fun handleError(throwable: Throwable) {
        Log.e(TAG, "EntranceViewModel: exceptionHandler: Failed: ${throwable.message}")
        _state.update { it.copy(isLoading = false) }
        if (!_state.value.hasResolvedDestination) {
            _state.update { it.copy(hasResolvedDestination = true) }
            _effect.emit(EntranceEffect.NavigateToLanguage)
        }
    }

    private companion object {
        const val CONSENT_TIMEOUT_MS = 8000L
        const val ADS_TIMEOUT_MS = 8000L
        const val PURCHASE_POLL_MS = 200L
    }
}