package com.pal.starktest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row table. */
@Entity(tableName = "ride_settings")
data class RideSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val powerMap: String,
    val maxPowerHp: Double,
    val engineBrakingPct: Int,
    val regenPct: Int,
)
