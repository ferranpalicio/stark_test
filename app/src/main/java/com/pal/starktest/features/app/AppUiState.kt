package com.pal.starktest.features.app

import com.pal.starktest.domain.model.BikeOverview
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.User
import com.pal.starktest.features.common.UiState

data class AppUiState(
    val isRiding: Boolean = false,
    val user: UiState<User> = UiState.Loading,
    val bikeOverview: UiState<BikeOverview> = UiState.Loading,
    val liveTelemetry: UiState<BikeTelemetry> = UiState.Empty,
    val sessions: UiState<List<Session>> = UiState.Loading,
)
