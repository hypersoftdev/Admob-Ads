package com.hypersoft.admobads.newPackage.ads.interstitial.presentation.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.hypersoft.admobads.newPackage.ads.interstitial.domain.sealed.InterResponse
import com.hypersoft.admobads.newPackage.ads.interstitial.domain.useCases.UseCaseInterstitial
import com.hypersoft.admobads.newPackage.ads.interstitial.presentation.enums.InterAdKey
import com.hypersoft.admobads.newPackage.utilities.utils.Constants.TAG
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class ViewModelInterstitial(private val useCaseInterstitial: UseCaseInterstitial) : ViewModel() {

    private val _interResponseLiveData = MutableLiveData<InterResponse>()
    val interResponseLiveData: LiveData<InterResponse> get() = _interResponseLiveData

    private val _interAdLiveData = MutableLiveData<InterstitialAd>()
    val interAdLiveData: LiveData<InterstitialAd> get() = _interAdLiveData

    private val _loadFailedLiveData = MutableLiveData<Unit>()
    val loadFailedLiveData: LiveData<Unit> get() = _loadFailedLiveData

    fun loadInterAd(interAdKey: InterAdKey) = viewModelScope.launch {
        useCaseInterstitial.loadInterAd(interAdKey)
            .catch {
                Log.e(TAG, "ViewModelInterstitial: loadInterAd: ", it)
                emit(InterResponse.FAILURE("Exception found: ${it.message}"))
            }
            .collect {
                _interResponseLiveData.value = it
            }
    }

    private fun showInterstitialAd() = viewModelScope.launch {
        useCaseInterstitial.showInterAd()
    }
}