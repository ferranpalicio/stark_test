package com.pal.starktest.data.telemetry

import android.content.Context
import com.pal.starktest.data.telemetry.dto.BikeTelemetryDto
import com.pal.starktest.data.telemetry.dto.toDomain
import com.pal.starktest.domain.datasource.BikeTelemetryDataSource
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.Session
import java.time.Instant
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

private const val ASSET_FILE_NAME = "bike_telemetry.json"
private const val TICK_INTERVAL_MS = 60_000L

/**
 * Simulates the bike's live telemetry connection. The static [ASSET_FILE_NAME] snapshot is used
 * as a template; each tick derives mock, incrementally-changing values from it rather than
 * emitting a frozen copy, so the "Bike live" screen visibly updates every minute.
 */
class FakeBikeTelemetryDataSourceImpl(
    private val context: Context,
    private val json: Json,
) : BikeTelemetryDataSource {

    private var cachedTemplate: BikeTelemetryDto? = null

    override fun observeTelemetry(
        initialTimestamp: Instant?,
        initialSession: Session?,
    ): Flow<BikeTelemetry> = flow {
        val template = loadTemplate()
        var timestamp = initialTimestamp ?: Instant.now()
        var session = initialSession ?: Session(durationS = 0, distanceKm = 0.0, maxSpeedKmh = 0.0)
        var tick = 0

        while (true) {
            val speed = mockCurrentSpeedKmh(tick)
            val base = template.toDomain(currentSpeedKmh = speed)

            val telemetry = base.copy(
                timestamp = timestamp,
                battery = base.battery.copy(
                    stateOfChargePct = max(0, base.battery.stateOfChargePct - tick),
                    estimatedRangeKm = max(0, base.battery.estimatedRangeKm - tick / 2),
                ),
                motor = base.motor,
                session = session,
            )

            emit(telemetry)

            delay(TICK_INTERVAL_MS.milliseconds)
            tick += 1
            timestamp = timestamp.plusSeconds(60)
            session = session.copy(
                durationS = session.durationS + 60,
                distanceKm = session.distanceKm + (speed * 60 / 3600),
                maxSpeedKmh = max(session.maxSpeedKmh, speed),
            )
        }
    }

    override suspend fun getDefaultSnapshot(): BikeTelemetry =
        loadTemplate().toDomain(currentSpeedKmh = 0.0)

    /** Deterministic mock speed curve so the live screen has believable, varying values. */
    private fun mockCurrentSpeedKmh(tick: Int): Double =
        min(
            abs((50.0 + 35.0 * sin(tick * 0.5))),
            120.0
        )

    private fun loadTemplate(): BikeTelemetryDto {
        cachedTemplate?.let { return it }
        val raw = context.assets.open(ASSET_FILE_NAME).bufferedReader().use { it.readText() }
        return json.decodeFromString(BikeTelemetryDto.serializer(), raw)
            .also { cachedTemplate = it }
    }
}
