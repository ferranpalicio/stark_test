package com.pal.starktest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pal.starktest.data.local.entity.SessionEntity

/**
 * Ride sessions info (duration, distance, max speed)
 */
@Dao
interface SessionDao {
    @Query("SELECT * FROM session ORDER BY id DESC")
    suspend fun getSessions(): List<SessionEntity>

    @Query("SELECT * FROM session ORDER BY id DESC LIMIT 1")
    suspend fun getLastSession(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(entity: SessionEntity): Long
}
