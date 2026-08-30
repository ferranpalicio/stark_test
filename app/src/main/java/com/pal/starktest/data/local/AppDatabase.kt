package com.pal.starktest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pal.starktest.data.local.dao.SessionDao
import com.pal.starktest.data.local.entity.SessionEntity

/**
 * Version 2 dropped the five single-row tables (user, bike, battery_summary, ride_settings,
 * diagnostics) in favour of a typed DataStore; only session history remains. The upgrade is
 * destructive on purpose — those tables were caches of telemetry that re-populates on the next
 * tick, so there is nothing worth migrating.
 */
@Database(
    entities = [SessionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        const val NAME = "stark_test.db"
    }
}
