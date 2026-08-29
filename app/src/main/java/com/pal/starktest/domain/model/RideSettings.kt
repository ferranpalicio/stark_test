package com.pal.starktest.domain.model

enum class PowerMap {
    ECO,
    ENDURO,
    RALLY,
    MX,
}

data class RideSettings(
    val powerMap: PowerMap,
    val maxPowerHp: Double,
    val engineBrakingPct: Int,
    val regenPct: Int,
)
