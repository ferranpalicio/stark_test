package com.pal.starktest.features.sessions

import com.pal.starktest.domain.model.Session
import com.pal.starktest.features.common.UiState

data class SessionsUiState(
    val sessions: UiState<List<Session>> = UiState.Loading,
)
