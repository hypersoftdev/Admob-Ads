package com.hypersoft.ads.practice.di

import com.hypersoft.ads.practice.core.di.coreModule
import com.hypersoft.ads.practice.data.di.dataModule
import com.hypersoft.ads.practice.entrance.di.entrancePresentationModule
import com.hypersoft.ads.practice.gmaAds.di.gmaAdsModule
import org.koin.core.module.LazyModule

class KoinModules {

    fun getKoinModules(): List<LazyModule> = listOf(
        //// Cores
        coreModule,

        //// Data
        dataModule,

        //// Ads
        gmaAdsModule,

        //// Presentation
        entrancePresentationModule,
    )
}