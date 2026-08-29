package com.pal.starktest.data.local

import com.pal.starktest.data.local.dao.BikeDao
import com.pal.starktest.data.local.dao.UserDao
import com.pal.starktest.data.local.entity.BatterySummaryEntity
import com.pal.starktest.data.local.entity.BikeEntity
import com.pal.starktest.data.local.entity.DiagnosticsEntity
import com.pal.starktest.data.local.entity.RideSettingsEntity
import com.pal.starktest.data.local.entity.SessionEntity
import com.pal.starktest.data.local.entity.UserEntity
import com.pal.starktest.domain.model.BatterySummary
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.PowerMap
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.User
import com.pal.starktest.domain.model.Warning
import com.pal.starktest.domain.model.WarningSeverity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LocalDataSourceImplTest {

    private val userDao: UserDao = mockk()
    private val bikeDao: BikeDao = mockk()
    private lateinit var dataSource: LocalDataSourceImpl

    @Before
    fun setUp() {
        dataSource = LocalDataSourceImpl(userDao, bikeDao, Json)
    }

    @Test
    fun `getUser returns null when nothing stored`() = runTest {
        coEvery { userDao.get() } returns null

        assertNull(dataSource.getUser())
    }

    @Test
    fun `getUser maps entity to domain`() = runTest {
        coEvery { userDao.get() } returns UserEntity(email = "a@b.com", name = "A B", phone = "123", country = "ES")

        val user = dataSource.getUser()

        assertEquals(User(email = "a@b.com", name = "A B", phone = "123", country = "ES"), user)
    }

    @Test
    fun `saveUser upserts mapped entity`() = runTest {
        coEvery { userDao.upsert(any()) } returns Unit

        dataSource.saveUser(User(email = "a@b.com", name = "A B", phone = null, country = null))

        coVerify { userDao.upsert(UserEntity(email = "a@b.com", name = "A B", phone = null, country = null)) }
    }

    @Test
    fun `getBike maps entity to domain`() = runTest {
        coEvery { bikeDao.getBike() } returns BikeEntity(
            model = "VARG",
            variant = "Alpha",
            firmwareVersion = "1.0",
            imageUrl = "url",
        )

        val bike = dataSource.getBike()

        assertEquals(Bike(model = "VARG", variant = "Alpha", firmwareVersion = "1.0", imageUrl = "url"), bike)
    }

    @Test
    fun `getBatterySummary maps entity to domain`() = runTest {
        coEvery { bikeDao.getBatterySummary() } returns BatterySummaryEntity(stateOfChargePct = 80, estimatedRangeKm = 40)

        val summary = dataSource.getBatterySummary()

        assertEquals(BatterySummary(stateOfChargePct = 80, estimatedRangeKm = 40), summary)
    }

    @Test
    fun `getRideSettings falls back to ECO for unknown stored power map`() = runTest {
        coEvery { bikeDao.getRideSettings() } returns RideSettingsEntity(
            powerMap = "not-a-real-map",
            maxPowerHp = 80.0,
            engineBrakingPct = 10,
            regenPct = 20,
        )

        val settings = dataSource.getRideSettings()

        assertEquals(PowerMap.ECO, settings?.powerMap)
    }

    @Test
    fun `saveRideSettings stores power map lowercased`() = runTest {
        coEvery { bikeDao.upsertRideSettings(any()) } returns Unit

        dataSource.saveRideSettings(
            RideSettings(powerMap = PowerMap.RALLY, maxPowerHp = 80.0, engineBrakingPct = 10, regenPct = 20),
        )

        coVerify {
            bikeDao.upsertRideSettings(
                RideSettingsEntity(powerMap = "rally", maxPowerHp = 80.0, engineBrakingPct = 10, regenPct = 20),
            )
        }
    }

    @Test
    fun `getLastSession maps entity to domain`() = runTest {
        coEvery { bikeDao.getLastSession() } returns SessionEntity(id = 5, durationS = 60, distanceKm = 1.0, maxSpeedKmh = 30.0)

        val session = dataSource.getLastSession()

        assertEquals(Session(id = 5, durationS = 60, distanceKm = 1.0, maxSpeedKmh = 30.0), session)
    }

    @Test
    fun `saveSession returns row id from dao`() = runTest {
        coEvery { bikeDao.upsertSession(any()) } returns 7L

        val id = dataSource.saveSession(Session(id = 0, durationS = 60, distanceKm = 1.0, maxSpeedKmh = 30.0))

        assertEquals(7L, id)
        coVerify { bikeDao.upsertSession(SessionEntity(id = 0, durationS = 60, distanceKm = 1.0, maxSpeedKmh = 30.0)) }
    }

    @Test
    fun `getDiagnostics decodes json columns`() = runTest {
        coEvery { bikeDao.getDiagnostics() } returns DiagnosticsEntity(
            faultCodesJson = """["E01"]""",
            warningsJson = """[{"code":"W1","message":"msg","severity":"critical"}]""",
        )

        val diagnostics = dataSource.getDiagnostics()

        assertEquals(
            Diagnostics(
                faultCodes = listOf("E01"),
                warnings = listOf(Warning(code = "W1", message = "msg", severity = WarningSeverity.CRITICAL)),
            ),
            diagnostics,
        )
    }

    @Test
    fun `saveDiagnostics encodes fields as json`() = runTest {
        coEvery { bikeDao.upsertDiagnostics(any()) } returns Unit

        dataSource.saveDiagnostics(
            Diagnostics(
                faultCodes = listOf("E01"),
                warnings = listOf(Warning(code = "W1", message = "msg", severity = WarningSeverity.INFO)),
            ),
        )

        coVerify {
            bikeDao.upsertDiagnostics(
                DiagnosticsEntity(
                    faultCodesJson = """["E01"]""",
                    warningsJson = """[{"code":"W1","message":"msg","severity":"info"}]""",
                ),
            )
        }
    }
}
