package com.pal.starktest.data.telemetry.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors the JSON payload defined in the assessment brief, field for field. */
@Serializable
data class BikeTelemetryDto(
    val bike: BikeDto,
    val timestamp: String,
    val battery: BatteryDto,
    val motor: MotorDto,
    @SerialName("ride_settings") val rideSettings: RideSettingsDto,
    val session: SessionDto,
    val diagnostics: DiagnosticsDto,
)

@Serializable
data class BikeDto(
    val model: String,
    val variant: String,
    @SerialName("firmware_version") val firmwareVersion: String,
    @SerialName("image_url") val imageUrl: String,
)

@Serializable
data class BatteryDto(
    @SerialName("state_of_charge_pct") val stateOfChargePct: Int,
    @SerialName("estimated_range_km") val estimatedRangeKm: Int,
    @SerialName("temperature_c") val temperatureC: Double,
    @SerialName("charging_state") val chargingState: String,
)

@Serializable
data class MotorDto(
    @SerialName("power_hp") val powerHp: Double,
    @SerialName("temperature_c") val temperatureC: Double,
)

@Serializable
data class RideSettingsDto(
    @SerialName("power_map") val powerMap: String,
    @SerialName("max_power_hp") val maxPowerHp: Double,
    @SerialName("engine_braking_pct") val engineBrakingPct: Int,
    @SerialName("regen_pct") val regenPct: Int,
)

@Serializable
data class SessionDto(
    @SerialName("duration_s") val durationS: Long,
    @SerialName("distance_km") val distanceKm: Double,
    @SerialName("max_speed_kmh") val maxSpeedKmh: Double,
)

@Serializable
data class DiagnosticsDto(
    @SerialName("fault_codes") val faultCodes: List<String>,
    val warnings: List<WarningDto>,
)

@Serializable
data class WarningDto(
    val code: String,
    val message: String,
    val severity: String,
)
