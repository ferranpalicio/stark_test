package com.pal.starktest.domain.repository

import com.pal.starktest.domain.model.BikeOverview
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.Session
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
     * Live telemetry stream while the bike is "riding". Each emitted snapshot updates the cached
     * bike state; the running session totals it carries are *not* persisted — the flow itself is
     * the ride in progress, and it only reaches history via [saveSession] when it ends.
     */
    fun observeLiveTelemetry(): Flow<BikeTelemetry>

    /** Appends a finished ride to session history as a new row. */
    suspend fun saveSession(session: Session)

    /** Completed ride sessions, most recent first. */
    suspend fun getSessions(): List<Session>

    /**
     * Last known bike state for the "Bike data" screen. Falls back to the bundled mock data if
     * nothing has been saved locally yet.
     */
    suspend fun getBikeOverview(): BikeOverview
}
