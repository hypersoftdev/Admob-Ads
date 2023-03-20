package com.hypersoft.admobads.helpers.utils

import com.hypersoft.admobads.adsconfig.constants.AdsConstants
import com.hypersoft.admobads.helpers.firebase.RemoteConstants

object CleanMemory {

    fun clean() {
        RemoteConstants.reset()
        AdsConstants.reset()
    }

}