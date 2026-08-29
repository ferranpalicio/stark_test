package com.pal.starktest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table. [faultCodesJson] and [warningsJson] are lists encoded as JSON text - simpler
 * than normalizing into extra tables for this app's scale, see [com.pal.starktest.data.local.LocalDataSourceImpl].
 */
@Entity(tableName = "diagnostics")
data class DiagnosticsEntity(
    @PrimaryKey val id: Int = 0,
    val faultCodesJson: String,
    val warningsJson: String,
)
