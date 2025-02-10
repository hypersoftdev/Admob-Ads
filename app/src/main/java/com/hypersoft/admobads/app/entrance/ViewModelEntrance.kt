package com.hypersoft.admobads.app.entrance

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by: Sohaib Ahmed
 * Date: 2/10/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class ViewModelEntrance : ViewModel() {

    /* ----------------------------------- Remote Config ----------------------------------- */

    private val _remoteConfigResponseLiveData = MutableLiveData<Unit>()
    val remoteConfigResponseLiveData: LiveData<Unit> get() = _remoteConfigResponseLiveData

    fun onRemoteConfigResponse() {
        _remoteConfigResponseLiveData.value = Unit
    }

    /* ----------------------------------- Consent & Ads ----------------------------------- */

    private val _cmpTimerLiveData = MutableLiveData<Unit>()
    val cmpTimerLiveData: LiveData<Unit> get() = _cmpTimerLiveData

    private val _loadAdsLiveData = MutableLiveData<Unit>()
    val loadAdsLiveData: LiveData<Unit> get() = _loadAdsLiveData

    private val _navigateLiveData = MutableLiveData<Unit>()
    val navigateLiveData: LiveData<Unit> get() = _navigateLiveData

    private var jobCMP = Job()
    private var jobAds = Job()

    private var isAdTimerStarted = false
    private val consentTimeout = 8000L
    private val adsTimeout = 8000L

    init {
        startCMPTimer()
    }

    private fun startCMPTimer() = viewModelScope.launch(Dispatchers.Default + jobCMP) {
        Log.i("AdsInformation", "CMP -> startCMPTimer: Started 8 seconds")
        delay(consentTimeout)
        _cmpTimerLiveData.postValue(Unit)
    }

    fun startAdTimer() = viewModelScope.launch(Dispatchers.Default + jobAds) {
        if (isAdTimerStarted) return@launch
        Log.i("AdsInformation", "Ads -> startAdTimer: Started 8 seconds for Ads")

        isAdTimerStarted = true
        _loadAdsLiveData.postValue(Unit)

        delay(adsTimeout)
        _navigateLiveData.postValue(Unit)
    }


    fun cancelCMPJob() {
        if (jobCMP.isActive) {
            Log.e("AdsInformation", "CMP -> cancelCMPJob: Cancelled 8 seconds")
            jobCMP.cancel()
        }
    }

    fun cancelAdsJob() {
        if (jobAds.isActive) {
            Log.e("AdsInformation", "Ads -> cancelAdsJob: Cancelled 8 seconds for Ads")
            jobAds.cancel()
        }
    }

    /* ----------------------------------- Ads Responses ----------------------------------- */

    private val totalAds = 2
    private val loadedAdsCounter = AtomicInteger(0)

    fun onAdResponse() {
        if (loadedAdsCounter.incrementAndGet() >= totalAds) {
            cancelAdsJob()
            _navigateLiveData.postValue(Unit)
        }
    }
}