package com.pal.starktest.domain.datasource

import com.pal.starktest.domain.model.BatterySummary
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.User

/**
 * Local persistence (Room). Queries and stores user data and the bike-related data described in
 * the assessment: bike (single row), battery summary (single row), ride settings (single row),
 * session history (one row per session), and diagnostics.
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

    /** Upserts the session and returns its row id (stable across ticks of the same ride). */
    suspend fun saveSession(session: Session): Long

    suspend fun getDiagnostics(): Diagnostics?
    suspend fun saveDiagnostics(diagnostics: Diagnostics)
}
