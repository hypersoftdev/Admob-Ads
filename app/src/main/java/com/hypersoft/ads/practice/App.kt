package com.hypersoft.ads.practice

import android.app.Application
import com.hypersoft.ads.practice.di.KoinModules
import com.hypersoft.ads.practice.gmaAds.AdsManager
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.lazyModules
import org.koin.core.waitAllStartJobs

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin()
        getKoin().waitAllStartJobs()
        get<AdsManager>()
    }

    private fun startKoin() {
        startKoin {
            androidContext(this@App)
            lazyModules(KoinModules().getKoinModules())
        }
    }
}