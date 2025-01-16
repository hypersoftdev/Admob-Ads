package com.hypersoft.admobads.ads.natives.presentation.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.hypersoft.admobads.ads.natives.domain.useCases.UseCaseNative
import com.hypersoft.admobads.ads.natives.presentation.enums.NativeAdKey
import kotlinx.coroutines.launch

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class ViewModelNative(private val useCaseNative: UseCaseNative) : ViewModel() {

    private val _adViewLiveData = MutableLiveData<NativeAd>()
    val adViewLiveData: LiveData<NativeAd> get() = _adViewLiveData

    private val _loadFailedLiveData = MutableLiveData<Unit>()
    val loadFailedLiveData: LiveData<Unit> get() = _loadFailedLiveData

    fun loadNativeAd(nativeAdKey: NativeAdKey) = viewModelScope.launch {
        useCaseNative.loadNativeAd(nativeAdKey) { itemNativeAd ->
            itemNativeAd?.let {
                _adViewLiveData.value = it.nativeAd
            } ?: kotlin.run {
                _loadFailedLiveData.value = Unit
            }
        }
    }

    fun destroyNative(nativeAdKey: NativeAdKey) {
        useCaseNative.destroyNative(nativeAdKey)
    }
}