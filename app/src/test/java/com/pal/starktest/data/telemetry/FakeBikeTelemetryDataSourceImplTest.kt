package com.pal.starktest.data.telemetry

import android.content.Context
import android.content.res.AssetManager
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.time.Duration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Mirrors the production `TICK_INTERVAL_S`, which is private to the data source. */
private const val TICK_INTERVAL_S = 15L

@OptIn(ExperimentalCoroutinesApi::class)
class FakeBikeTelemetryDataSourceImplTest {

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
    private lateinit var dataSource: FakeBikeTelemetryDataSourceImpl

    @Before
    fun setUp() {
        every { context.assets } returns assetManager
        every { assetManager.open(any()) } answers { ByteArrayInputStream(templateJson.toByteArray()) }
        dataSource = FakeBikeTelemetryDataSourceImpl(context, Json { ignoreUnknownKeys = true })
    }

    @Test
    fun `getDefaultSnapshot loads template with zero speed`() = runTest {
        val snapshot = dataSource.getDefaultSnapshot()

        assertEquals("VARG", snapshot.bike.model)
        assertEquals(0.0, snapshot.currentSpeedKmh, 0.0)
    }

    @Test
    fun `observeTelemetry always starts a fresh session from zero`() = runTest {
        dataSource.observeTelemetry().test {
            val first = awaitItem()
            assertEquals(0L, first.session.durationS)
            assertEquals(0.0, first.session.distanceKm, 0.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeTelemetry increments duration and timestamp by one tick interval`() = runTest {
        val results = dataSource.observeTelemetry().take(2).toList()

        assertEquals(2, results.size)
        // Simulated elapsed time must track the wall-clock delay, or the session totals drift.
        assertEquals(TICK_INTERVAL_S, results[1].session.durationS - results[0].session.durationS)
        assertEquals(
            TICK_INTERVAL_S,
            Duration.between(results[0].timestamp, results[1].timestamp).seconds,
        )
    }

    @Test
    fun `observeTelemetry emits one snapshot per tick interval`() = runTest {
        val start = testScheduler.currentTime

        dataSource.observeTelemetry().take(2).toList()

        assertEquals(TICK_INTERVAL_S * 1_000, testScheduler.currentTime - start)
    }

    @Test
    fun `observeTelemetry accumulates distance from speed over the tick interval`() = runTest {
        val results = dataSource.observeTelemetry().take(2).toList()

        val expected = results[0].currentSpeedKmh * TICK_INTERVAL_S / 3600
        assertEquals(expected, results[1].session.distanceKm, 1e-9)
    }

    @Test
    fun `observeTelemetry drains battery over ticks`() = runTest {
        val results = dataSource.observeTelemetry().take(2).toList()

        assertTrue(results[1].battery.stateOfChargePct <= results[0].battery.stateOfChargePct)
    }
}
