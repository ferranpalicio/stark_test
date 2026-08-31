package com.pal.starktest.features.sessions

import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.repository.BikeRepository
import com.pal.starktest.features.common.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: BikeRepository = mockk()

    private val sessions =
        listOf(Session(id = 1, durationS = 120, distanceKm = 2.0, maxSpeedKmh = 50.0))

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads sessions`() = runTest {
        coEvery { repository.getSessions() } returns sessions

        val vm = SessionsViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Success(sessions), vm.uiState.value.sessions)
        coVerify(exactly = 1) { repository.getSessions() }
    }

    @Test
    fun `state is Loading until the repository answers`() = runTest {
        coEvery { repository.getSessions() } returns sessions

        val vm = SessionsViewModel(repository)

        // Nothing has run on the test dispatcher yet.
        assertEquals(UiState.Loading, vm.uiState.value.sessions)
    }

    @Test
    fun `an empty history is a successful empty list, not an error`() = runTest {
        coEvery { repository.getSessions() } returns emptyList()

        val vm = SessionsViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Success(emptyList<Session>()), vm.uiState.value.sessions)
    }

    @Test
    fun `repository failure surfaces as error state`() = runTest {
        coEvery { repository.getSessions() } throws IllegalStateException("boom")

        val vm = SessionsViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Error("boom"), vm.uiState.value.sessions)
    }

    @Test
    fun `repository failure without a message falls back to a generic error`() = runTest {
        coEvery { repository.getSessions() } throws IllegalStateException()

        val vm = SessionsViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Error("Unknown error"), vm.uiState.value.sessions)
    }

    @Test
    fun `loadSessions re-reads and picks up a newly saved ride`() = runTest {
        coEvery { repository.getSessions() } returns sessions
        val vm = SessionsViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()
        val withNewRide =
            sessions + Session(id = 2, durationS = 15, distanceKm = 0.2, maxSpeedKmh = 40.0)
        coEvery { repository.getSessions() } returns withNewRide

        vm.loadSessions()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Success(withNewRide), vm.uiState.value.sessions)
        coVerify(exactly = 2) { repository.getSessions() }
    }

    @Test
    fun `a reload after a failure recovers`() = runTest {
        coEvery { repository.getSessions() } throws IllegalStateException("boom")
        val vm = SessionsViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()
        coEvery { repository.getSessions() } returns sessions

        vm.loadSessions()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Success(sessions), vm.uiState.value.sessions)
    }
}
