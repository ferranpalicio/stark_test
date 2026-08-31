package com.pal.starktest.di

import com.pal.starktest.features.app.AppViewModel
import com.pal.starktest.features.sessions.SessionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { AppViewModel(get()) }
    // Resolved inside the Sessions nav entry, so its store owner is the entry, not the Activity.
    viewModel { SessionsViewModel(get()) }
}
