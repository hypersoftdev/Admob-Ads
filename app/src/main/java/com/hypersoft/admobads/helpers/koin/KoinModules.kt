package com.hypersoft.admobads.helpers.koin

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.hypersoft.admobads.helpers.firebase.RemoteConfiguration
import com.hypersoft.admobads.helpers.managers.InternetManager
import com.hypersoft.admobads.helpers.preferences.SharedPreferenceUtils
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val managerModules = module {
    single { InternetManager(androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager) }
}

private val utilsModules = module {
    single { SharedPreferenceUtils(androidContext().getSharedPreferences("app_preferences", Application.MODE_PRIVATE)) }
}

private val firebaseModule = module {
    single {
        RemoteConfiguration(
            get(),
            androidContext().getSharedPreferences("firebase_preferences", Application.MODE_PRIVATE)
        )
    }
}

val modulesList = listOf(utilsModules, managerModules, firebaseModule)