package com.hypersoft.admobads.helpers.utils

import com.hypersoft.admobads.adsconfig.constants.AdsConstants
import com.hypersoft.admobads.helpers.firebase.RemoteConstants

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
object CleanMemory {
    fun clean() {
        AdsConstants.reset()
    }
}