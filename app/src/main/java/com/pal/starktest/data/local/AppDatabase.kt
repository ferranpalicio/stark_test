package com.pal.starktest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pal.starktest.data.local.dao.BikeDao
import com.pal.starktest.data.local.dao.UserDao
import com.pal.starktest.data.local.entity.BatterySummaryEntity
import com.pal.starktest.data.local.entity.BikeEntity
import com.pal.starktest.data.local.entity.DiagnosticsEntity
import com.pal.starktest.data.local.entity.RideSettingsEntity
import com.pal.starktest.data.local.entity.SessionEntity
import com.pal.starktest.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        BikeEntity::class,
        BatterySummaryEntity::class,
        RideSettingsEntity::class,
        SessionEntity::class,
        DiagnosticsEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bikeDao(): BikeDao

    companion object {
        const val NAME = "stark_test.db"
    }
}
