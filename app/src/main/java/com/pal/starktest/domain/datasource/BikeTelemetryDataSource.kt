package com.pal.starktest.domain.datasource

import com.pal.starktest.domain.model.BikeTelemetry
import kotlinx.coroutines.flow.Flow

/**
 * Simulates the live connection to the bike. Emits one [BikeTelemetry] snapshot per tick.
 *
 * Every ride starts from zero: the flow is collected for exactly as long as the riding toggle is
 * on, and a ride is never left half-finished anywhere, so there is no session to resume from here.
 */
interface BikeTelemetryDataSource {
    fun observeTelemetry(): Flow<BikeTelemetry>

    /** The static mock snapshot bundled with the app, used as a fallback when there is no data. */
    suspend fun getDefaultSnapshot(): BikeTelemetry
}
