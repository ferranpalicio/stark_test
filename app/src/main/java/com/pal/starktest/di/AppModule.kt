package com.pal.starktest.di

import com.pal.starktest.features.app.AppViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { AppViewModel(get()) }
}
