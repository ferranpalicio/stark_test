package com.pal.starktest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row table: the app only ever tracks the signed-in rider. */
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Int = 0,
    val email: String,
    val name: String,
    val phone: String?,
    val country: String?,
)
