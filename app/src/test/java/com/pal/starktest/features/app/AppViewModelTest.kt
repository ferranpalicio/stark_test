package com.pal.starktest.features.app

import com.pal.starktest.domain.model.BatterySummary
import com.pal.starktest.domain.model.Battery
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.BikeOverview
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.ChargingState
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.Motor
import com.pal.starktest.domain.model.PowerMap
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.User
import com.pal.starktest.domain.repository.BikeRepository
import com.pal.starktest.features.common.UiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: BikeRepository = mockk()

    private val user = User(email = "a@b.com", name = "A B")
    private val overview = BikeOverview(
        bike = Bike(model = "VARG", variant = "Alpha", firmwareVersion = "1.0", imageUrl = "url"),
        battery = BatterySummary(90, 50),
        rideSettings = RideSettings(PowerMap.ECO, 80.0, 10, 20),
        lastSession = null,
        diagnostics = Diagnostics(emptyList(), emptyList()),
    )
    private val telemetry = BikeTelemetry(
        bike = overview.bike,
        timestamp = java.time.Instant.parse("2025-01-01T00:00:00Z"),
        battery = Battery(90, 50, 30.0, ChargingState.DISCHARGING),
        motor = Motor(50.0, 60.0),
        rideSettings = overview.rideSettings,
        session = Session(durationS = 0, distanceKm = 0.0, maxSpeedKmh = 0.0),
        diagnostics = overview.diagnostics,
        currentSpeedKmh = 40.0,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): AppViewModel {
        coEvery { repository.getUser() } returns user
        coEvery { repository.getBikeOverview() } returns overview
        return AppViewModel(repository)
    }

    @Test
    fun `init loads user and bike overview`() = runTest {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(UiState.Success(user), state.user)
        assertEquals(UiState.Success(overview), state.bikeOverview)
        assertEquals(UiState.Empty, state.liveTelemetry)
        assertTrue(!state.isRiding)
    }

    @Test
    fun `loadUser surfaces repository failure as error state`() = runTest {
        coEvery { repository.getUser() } throws IllegalStateException("boom")
        coEvery { repository.getBikeOverview() } returns overview

        val vm = AppViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Error("boom"), vm.uiState.value.user)
    }

    @Test
    fun `setRiding true starts telemetry collection`() = runTest {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()
        coEvery { repository.observeLiveTelemetry() } returns flowOf(telemetry)

        vm.setRiding(true)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isRiding)
        assertEquals(UiState.Success(telemetry), vm.uiState.value.liveTelemetry)
    }

    @Test
    fun `setRiding false stops telemetry and clears state`() = runTest {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()
        coEvery { repository.observeLiveTelemetry() } returns flowOf(telemetry)
        vm.setRiding(true)
        dispatcher.scheduler.advanceUntilIdle()

        vm.setRiding(false)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(!vm.uiState.value.isRiding)
        assertEquals(UiState.Empty, vm.uiState.value.liveTelemetry)
    }

    @Test
    fun `setRiding with same value is a no-op`() = runTest {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.setRiding(false)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(!vm.uiState.value.isRiding)
        assertEquals(UiState.Empty, vm.uiState.value.liveTelemetry)
    }
}
