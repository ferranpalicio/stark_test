package com.pal.starktest.data.telemetry

import android.content.Context
import android.content.res.AssetManager
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BikeTelemetryDataSourceImplTest {

    private val templateJson = """
        {
          "bike": {"model": "VARG", "variant": "Alpha", "firmware_version": "1.0", "image_url": "url"},
          "timestamp": "2025-01-01T00:00:00Z",
          "battery": {"state_of_charge_pct": 80, "estimated_range_km": 40, "temperature_c": 30.0, "charging_state": "discharging"},
          "motor": {"power_hp": 50.0, "temperature_c": 60.0},
          "ride_settings": {"power_map": "eco", "max_power_hp": 80.0, "engine_braking_pct": 10, "regen_pct": 20},
          "session": {"duration_s": 0, "distance_km": 0.0, "max_speed_kmh": 0.0},
          "diagnostics": {"fault_codes": [], "warnings": []}
        }
    """.trimIndent()

    private val context: Context = mockk()
    private val assetManager: AssetManager = mockk()
    private lateinit var dataSource: BikeTelemetryDataSourceImpl

    @Before
    fun setUp() {
        every { context.assets } returns assetManager
        every { assetManager.open(any()) } answers { ByteArrayInputStream(templateJson.toByteArray()) }
        dataSource = BikeTelemetryDataSourceImpl(context, Json { ignoreUnknownKeys = true })
    }

    @Test
    fun `getDefaultSnapshot loads template with zero speed`() = runTest {
        val snapshot = dataSource.getDefaultSnapshot()

        assertEquals("VARG", snapshot.bike.model)
        assertEquals(0.0, snapshot.currentSpeedKmh, 0.0)
    }

    @Test
    fun `observeTelemetry starts a fresh session from zero when none provided`() = runTest {
        dataSource.observeTelemetry(initialTimestamp = null, initialSession = null).test {
            val first = awaitItem()
            assertEquals(0L, first.session.durationS)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeTelemetry resumes from provided session totals`() = runTest {
        val resumed = com.pal.starktest.domain.model.Session(durationS = 300, distanceKm = 5.0, maxSpeedKmh = 70.0)

        dataSource.observeTelemetry(initialTimestamp = null, initialSession = resumed).test {
            val first = awaitItem()
            assertEquals(300L, first.session.durationS)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeTelemetry increments duration and timestamp on each tick`() = runTest {
        val results = dataSource.observeTelemetry().take(2).toList()

        assertEquals(2, results.size)
        assertTrue(results[1].session.durationS > results[0].session.durationS)
        assertTrue(results[1].timestamp.isAfter(results[0].timestamp))
    }

    @Test
    fun `observeTelemetry drains battery over ticks`() = runTest {
        val results = dataSource.observeTelemetry().take(2).toList()

        assertTrue(results[1].battery.stateOfChargePct <= results[0].battery.stateOfChargePct)
    }
}
