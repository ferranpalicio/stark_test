package com.pal.starktest.data.telemetry.dto

import com.pal.starktest.domain.model.Battery
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.ChargingState
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.Motor
import com.pal.starktest.domain.model.PowerMap
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.Warning
import com.pal.starktest.domain.model.WarningSeverity
import java.time.Instant
import kotlin.math.round

fun BikeDto.toDomain(): Bike = Bike(
    model = model,
    variant = variant,
    firmwareVersion = firmwareVersion,
    imageUrl = imageUrl,
)

fun BatteryDto.toDomain(): Battery = Battery(
    stateOfChargePct = stateOfChargePct,
    estimatedRangeKm = estimatedRangeKm,
    temperatureC = temperatureC,
    chargingState = chargingState.toChargingState(),
)

fun MotorDto.toDomain(): Motor = Motor(powerHp = powerHp, temperatureC = temperatureC)

fun RideSettingsDto.toDomain(): RideSettings = RideSettings(
    powerMap = powerMap.toPowerMap(),
    maxPowerHp = maxPowerHp,
    engineBrakingPct = engineBrakingPct,
    regenPct = regenPct,
)

fun SessionDto.toDomain(): Session = Session(
    durationS = durationS,
    distanceKm = distanceKm,
    maxSpeedKmh = maxSpeedKmh,
)

fun WarningDto.toDomain(): Warning = Warning(
    code = code,
    message = message,
    severity = severity.toWarningSeverity(),
)

fun DiagnosticsDto.toDomain(): Diagnostics = Diagnostics(
    faultCodes = faultCodes,
    warnings = warnings.map { it.toDomain() },
)

fun BikeTelemetryDto.toDomain(currentSpeedKmh: Double = 0.0): BikeTelemetry = BikeTelemetry(
    bike = bike.toDomain(),
    timestamp = Instant.parse(timestamp),
    battery = battery.toDomain(),
    motor = motor.toDomain(),
    rideSettings = rideSettings.toDomain(),
    session = session.toDomain(),
    diagnostics = diagnostics.toDomain(),
    currentSpeedKmh = currentSpeedKmh.roundToTwoDecimals(),
)

private fun String.toChargingState(): ChargingState =
    ChargingState.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: ChargingState.IDLE

private fun String.toPowerMap(): PowerMap =
    PowerMap.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: PowerMap.ECO

private fun String.toWarningSeverity(): WarningSeverity =
    WarningSeverity.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: WarningSeverity.INFO

private fun Double.roundToTwoDecimals(): Double = round(this * 100.0) / 100.0
