package com.pal.starktest.domain.model

import java.time.Instant

/**
 * Full telemetry snapshot as emitted by [com.pal.starktest.domain.datasource.BikeTelemetryDataSource].
 * Mirrors the JSON payload from the assessment brief, plus [currentSpeedKmh] which is a
 * live-only mock field (not part of the original schema, not persisted).
 */
data class BikeTelemetry(
    val bike: Bike,
    val timestamp: Instant,
    val battery: Battery,
    val motor: Motor,
    val rideSettings: RideSettings,
    val session: Session,
    val diagnostics: Diagnostics,
    val currentSpeedKmh: Double,
)

/**
 * Aggregate used by the "Bike data" screen: last known state of the bike when it is not
 * actively streaming live telemetry.
 */
data class BikeOverview(
    val bike: Bike,
    val battery: BatterySummary,
    val rideSettings: RideSettings,
    val lastSession: Session?,
    val diagnostics: Diagnostics,
)
