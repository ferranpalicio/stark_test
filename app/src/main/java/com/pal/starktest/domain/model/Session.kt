package com.pal.starktest.domain.model

data class Session(
    val id: Long = 0,
    val durationS: Long,
    val distanceKm: Double,
    val maxSpeedKmh: Double,
)
