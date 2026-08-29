package com.pal.starktest.data.local

import com.pal.starktest.data.local.dao.BikeDao
import com.pal.starktest.data.local.dao.UserDao
import com.pal.starktest.data.local.entity.BatterySummaryEntity
import com.pal.starktest.data.local.entity.BikeEntity
import com.pal.starktest.data.local.entity.DiagnosticsEntity
import com.pal.starktest.data.local.entity.RideSettingsEntity
import com.pal.starktest.data.local.entity.SessionEntity
import com.pal.starktest.data.local.entity.UserEntity
import com.pal.starktest.domain.datasource.LocalDataSource
import com.pal.starktest.domain.model.BatterySummary
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.PowerMap
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.User
import com.pal.starktest.domain.model.Warning
import com.pal.starktest.domain.model.WarningSeverity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalDataSourceImpl(
    private val userDao: UserDao,
    private val bikeDao: BikeDao,
    private val json: Json,
) : LocalDataSource {

    override suspend fun getUser(): User? = userDao.get()?.let {
        User(email = it.email, name = it.name, phone = it.phone, country = it.country)
    }

    override suspend fun saveUser(user: User) {
        userDao.upsert(
            UserEntity(email = user.email, name = user.name, phone = user.phone, country = user.country),
        )
    }

    override suspend fun getBike(): Bike? = bikeDao.getBike()?.let {
        Bike(model = it.model, variant = it.variant, firmwareVersion = it.firmwareVersion, imageUrl = it.imageUrl)
    }

    override suspend fun saveBike(bike: Bike) {
        bikeDao.upsertBike(
            BikeEntity(
                model = bike.model,
                variant = bike.variant,
                firmwareVersion = bike.firmwareVersion,
                imageUrl = bike.imageUrl,
            ),
        )
    }

    override suspend fun getBatterySummary(): BatterySummary? = bikeDao.getBatterySummary()?.let {
        BatterySummary(stateOfChargePct = it.stateOfChargePct, estimatedRangeKm = it.estimatedRangeKm)
    }

    override suspend fun saveBatterySummary(summary: BatterySummary) {
        bikeDao.upsertBatterySummary(
            BatterySummaryEntity(
                stateOfChargePct = summary.stateOfChargePct,
                estimatedRangeKm = summary.estimatedRangeKm,
            ),
        )
    }

    override suspend fun getRideSettings(): RideSettings? = bikeDao.getRideSettings()?.let {
        RideSettings(
            powerMap = PowerMap.entries.firstOrNull { pm -> pm.name.equals(it.powerMap, ignoreCase = true) }
                ?: PowerMap.ECO,
            maxPowerHp = it.maxPowerHp,
            engineBrakingPct = it.engineBrakingPct,
            regenPct = it.regenPct,
        )
    }

    override suspend fun saveRideSettings(settings: RideSettings) {
        bikeDao.upsertRideSettings(
            RideSettingsEntity(
                powerMap = settings.powerMap.name.lowercase(),
                maxPowerHp = settings.maxPowerHp,
                engineBrakingPct = settings.engineBrakingPct,
                regenPct = settings.regenPct,
            ),
        )
    }

    override suspend fun getSessions(): List<Session> = bikeDao.getSessions().map { it.toDomain() }

    override suspend fun getLastSession(): Session? = bikeDao.getLastSession()?.toDomain()

    override suspend fun saveSession(session: Session): Long = bikeDao.upsertSession(
        SessionEntity(
            id = session.id,
            durationS = session.durationS,
            distanceKm = session.distanceKm,
            maxSpeedKmh = session.maxSpeedKmh,
        ),
    )

    override suspend fun getDiagnostics(): Diagnostics? = bikeDao.getDiagnostics()?.let { entity ->
        Diagnostics(
            faultCodes = json.decodeFromString(entity.faultCodesJson),
            warnings = json.decodeFromString<List<WarningEntry>>(entity.warningsJson).map { it.toDomain() },
        )
    }

    override suspend fun saveDiagnostics(diagnostics: Diagnostics) {
        bikeDao.upsertDiagnostics(
            DiagnosticsEntity(
                faultCodesJson = json.encodeToString(diagnostics.faultCodes),
                warningsJson = json.encodeToString(diagnostics.warnings.map { WarningEntry.from(it) }),
            ),
        )
    }

    private fun SessionEntity.toDomain() = Session(
        id = id,
        durationS = durationS,
        distanceKm = distanceKm,
        maxSpeedKmh = maxSpeedKmh,
    )
}

@kotlinx.serialization.Serializable
private data class WarningEntry(val code: String, val message: String, val severity: String) {
    fun toDomain() = Warning(
        code = code,
        message = message,
        severity = WarningSeverity.entries.firstOrNull { it.name.equals(severity, ignoreCase = true) }
            ?: WarningSeverity.INFO,
    )

    companion object {
        fun from(warning: Warning) = WarningEntry(warning.code, warning.message, warning.severity.name.lowercase())
    }
}
