package com.hypersoft.admobads.oldPackage.helpers.koin

import com.hypersoft.admobads.oldPackage.helpers.firebase.RemoteConfiguration
import com.hypersoft.admobads.oldPackage.helpers.managers.InternetManager
import com.hypersoft.admobads.oldPackage.helpers.preferences.SharedPreferenceUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
class DIComponent : KoinComponent {

    // Utils
    val sharedPreferenceUtils by inject<SharedPreferenceUtils>()

    // Managers
    val internetManager by inject<InternetManager>()

    // Remote Configuration
    val remoteConfiguration by inject<RemoteConfiguration>()
}