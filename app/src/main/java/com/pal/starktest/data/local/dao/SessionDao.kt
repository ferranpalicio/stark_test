package com.pal.starktest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pal.starktest.data.local.entity.SessionEntity

/**
 * Completed ride sessions info (duration, distance, max speed). A ride only lands here once it
 * ends; while it is in progress it lives in DataStore.
 */
@Dao
interface SessionDao {
    @Query("SELECT * FROM session ORDER BY id DESC")
    suspend fun getSessions(): List<SessionEntity>

    @Query("SELECT * FROM session ORDER BY id DESC LIMIT 1")
    suspend fun getLastSession(): SessionEntity?

    /** Pass `id = 0` to let Room autogenerate; returns the new row id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(entity: SessionEntity): Long
}
