package com.hypersoft.ads.practice.data.di

import com.hypersoft.ads.practice.data.remoteConfig.dataSource.RemoteConfigDataSource
import com.hypersoft.ads.practice.data.remoteConfig.repository.RemoteConfigRepository
import com.hypersoft.ads.practice.data.remoteConfig.repository.RemoteConfigRepositoryImpl
import com.hypersoft.ads.practice.data.sharedPreferences.dataSource.SharedPrefManager
import com.hypersoft.ads.practice.data.sharedPreferences.repository.SharedPrefRepository
import com.hypersoft.ads.practice.data.sharedPreferences.repository.SharedPrefRepositoryImpl
import org.koin.dsl.lazyModule

val dataModule = lazyModule {

    //// DataSources
    single { SharedPrefManager(get()) }
    single { RemoteConfigDataSource() }

    //// Repositories
    single<SharedPrefRepository> { SharedPrefRepositoryImpl(sharedPrefManager = get()) }
    single<RemoteConfigRepository> { RemoteConfigRepositoryImpl(remoteConfigDataSource = get(), sharedPrefManager = get(), internetManager = get()) }
}