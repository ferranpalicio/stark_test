package com.pal.starktest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One row per ride session. */
@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationS: Long,
    val distanceKm: Double,
    val maxSpeedKmh: Double,
)
