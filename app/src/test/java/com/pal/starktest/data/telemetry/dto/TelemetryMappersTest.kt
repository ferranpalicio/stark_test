package com.pal.starktest.data.telemetry.dto

import com.pal.starktest.domain.model.ChargingState
import com.pal.starktest.domain.model.PowerMap
import com.pal.starktest.domain.model.WarningSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryMappersTest {

    private val bikeDto = BikeDto(
        model = "Stark VARG MX 1.2",
        variant = "Alpha",
        firmwareVersion = "3.4.1",
        imageUrl = "https://example.com/bike.webp",
    )
    private val batteryDto = BatteryDto(
        stateOfChargePct = 73,
        estimatedRangeKm = 38,
        temperatureC = 34.7,
        chargingState = "discharging",
    )
    private val motorDto = MotorDto(powerHp = 52.4, temperatureC = 61.2)
    private val rideSettingsDto = RideSettingsDto(
        powerMap = "enduro",
        maxPowerHp = 80.0,
        engineBrakingPct = 45,
        regenPct = 60,
    )
    private val sessionDto = SessionDto(durationS = 3742, distanceKm = 24.7, maxSpeedKmh = 94.1)
    private val warningDto =
        WarningDto(code = "W_MOT_TEMP_HIGH", message = "Motor temperature elevated", severity = "warning")
    private val diagnosticsDto = DiagnosticsDto(faultCodes = emptyList(), warnings = listOf(warningDto))
    private val telemetryDto = BikeTelemetryDto(
        bike = bikeDto,
        timestamp = "2025-05-19T10:32:45Z",
        battery = batteryDto,
        motor = motorDto,
        rideSettings = rideSettingsDto,
        session = sessionDto,
        diagnostics = diagnosticsDto,
    )

    @Test
    fun `BikeDto maps field for field`() {
        val domain = bikeDto.toDomain()
        assertEquals("Stark VARG MX 1.2", domain.model)
        assertEquals("Alpha", domain.variant)
        assertEquals("3.4.1", domain.firmwareVersion)
        assertEquals("https://example.com/bike.webp", domain.imageUrl)
    }

    @Test
    fun `BatteryDto maps charging state case-insensitively`() {
        val domain = batteryDto.copy(chargingState = "CHARGING").toDomain()
        assertEquals(ChargingState.CHARGING, domain.chargingState)
    }

    @Test
    fun `BatteryDto falls back to IDLE for unknown charging state`() {
        val domain = batteryDto.copy(chargingState = "unknown").toDomain()
        assertEquals(ChargingState.IDLE, domain.chargingState)
    }

    @Test
    fun `RideSettingsDto falls back to ECO for unknown power map`() {
        val domain = rideSettingsDto.copy(powerMap = "unknown").toDomain()
        assertEquals(PowerMap.ECO, domain.powerMap)
    }

    @Test
    fun `RideSettingsDto maps known power map`() {
        val domain = rideSettingsDto.copy(powerMap = "RALLY").toDomain()
        assertEquals(PowerMap.RALLY, domain.powerMap)
    }

    @Test
    fun `WarningDto falls back to INFO for unknown severity`() {
        val domain = warningDto.copy(severity = "unknown").toDomain()
        assertEquals(WarningSeverity.INFO, domain.severity)
    }

    @Test
    fun `DiagnosticsDto maps warnings list`() {
        val domain = diagnosticsDto.toDomain()
        assertTrue(domain.faultCodes.isEmpty())
        assertEquals(1, domain.warnings.size)
        assertEquals("W_MOT_TEMP_HIGH", domain.warnings.first().code)
    }

    @Test
    fun `BikeTelemetryDto maps full snapshot with given speed`() {
        val domain = telemetryDto.toDomain(currentSpeedKmh = 42.0)
        assertEquals(42.0, domain.currentSpeedKmh, 0.0)
        assertEquals("Stark VARG MX 1.2", domain.bike.model)
        assertEquals(3742, domain.session.durationS)
        assertEquals("2025-05-19T10:32:45Z", domain.timestamp.toString())
    }
}
