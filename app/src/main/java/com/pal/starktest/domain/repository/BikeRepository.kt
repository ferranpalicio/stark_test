package com.pal.starktest.domain.repository

import com.pal.starktest.domain.model.BikeOverview
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for bike telemetry and user data. Reads/writes through
 * [com.pal.starktest.domain.datasource.LocalDataSource] and falls back to the bundled mock
 * snapshot (via [com.pal.starktest.domain.datasource.BikeTelemetryDataSource]) when the local
 * database has no data yet.
 */
interface BikeRepository {
    /** Returns the cached user, fetching and caching it from the network on first use. */
    suspend fun getUser(): User

    /**
     * Live telemetry stream while the bike is "riding". Resumes the last known timestamp/session
     * from local storage (if any) and persists every emitted snapshot.
     */
    fun observeLiveTelemetry(): Flow<BikeTelemetry>

    /**
     * Last known bike state for the "Bike data" screen. Falls back to the bundled mock data if
     * nothing has been saved locally yet.
     */
    suspend fun getBikeOverview(): BikeOverview
}
