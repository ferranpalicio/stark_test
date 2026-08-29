package com.pal.starktest

import android.app.Application
import com.pal.starktest.di.appModule
import com.pal.starktest.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class StarkTestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@StarkTestApp)
            modules(dataModule, appModule)
        }
    }
}
