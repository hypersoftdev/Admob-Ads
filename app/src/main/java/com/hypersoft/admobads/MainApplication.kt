package com.hypersoft.admobads

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.hypersoft.admobads.helpers.koin.modulesList
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()

        // to get test ads on this device."
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("E13FEE4C2083A31575BFEFD22146CE76")).build()
        )
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@MainApplication)
            modules(modulesList)
        }
    }
}