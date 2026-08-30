package com.pal.starktest.data.local

import androidx.datastore.core.DataStore
import com.pal.starktest.data.local.dao.SessionDao
import com.pal.starktest.data.local.datastore.BatterySummaryPrefs
import com.pal.starktest.data.local.datastore.BikePrefs
import com.pal.starktest.data.local.datastore.DiagnosticsPrefs
import com.pal.starktest.data.local.datastore.RideSettingsPrefs
import com.pal.starktest.data.local.datastore.StarkPreferences
import com.pal.starktest.data.local.datastore.UserPrefs
import com.pal.starktest.data.local.entity.SessionEntity
import com.pal.starktest.domain.datasource.LocalDataSource
import com.pal.starktest.domain.model.BatterySummary
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.User
import kotlinx.coroutines.flow.first

/**
 * Splits local storage two ways: single-valued state goes to [dataStore], the growing list of ride
 * sessions to [sessionDao].
 *
 * Every getter reads the current snapshot with [first]; every setter goes through
 * [DataStore.updateData], which serialises concurrent writers and only persists once the new value
 * is safely on disk.
 */
class LocalDataSourceImpl(
    private val dataStore: DataStore<StarkPreferences>,
    private val sessionDao: SessionDao,
) : LocalDataSource {

    private suspend fun preferences(): StarkPreferences = dataStore.data.first()

    override suspend fun getUser(): User? = preferences().user?.toDomain()

    override suspend fun saveUser(user: User) {
        dataStore.updateData { it.copy(user = UserPrefs.from(user)) }
    }

    override suspend fun getBike(): Bike? = preferences().bike?.toDomain()

    override suspend fun saveBike(bike: Bike) {
        dataStore.updateData { it.copy(bike = BikePrefs.from(bike)) }
    }

    override suspend fun getBatterySummary(): BatterySummary? =
        preferences().batterySummary?.toDomain()

    override suspend fun saveBatterySummary(summary: BatterySummary) {
        dataStore.updateData { it.copy(batterySummary = BatterySummaryPrefs.from(summary)) }
    }

    override suspend fun getRideSettings(): RideSettings? = preferences().rideSettings?.toDomain()

    override suspend fun saveRideSettings(settings: RideSettings) {
        dataStore.updateData { it.copy(rideSettings = RideSettingsPrefs.from(settings)) }
    }

    override suspend fun getSessions(): List<Session> = sessionDao.getSessions().map { it.toDomain() }

    override suspend fun getLastSession(): Session? = sessionDao.getLastSession()?.toDomain()

    override suspend fun saveSession(session: Session): Long = sessionDao.upsertSession(
        SessionEntity(
            id = session.id,
            durationS = session.durationS,
            distanceKm = session.distanceKm,
            maxSpeedKmh = session.maxSpeedKmh,
        ),
    )

    override suspend fun getDiagnostics(): Diagnostics? = preferences().diagnostics?.toDomain()

    override suspend fun saveDiagnostics(diagnostics: Diagnostics) {
        dataStore.updateData { it.copy(diagnostics = DiagnosticsPrefs.from(diagnostics)) }
    }

    private fun SessionEntity.toDomain() = Session(
        id = id,
        durationS = durationS,
        distanceKm = distanceKm,
        maxSpeedKmh = maxSpeedKmh,
    )
}
