package com.hypersoft.ads.practice.core.di

import com.hypersoft.ads.practice.core.platform.InternetManager
import org.koin.dsl.lazyModule

val coreModule = lazyModule {

    //// Managers
    single { InternetManager(get()) }
}