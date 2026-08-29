package com.pal.starktest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row table: the app only ever pairs with one bike at a time. */
@Entity(tableName = "bike")
data class BikeEntity(
    @PrimaryKey val id: Int = 0,
    val model: String,
    val variant: String,
    val firmwareVersion: String,
    val imageUrl: String,
)
