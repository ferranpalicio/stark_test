package com.pal.starktest.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.room.Room
import com.pal.starktest.data.local.AppDatabase
import com.pal.starktest.data.local.LocalDataSourceImpl
import com.pal.starktest.data.local.datastore.StarkPreferences
import com.pal.starktest.data.local.datastore.StarkPreferencesSerializer
import com.pal.starktest.data.remote.NetworkDataSourceImpl
import com.pal.starktest.data.repository.BikeRepositoryImpl
import com.pal.starktest.data.telemetry.FakeBikeTelemetryDataSourceImpl
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

    single<DataStore<StarkPreferences>> {
        DataStoreFactory.create(
            serializer = StarkPreferencesSerializer,
            produceFile = {
                androidContext().dataStoreFile(StarkPreferencesSerializer.FILE_NAME)
            },
        )
    }

    single {
        // v1 held five single-row tables that DataStore now owns; nothing in them is worth
        // migrating, so an existing install just drops them.
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<AppDatabase>().sessionDao() }

    single<LocalDataSource> { LocalDataSourceImpl(get(), get()) }
    single<NetworkDataSource> { NetworkDataSourceImpl() }
    single<BikeTelemetryDataSource> { FakeBikeTelemetryDataSourceImpl(androidContext(), get()) }

    single<BikeRepository> { BikeRepositoryImpl(get(), get(), get()) }
}
