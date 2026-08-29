package com.pal.starktest.features.bikelive

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
import androidx.compose.ui.unit.dp
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.features.common.UiState

@Composable
fun BikeLiveScreen(
    isRiding: Boolean,
    telemetry: UiState<BikeTelemetry>,
    modifier: Modifier = Modifier,
) {
    when {
        !isRiding -> EmptyState(modifier, "Not connected to bike. Enable riding in Settings to simulate a session.")
        telemetry is UiState.Loading -> LoadingState(modifier)
        telemetry is UiState.Error -> EmptyState(modifier, "Error: ${telemetry.message}")
        telemetry is UiState.Empty -> EmptyState(modifier, "Waiting for telemetry…")
        telemetry is UiState.Success -> LiveContent(telemetry.data, modifier)
    }
}

@Composable
private fun LiveContent(data: BikeTelemetry, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { InfoCard("Battery", "${data.battery.stateOfChargePct}% · ${data.battery.estimatedRangeKm} km range") }
        item { InfoCard("Power", "${data.motor.powerHp} hp / ${data.rideSettings.maxPowerHp} hp max") }
        item { InfoCard("Speed", "${data.currentSpeedKmh} km/h") }
        item { InfoCard("Temperature", "${data.motor.temperatureC} °C") }
        item { InfoCard("Power map", data.rideSettings.powerMap.name) }
        item { InfoCard("Session duration", "${data.session.durationS} s") }
        items(data.diagnostics.warnings) { warning ->
            InfoCard("Warning: ${warning.code}", warning.message)
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
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
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(modifier: Modifier, message: String) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}
