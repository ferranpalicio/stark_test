package com.pal.starktest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pal.starktest.data.local.entity.BatterySummaryEntity
import com.pal.starktest.data.local.entity.BikeEntity
import com.pal.starktest.data.local.entity.DiagnosticsEntity
import com.pal.starktest.data.local.entity.RideSettingsEntity
import com.pal.starktest.data.local.entity.SessionEntity

@Dao
interface BikeDao {
    @Query("SELECT * FROM bike WHERE id = 0 LIMIT 1")
    suspend fun getBike(): BikeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBike(entity: BikeEntity)

    @Query("SELECT * FROM battery_summary WHERE id = 0 LIMIT 1")
    suspend fun getBatterySummary(): BatterySummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBatterySummary(entity: BatterySummaryEntity)

    @Query("SELECT * FROM ride_settings WHERE id = 0 LIMIT 1")
    suspend fun getRideSettings(): RideSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRideSettings(entity: RideSettingsEntity)

    @Query("SELECT * FROM session ORDER BY id DESC")
    suspend fun getSessions(): List<SessionEntity>

    @Query("SELECT * FROM session ORDER BY id DESC LIMIT 1")
    suspend fun getLastSession(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(entity: SessionEntity): Long

    @Query("SELECT * FROM diagnostics WHERE id = 0 LIMIT 1")
    suspend fun getDiagnostics(): DiagnosticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiagnostics(entity: DiagnosticsEntity)
}
