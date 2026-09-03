package com.hypersoft.ads.practice.entrance.di

import com.hypersoft.ads.practice.entrance.viewModel.EntranceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule

val entrancePresentationModule = lazyModule {

    //// ViewModels
    viewModel {
        EntranceViewModel(
            remoteConfigRepository = get(),
            sharedPrefRepository = get(),
        )
    }
}