package com.pal.starktest.data.local.datastore

import com.pal.starktest.domain.model.BatterySummary
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.FaultCode
import com.pal.starktest.domain.model.PowerMap
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.User
import com.pal.starktest.domain.model.Warning
import com.pal.starktest.domain.model.WarningSeverity
import kotlinx.serialization.Serializable

/**
 * Everything the app stores exactly once: the paired bike, the signed-in rider, the last known
 * battery summary, ride settings and diagnostics. These used to be single-row Room tables; a typed
 * DataStore models "zero or one value" natively (a `null` field is simply "never saved") and needs
 * no schema or migration.
 *
 * Ride sessions stay in Room — they are a growing, queryable list, which is what a database is for.
 * See [com.pal.starktest.data.local.dao.SessionDao]. The ride *in progress* is not persisted at all:
 * it lives in the telemetry flow while it runs and is written once, when it ends.
 *
 * These are persistence DTOs, deliberately separate from the domain models: enums are stored as
 * lowercase strings so an unrecognised value read back from disk degrades to a default instead of
 * throwing.
 */
@Serializable
data class StarkPreferences(
    val user: UserPrefs? = null,
    val bike: BikePrefs? = null,
    val batterySummary: BatterySummaryPrefs? = null,
    val rideSettings: RideSettingsPrefs? = null,
    val diagnostics: DiagnosticsPrefs? = null,
)

@Serializable
data class UserPrefs(
    val email: String,
    val name: String,
    val phone: String? = null,
    val country: String? = null,
) {
    fun toDomain() = User(email = email, name = name, phone = phone, country = country)

    companion object {
        fun from(user: User) = UserPrefs(
            email = user.email,
            name = user.name,
            phone = user.phone,
            country = user.country,
        )
    }
}

@Serializable
data class BikePrefs(
    val model: String,
    val variant: String,
    val firmwareVersion: String,
    val imageUrl: String,
) {
    fun toDomain() = Bike(
        model = model,
        variant = variant,
        firmwareVersion = firmwareVersion,
        imageUrl = imageUrl,
    )

    companion object {
        fun from(bike: Bike) = BikePrefs(
            model = bike.model,
            variant = bike.variant,
            firmwareVersion = bike.firmwareVersion,
            imageUrl = bike.imageUrl,
        )
    }
}

/** Only the fields the spec calls out: soc % and estimated range. */
@Serializable
data class BatterySummaryPrefs(
    val stateOfChargePct: Int,
    val estimatedRangeKm: Int,
) {
    fun toDomain() = BatterySummary(
        stateOfChargePct = stateOfChargePct,
        estimatedRangeKm = estimatedRangeKm,
    )

    companion object {
        fun from(summary: BatterySummary) = BatterySummaryPrefs(
            stateOfChargePct = summary.stateOfChargePct,
            estimatedRangeKm = summary.estimatedRangeKm,
        )
    }
}

@Serializable
data class RideSettingsPrefs(
    val powerMap: String,
    val maxPowerHp: Double,
    val engineBrakingPct: Int,
    val regenPct: Int,
) {
    fun toDomain() = RideSettings(
        powerMap = PowerMap.entries.firstOrNull { it.name.equals(powerMap, ignoreCase = true) }
            ?: PowerMap.ECO,
        maxPowerHp = maxPowerHp,
        engineBrakingPct = engineBrakingPct,
        regenPct = regenPct,
    )

    companion object {
        fun from(settings: RideSettings) = RideSettingsPrefs(
            powerMap = settings.powerMap.name.lowercase(),
            maxPowerHp = settings.maxPowerHp,
            engineBrakingPct = settings.engineBrakingPct,
            regenPct = settings.regenPct,
        )
    }
}

@Serializable
data class DiagnosticsPrefs(
    val faultCodes: List<FaultCode>,
    val warnings: List<WarningPrefs>,
) {
    fun toDomain() = Diagnostics(
        faultCodes = faultCodes,
        warnings = warnings.map { it.toDomain() },
    )

    companion object {
        fun from(diagnostics: Diagnostics) = DiagnosticsPrefs(
            faultCodes = diagnostics.faultCodes,
            warnings = diagnostics.warnings.map { WarningPrefs.from(it) },
        )
    }
}

@Serializable
data class WarningPrefs(
    val code: String,
    val message: String,
    val severity: String,
) {
    fun toDomain() = Warning(
        code = code,
        message = message,
        severity = WarningSeverity.entries.firstOrNull { it.name.equals(severity, ignoreCase = true) }
            ?: WarningSeverity.INFO,
    )

    companion object {
        fun from(warning: Warning) = WarningPrefs(
            code = warning.code,
            message = warning.message,
            severity = warning.severity.name.lowercase(),
        )
    }
}
