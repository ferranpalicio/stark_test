package com.pal.starktest.data.repository

import com.pal.starktest.domain.datasource.BikeTelemetryDataSource
import com.pal.starktest.domain.datasource.LocalDataSource
import com.pal.starktest.domain.datasource.NetworkDataSource
import com.pal.starktest.domain.model.BatterySummary
import com.pal.starktest.domain.model.BikeOverview
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.User
import com.pal.starktest.domain.repository.BikeRepository
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
        telemetry.observeTelemetry().collect { snapshot ->
            local.saveBike(snapshot.bike)
            local.saveBatterySummary(
                BatterySummary(snapshot.battery.stateOfChargePct, snapshot.battery.estimatedRangeKm),
            )
            local.saveRideSettings(snapshot.rideSettings)
            local.saveDiagnostics(snapshot.diagnostics)
            // The session totals are deliberately not written here: while the flow runs they *are*
            // the ride, and writing them per tick is what previously produced one ever-growing row.

            emit(snapshot)
        }
    }

    override suspend fun saveSession(session: Session) {
        // id = 0 so Room autogenerates: every finished ride is its own row.
        local.saveSession(session.copy(id = 0))
    }

    override suspend fun getSessions(): List<Session> = local.getSessions()

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
