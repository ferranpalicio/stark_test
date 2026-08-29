package com.pal.starktest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row table storing only the fields the spec calls out: soc % and estimated range. */
@Entity(tableName = "battery_summary")
data class BatterySummaryEntity(
    @PrimaryKey val id: Int = 0,
    val stateOfChargePct: Int,
    val estimatedRangeKm: Int,
)
