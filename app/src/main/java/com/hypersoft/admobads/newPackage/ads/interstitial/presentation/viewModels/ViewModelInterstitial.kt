package com.hypersoft.admobads.newPackage.ads.interstitial.presentation.viewModels

import androidx.lifecycle.ViewModel
import com.hypersoft.admobads.newPackage.ads.interstitial.domain.useCases.UseCaseInterstitial
import com.hypersoft.admobads.newPackage.ads.interstitial.presentation.enums.InterAdKey

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class ViewModelInterstitial(private val useCaseInterstitial: UseCaseInterstitial) : ViewModel() {

    private fun loadInterAd(interAdKey: InterAdKey) {
        useCaseInterstitial.loadInterAd(interAdKey)
    }
}