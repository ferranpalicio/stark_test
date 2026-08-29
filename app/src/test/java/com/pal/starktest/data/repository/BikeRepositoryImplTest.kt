package com.pal.starktest.data.repository

import app.cash.turbine.test
import com.pal.starktest.domain.datasource.BikeTelemetryDataSource
import com.pal.starktest.domain.datasource.LocalDataSource
import com.pal.starktest.domain.datasource.NetworkDataSource
import com.pal.starktest.domain.model.BatterySummary
import com.pal.starktest.domain.model.Battery
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.ChargingState
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.Motor
import com.pal.starktest.domain.model.PowerMap
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BikeRepositoryImplTest {

    private val local: LocalDataSource = mockk()
    private val network: NetworkDataSource = mockk()
    private val telemetry: BikeTelemetryDataSource = mockk()
    private lateinit var repository: BikeRepositoryImpl

    private val user = User(email = "a@b.com", name = "A B")
    private val bike = Bike(model = "VARG", variant = "Alpha", firmwareVersion = "1.0", imageUrl = "url")
    private val battery = Battery(stateOfChargePct = 90, estimatedRangeKm = 50, temperatureC = 30.0, chargingState = ChargingState.DISCHARGING)
    private val motor = Motor(powerHp = 50.0, temperatureC = 60.0)
    private val rideSettings = RideSettings(powerMap = PowerMap.ECO, maxPowerHp = 80.0, engineBrakingPct = 10, regenPct = 20)
    private val diagnostics = Diagnostics(faultCodes = emptyList(), warnings = emptyList())

    private fun snapshot(session: Session) = BikeTelemetry(
        bike = bike,
        timestamp = java.time.Instant.parse("2025-01-01T00:00:00Z"),
        battery = battery,
        motor = motor,
        rideSettings = rideSettings,
        session = session,
        diagnostics = diagnostics,
        currentSpeedKmh = 40.0,
    )

    @Before
    fun setUp() {
        repository = BikeRepositoryImpl(local, network, telemetry)
    }

    @Test
    fun `getUser returns cached user without hitting network`() = runTest {
        coEvery { local.getUser() } returns user

        val result = repository.getUser()

        assertEquals(user, result)
        coVerify(exactly = 0) { network.fetchUser() }
    }

    @Test
    fun `getUser fetches from network and caches when local is empty`() = runTest {
        coEvery { local.getUser() } returns null
        coEvery { network.fetchUser() } returns user
        coEvery { local.saveUser(user) } returns Unit

        val result = repository.getUser()

        assertEquals(user, result)
        coVerify { local.saveUser(user) }
    }

    @Test
    fun `getBikeOverview falls back to default snapshot when nothing stored locally`() = runTest {
        coEvery { local.getBike() } returns null
        coEvery { local.getBatterySummary() } returns null
        coEvery { local.getRideSettings() } returns null
        coEvery { local.getLastSession() } returns null
        coEvery { local.getDiagnostics() } returns null
        coEvery { telemetry.getDefaultSnapshot() } returns snapshot(Session(durationS = 100, distanceKm = 5.0, maxSpeedKmh = 60.0))

        val overview = repository.getBikeOverview()

        assertEquals(bike, overview.bike)
        assertEquals(BatterySummary(90, 50), overview.battery)
        assertEquals(100L, overview.lastSession?.durationS)
    }

    @Test
    fun `getBikeOverview prefers locally stored data over defaults`() = runTest {
        val storedSession = Session(id = 3, durationS = 500, distanceKm = 10.0, maxSpeedKmh = 80.0)
        coEvery { local.getBike() } returns bike
        coEvery { local.getBatterySummary() } returns BatterySummary(70, 30)
        coEvery { local.getRideSettings() } returns rideSettings
        coEvery { local.getLastSession() } returns storedSession
        coEvery { local.getDiagnostics() } returns diagnostics
        coEvery { telemetry.getDefaultSnapshot() } returns snapshot(Session(durationS = 0, distanceKm = 0.0, maxSpeedKmh = 0.0))

        val overview = repository.getBikeOverview()

        assertEquals(storedSession, overview.lastSession)
        assertEquals(BatterySummary(70, 30), overview.battery)
    }

    @Test
    fun `observeLiveTelemetry persists each snapshot and assigns the saved session id`() = runTest {
        coEvery { local.getLastSession() } returns null
        val emitted = snapshot(Session(durationS = 60, distanceKm = 1.0, maxSpeedKmh = 40.0))
        coEvery { telemetry.observeTelemetry(any(), any()) } returns flowOf(emitted)
        coEvery { local.saveBike(any()) } returns Unit
        coEvery { local.saveBatterySummary(any()) } returns Unit
        coEvery { local.saveRideSettings(any()) } returns Unit
        coEvery { local.saveSession(any()) } returns 42L
        coEvery { local.saveDiagnostics(any()) } returns Unit

        repository.observeLiveTelemetry().test {
            val result = awaitItem()
            assertEquals(42L, result.session.id)
            awaitComplete()
        }

        coVerify { local.saveBike(bike) }
        coVerify { local.saveBatterySummary(BatterySummary(90, 50)) }
        coVerify { local.saveSession(match { it.id == 0L && it.durationS == 60L }) }
    }
}
