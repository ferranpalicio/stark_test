package com.pal.starktest.features.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pal.starktest.domain.repository.BikeRepository
import com.pal.starktest.features.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns session history for the Sessions destination only. It is scoped to the nav entry, so the
 * list is re-read every time the tab is entered — that is what keeps it in sync with a ride that
 * just ended, since a finished ride is written by `AppViewModel` and nothing pushes an update here.
 */
class SessionsViewModel(
    private val repository: BikeRepository,
) : ViewModel() {

    val uiState: StateFlow<SessionsUiState>
        field = MutableStateFlow(SessionsUiState())

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            uiState.update { it.copy(sessions = UiState.Loading) }
            runCatching { repository.getSessions() }
                .onSuccess { sessions -> uiState.update { it.copy(sessions = UiState.Success(sessions)) } }
                .onFailure { e -> uiState.update { it.copy(sessions = UiState.Error(e.message ?: "Unknown error")) } }
        }
    }
}
