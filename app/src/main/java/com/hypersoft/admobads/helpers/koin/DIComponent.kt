package com.hypersoft.admobads.helpers.koin

import com.hypersoft.admobads.helpers.firebase.RemoteConfiguration
import com.hypersoft.admobads.helpers.managers.InternetManager
import com.hypersoft.admobads.helpers.preferences.SharedPreferenceUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DIComponent : KoinComponent {

    // Utils
    val sharedPreferenceUtils by inject<SharedPreferenceUtils>()

    // Managers
    val internetManager by inject<InternetManager>()

    // Remote Configuration
    val remoteConfiguration by inject<RemoteConfiguration>()
}