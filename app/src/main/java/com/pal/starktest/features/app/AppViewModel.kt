package com.pal.starktest.features.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pal.starktest.domain.repository.BikeRepository
import com.pal.starktest.features.common.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: BikeRepository,
) : ViewModel() {

    val uiState: StateFlow<AppUiState>
        field = MutableStateFlow(AppUiState())

    private var telemetryJob: Job? = null

    init {
        loadUser()
        loadBikeOverview()
    }

    fun setRiding(riding: Boolean) {
        if (uiState.value.isRiding == riding) return
        uiState.update { it.copy(isRiding = riding) }
        if (riding) startTelemetry() else endRide()
    }

    /**
     * The ride is whatever the telemetry flow last emitted, so read it before [stopTelemetry]
     * clears it. A ride that never got past its first tick has nothing worth recording.
     *
     * Flipping the toggle off is the *only* way a ride reaches history: nothing is written while it
     * runs, so a process death during a ride discards it.
     */
    private fun endRide() {
        val session = (uiState.value.liveTelemetry as? UiState.Success)?.data?.session
        stopTelemetry()
        viewModelScope.launch {
            if (session != null && session.durationS > 0) {
                runCatching { repository.saveSession(session) }
            }
            loadBikeOverview()
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            uiState.update { it.copy(user = UiState.Loading) }
            runCatching { repository.getUser() }
                .onSuccess { user -> uiState.update { it.copy(user = UiState.Success(user)) } }
                .onFailure { e -> uiState.update { it.copy(user = UiState.Error(e.message ?: "Unknown error")) } }
        }
    }

    /** The overview carries `lastSession`, so it has to be re-read once a ride is saved. */
    private fun loadBikeOverview() {
        viewModelScope.launch {
            uiState.update { it.copy(bikeOverview = UiState.Loading) }
            runCatching { repository.getBikeOverview() }
                .onSuccess { overview -> uiState.update { it.copy(bikeOverview = UiState.Success(overview)) } }
                .onFailure { e -> uiState.update { it.copy(bikeOverview = UiState.Error(e.message ?: "Unknown error")) } }
        }
    }

    private fun startTelemetry() {
        telemetryJob?.cancel()
        telemetryJob = repository.observeLiveTelemetry()
            .onStart { uiState.update { it.copy(liveTelemetry = UiState.Loading) } }
            .catch { e -> uiState.update { it.copy(liveTelemetry = UiState.Error(e.message ?: "Unknown error")) } }
            .onEach { telemetry -> uiState.update { it.copy(liveTelemetry = UiState.Success(telemetry)) } }
            .launchIn(viewModelScope)
    }

    private fun stopTelemetry() {
        telemetryJob?.cancel()
        telemetryJob = null
        uiState.update { it.copy(liveTelemetry = UiState.Empty) }
    }
}
