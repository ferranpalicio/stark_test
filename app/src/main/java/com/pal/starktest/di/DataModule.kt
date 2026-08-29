package com.pal.starktest.di

import androidx.room.Room
import com.pal.starktest.data.local.AppDatabase
import com.pal.starktest.data.local.LocalDataSourceImpl
import com.pal.starktest.data.remote.NetworkDataSourceImpl
import com.pal.starktest.data.repository.BikeRepositoryImpl
import com.pal.starktest.data.telemetry.BikeTelemetryDataSourceImpl
import com.pal.starktest.domain.datasource.BikeTelemetryDataSource
import com.pal.starktest.domain.datasource.LocalDataSource
import com.pal.starktest.domain.datasource.NetworkDataSource
import com.pal.starktest.domain.repository.BikeRepository
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.NAME).build()
    }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().bikeDao() }

    single<LocalDataSource> { LocalDataSourceImpl(get(), get(), get()) }
    single<NetworkDataSource> { NetworkDataSourceImpl() }
    single<BikeTelemetryDataSource> { BikeTelemetryDataSourceImpl(androidContext(), get()) }

    single<BikeRepository> { BikeRepositoryImpl(get(), get(), get()) }
}
