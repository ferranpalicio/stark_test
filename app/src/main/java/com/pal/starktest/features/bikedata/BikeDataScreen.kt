package com.pal.starktest.features.bikedata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pal.starktest.domain.model.BikeOverview
import com.pal.starktest.features.common.UiState
import com.pal.starktest.ui.theme.StarkTheme

@Composable
fun BikeDataScreen(
    overview: UiState<BikeOverview>,
    modifier: Modifier = Modifier,
) {
    when (overview) {
        is UiState.Loading -> LoadingState(modifier)
        is UiState.Empty -> EmptyState(modifier, "No bike data available.")
        is UiState.Error -> EmptyState(modifier, "Error: ${overview.message}")
        is UiState.Success -> Content(overview.data, modifier)
    }
}

@Composable
private fun Content(data: BikeOverview, modifier: Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(StarkTheme.dimens.spacingLarge),
        verticalArrangement = Arrangement.spacedBy(StarkTheme.dimens.spacingMedium),
    ) {
        item { InfoCard("Model", "${data.bike.model} ${data.bike.variant}") }
        item { InfoCard("Firmware", data.bike.firmwareVersion) }
        item {
            val session = data.lastSession
            InfoCard(
                "Last session",
                if (session != null) {
                    "${session.durationS}s · ${session.distanceKm} km · max ${session.maxSpeedKmh} km/h"
                } else {
                    "No sessions yet"
                },
            )
        }
        item {
            InfoCard(
                "Fault codes",
                data.diagnostics.faultCodes.joinToString().ifEmpty { "None" })
        }
        items(data.diagnostics.warnings) { warning ->
            InfoCard("Warning: ${warning.code}", warning.message)
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String) {
    Card {
        Column(modifier = Modifier.padding(StarkTheme.dimens.spacingLarge)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { CircularProgressIndicator() }
}

@Composable
private fun EmptyState(modifier: Modifier, message: String) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(StarkTheme.dimens.spacingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { Text(message, style = MaterialTheme.typography.bodyLarge) }
}
