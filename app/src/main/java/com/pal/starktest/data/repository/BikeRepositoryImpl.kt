package com.pal.starktest.data.repository

import com.pal.starktest.domain.datasource.BikeTelemetryDataSource
import com.pal.starktest.domain.datasource.LocalDataSource
import com.pal.starktest.domain.datasource.NetworkDataSource
import com.pal.starktest.domain.model.BatterySummary
import com.pal.starktest.domain.model.BikeOverview
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.User
import com.pal.starktest.domain.repository.BikeRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BikeRepositoryImpl(
    private val local: LocalDataSource,
    private val network: NetworkDataSource,
    private val telemetry: BikeTelemetryDataSource,
) : BikeRepository {

    override suspend fun getUser(): User =
        local.getUser() ?: network.fetchUser().also { local.saveUser(it) }

    override fun observeLiveTelemetry(): Flow<BikeTelemetry> = flow {
        val lastSession = local.getLastSession()
        // A resumed session implies telemetry was flowing before; a fresh one starts from zero.
        val initialTimestamp = lastSession?.let { Instant.now() }
        var activeSessionId = lastSession?.id ?: 0L

        telemetry.observeTelemetry(initialTimestamp, lastSession).collect { snapshot ->
            local.saveBike(snapshot.bike)
            local.saveBatterySummary(
                BatterySummary(snapshot.battery.stateOfChargePct, snapshot.battery.estimatedRangeKm),
            )
            local.saveRideSettings(snapshot.rideSettings)
            val sessionToPersist = snapshot.session.copy(id = activeSessionId)
            activeSessionId = local.saveSession(sessionToPersist)
            local.saveDiagnostics(snapshot.diagnostics)

            emit(snapshot.copy(session = sessionToPersist.copy(id = activeSessionId)))
        }
    }

    override suspend fun getBikeOverview(): BikeOverview {
        val fallback = telemetry.getDefaultSnapshot()
        val bike = local.getBike() ?: fallback.bike
        val battery = local.getBatterySummary()
            ?: BatterySummary(fallback.battery.stateOfChargePct, fallback.battery.estimatedRangeKm)
        val rideSettings = local.getRideSettings() ?: fallback.rideSettings
        val lastSession = local.getLastSession() ?: fallback.session
        val diagnostics = local.getDiagnostics() ?: fallback.diagnostics

        return BikeOverview(
            bike = bike,
            battery = battery,
            rideSettings = rideSettings,
            lastSession = lastSession,
            diagnostics = diagnostics,
        )
    }
}
