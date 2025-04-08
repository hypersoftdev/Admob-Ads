package com.hypersoft.admobads.ads.banner.presentation.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdView
import com.hypersoft.admobads.ads.banner.domain.useCases.UseCaseBanner
import com.hypersoft.admobads.ads.banner.presentation.enums.BannerAdKey
import com.hypersoft.admobads.utilities.observers.SingleLiveEvent
import kotlinx.coroutines.launch

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class ViewModelBanner(private val useCaseBanner: UseCaseBanner) : ViewModel() {

    private val _adViewLiveData = MutableLiveData<AdView>()
    val adViewLiveData: LiveData<AdView> get() = _adViewLiveData

    private val _loadFailedLiveData = MutableLiveData<Unit>()
    val loadFailedLiveData: LiveData<Unit> get() = _loadFailedLiveData

    private val _clearViewLiveData = SingleLiveEvent<Unit>()
    val clearViewLiveData: LiveData<Unit> get() = _clearViewLiveData

    fun loadBannerAd(adView: AdView, bannerAdKey: BannerAdKey) = viewModelScope.launch {
        useCaseBanner.loadBannerAd(adView, bannerAdKey) { itemBannerAd ->
            itemBannerAd?.let {
                _adViewLiveData.value = it.adView
            } ?: kotlin.run {
                _loadFailedLiveData.value = Unit
            }
        }
    }

    fun destroyBanner(bannerAdKey: BannerAdKey) = viewModelScope.launch {
        if (useCaseBanner.destroyBanner(bannerAdKey)) {
            _clearViewLiveData.postValue(Unit)
        }
    }
}