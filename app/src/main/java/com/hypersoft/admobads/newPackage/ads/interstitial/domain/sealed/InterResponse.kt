package com.hypersoft.admobads.newPackage.ads.interstitial.domain.sealed

/**
 * Created by: Sohaib Ahmed
 * Date: 1/16/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

sealed class InterResponse {
    object LOADING : InterResponse()
    object SUCCESS : InterResponse()
    data class FAILURE(val errorMessage: String) : InterResponse()
}