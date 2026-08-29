package com.pal.starktest.domain.model

enum class ChargingState {
    CHARGING,
    DISCHARGING,
    IDLE,
}

data class Battery(
    val stateOfChargePct: Int,
    val estimatedRangeKm: Int,
    val temperatureC: Double,
    val chargingState: ChargingState,
)

/**
 * Subset of [Battery] persisted locally, per assessment spec (state_of_charge_pct,
 * estimated_range_km only).
 */
data class BatterySummary(
    val stateOfChargePct: Int,
    val estimatedRangeKm: Int,
)
