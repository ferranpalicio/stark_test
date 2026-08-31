package com.pal.starktest.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.pal.starktest.data.local.dao.SessionDao
import com.pal.starktest.data.local.datastore.RideSettingsPrefs
import com.pal.starktest.data.local.datastore.StarkPreferences
import com.pal.starktest.data.local.datastore.StarkPreferencesSerializer
import com.pal.starktest.data.local.entity.SessionEntity
import com.pal.starktest.domain.model.BatterySummary
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.FaultCode
import com.pal.starktest.domain.model.PowerMap
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.User
import com.pal.starktest.domain.model.Warning
import com.pal.starktest.domain.model.WarningSeverity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Runs against a real file-backed DataStore rather than a mock: the round trip through JSON is the
 * part worth covering, and DataStore on the JVM needs nothing but a writable file. The session DAO
 * stays mocked, matching how the rest of the suite treats Room.
 */
class LocalDataSourceImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val sessionDao: SessionDao = mockk()
    private lateinit var dataStore: DataStore<StarkPreferences>
    private lateinit var dataSource: LocalDataSourceImpl

    @Before
    fun setUp() {
        dataStore = DataStoreFactory.create(
            serializer = StarkPreferencesSerializer,
            // Deliberately not created up front — a missing file is how DataStore represents
            // "first launch", and that is what the null-returning getters below assert.
            produceFile = { File(temporaryFolder.newFolder(), StarkPreferencesSerializer.FILE_NAME) },
        )
        dataSource = LocalDataSourceImpl(dataStore, sessionDao)
    }

    @Test
    fun `getters return null when nothing stored`() = runTest {
        assertNull(dataSource.getUser())
        assertNull(dataSource.getBike())
        assertNull(dataSource.getBatterySummary())
        assertNull(dataSource.getRideSettings())
        assertNull(dataSource.getDiagnostics())
    }

    @Test
    fun `saveUser round trips through data store`() = runTest {
        val user = User(email = "a@b.com", name = "A B", phone = "123", country = "ES")

        dataSource.saveUser(user)

        assertEquals(user, dataSource.getUser())
    }

    @Test
    fun `saveUser keeps null optional fields null`() = runTest {
        dataSource.saveUser(User(email = "a@b.com", name = "A B", phone = null, country = null))

        val stored = dataSource.getUser()

        assertNull(stored?.phone)
        assertNull(stored?.country)
    }

    @Test
    fun `saveBike round trips through data store`() = runTest {
        val bike = Bike(model = "VARG", variant = "Alpha", firmwareVersion = "1.0", imageUrl = "url")

        dataSource.saveBike(bike)

        assertEquals(bike, dataSource.getBike())
    }

    @Test
    fun `saveBatterySummary round trips through data store`() = runTest {
        val summary = BatterySummary(stateOfChargePct = 80, estimatedRangeKm = 40)

        dataSource.saveBatterySummary(summary)

        assertEquals(summary, dataSource.getBatterySummary())
    }

    @Test
    fun `saveRideSettings round trips through data store`() = runTest {
        val settings = RideSettings(
            powerMap = PowerMap.RALLY,
            maxPowerHp = 80.0,
            engineBrakingPct = 10,
            regenPct = 20,
        )

        dataSource.saveRideSettings(settings)

        assertEquals(settings, dataSource.getRideSettings())
    }

    @Test
    fun `saveRideSettings stores power map lowercased`() = runTest {
        dataSource.saveRideSettings(
            RideSettings(powerMap = PowerMap.RALLY, maxPowerHp = 80.0, engineBrakingPct = 10, regenPct = 20),
        )

        assertEquals("rally", dataStore.data.first().rideSettings?.powerMap)
    }

    @Test
    fun `getRideSettings falls back to ECO for unknown stored power map`() = runTest {
        dataStore.updateData { preferences ->
            preferences.copy(
                rideSettings = RideSettingsPrefs(
                    powerMap = "not-a-real-map",
                    maxPowerHp = 80.0,
                    engineBrakingPct = 10,
                    regenPct = 20,
                ),
            )
        }

        assertEquals(PowerMap.ECO, dataSource.getRideSettings()?.powerMap)
    }

    @Test
    fun `saveDiagnostics round trips nested warnings`() = runTest {
        val diagnostics = Diagnostics(
            faultCodes = listOf(FaultCode.MOTOR_OVERHEAT),
            warnings = listOf(Warning(code = "W1", message = "msg", severity = WarningSeverity.CRITICAL)),
        )

        dataSource.saveDiagnostics(diagnostics)

        assertEquals(diagnostics, dataSource.getDiagnostics())
    }

    @Test
    fun `saveDiagnostics stores severity lowercased`() = runTest {
        dataSource.saveDiagnostics(
            Diagnostics(
                faultCodes = listOf(FaultCode.MOTOR_OVERHEAT),
                warnings = listOf(Warning(code = "W1", message = "msg", severity = WarningSeverity.INFO)),
            ),
        )

        assertEquals("info", dataStore.data.first().diagnostics?.warnings?.single()?.severity)
    }

    @Test
    fun `saves are independent of each other`() = runTest {
        dataSource.saveUser(User(email = "a@b.com", name = "A B"))
        dataSource.saveBike(Bike(model = "VARG", variant = "Alpha", firmwareVersion = "1.0", imageUrl = "url"))

        assertEquals("a@b.com", dataSource.getUser()?.email)
        assertEquals("VARG", dataSource.getBike()?.model)
    }

    @Test
    fun `getLastSession maps entity to domain`() = runTest {
        coEvery { sessionDao.getLastSession() } returns
            SessionEntity(id = 5, durationS = 60, distanceKm = 1.0, maxSpeedKmh = 30.0)

        val session = dataSource.getLastSession()

        assertEquals(Session(id = 5, durationS = 60, distanceKm = 1.0, maxSpeedKmh = 30.0), session)
    }

    @Test
    fun `getSessions maps every entity to domain`() = runTest {
        coEvery { sessionDao.getSessions() } returns listOf(
            SessionEntity(id = 2, durationS = 120, distanceKm = 2.0, maxSpeedKmh = 40.0),
            SessionEntity(id = 1, durationS = 60, distanceKm = 1.0, maxSpeedKmh = 30.0),
        )

        val sessions = dataSource.getSessions()

        assertEquals(
            listOf(
                Session(id = 2, durationS = 120, distanceKm = 2.0, maxSpeedKmh = 40.0),
                Session(id = 1, durationS = 60, distanceKm = 1.0, maxSpeedKmh = 30.0),
            ),
            sessions,
        )
    }

    @Test
    fun `saveSession returns row id from dao`() = runTest {
        coEvery { sessionDao.insertSession(any()) } returns 7L

        val id = dataSource.saveSession(Session(id = 0, durationS = 60, distanceKm = 1.0, maxSpeedKmh = 30.0))

        assertEquals(7L, id)
        coVerify { sessionDao.insertSession(SessionEntity(id = 0, durationS = 60, distanceKm = 1.0, maxSpeedKmh = 30.0)) }
    }

}
