package com.pal.starktest.domain.datasource

import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.Session
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/**
 * Simulates the live connection to the bike. Emits one [BikeTelemetry] snapshot per minute.
 *
 * @param initialTimestamp last known timestamp, or null if this is the first connection of the
 * ride (a fresh session starts from zero).
 * @param initialSession last known session totals to resume from, or null to start a new session.
 */
interface BikeTelemetryDataSource {
    fun observeTelemetry(
        initialTimestamp: Instant? = null,
        initialSession: Session? = null,
    ): Flow<BikeTelemetry>

    /** The static mock snapshot bundled with the app, used as a fallback when there is no data. */
    suspend fun getDefaultSnapshot(): BikeTelemetry
}
