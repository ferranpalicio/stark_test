package com.pal.starktest.domain.datasource

import com.pal.starktest.domain.model.BatterySummary
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.User

/**
 * Local persistence. Stores the user and the bike-related data described in the assessment:
 * bike, battery summary, ride settings and diagnostics — each a single value backed by DataStore —
 * plus session history, a list backed by Room.
 *
 * A `null` getter result means "never saved", which callers treat as a cue to fall back to
 * network or telemetry defaults.
 */
interface LocalDataSource {
    suspend fun getUser(): User?
    suspend fun saveUser(user: User)

    suspend fun getBike(): Bike?
    suspend fun saveBike(bike: Bike)

    suspend fun getBatterySummary(): BatterySummary?
    suspend fun saveBatterySummary(summary: BatterySummary)

    suspend fun getRideSettings(): RideSettings?
    suspend fun saveRideSettings(settings: RideSettings)

    suspend fun getSessions(): List<Session>
    suspend fun getLastSession(): Session?

    /** Inserts the session as a new history row and returns its generated id. */
    suspend fun saveSession(session: Session): Long

    suspend fun getDiagnostics(): Diagnostics?
    suspend fun saveDiagnostics(diagnostics: Diagnostics)
}
