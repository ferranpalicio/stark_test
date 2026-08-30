package com.pal.starktest.features.bikelive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pal.starktest.R
import com.pal.starktest.domain.model.Battery
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.ChargingState
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.Motor
import com.pal.starktest.domain.model.PowerMap
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.Warning
import com.pal.starktest.domain.model.WarningSeverity
import com.pal.starktest.features.common.UiState
import com.pal.starktest.ui.theme.StarkTheme
import java.time.Instant

@Composable
fun BikeLiveScreen(
    isRiding: Boolean,
    telemetry: UiState<BikeTelemetry>,
    modifier: Modifier = Modifier,
) {
    when {
        !isRiding -> EmptyState(
            modifier, "Not connected to bike. Enable riding in Settings to simulate a session."
        )

        telemetry is UiState.Loading -> LoadingState(modifier)
        telemetry is UiState.Error -> EmptyState(modifier, "Error: ${telemetry.message}")
        telemetry is UiState.Empty -> EmptyState(modifier, "Waiting for telemetry…")
        telemetry is UiState.Success -> LiveContent(telemetry.data, modifier)
    }
}

@Composable
private fun LiveContent(data: BikeTelemetry, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(StarkTheme.dimens.spacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopRowData(data.motor.temperatureC, data.battery.stateOfChargePct)
        Spacer(modifier = Modifier.padding(StarkTheme.dimens.spacingHuge))
        SpeedAndPower(
            data.currentSpeedKmh.toString(),
            data.motor.powerHp.toString(),
            data.rideSettings.maxPowerHp.toString()
        )
        Spacer(modifier = Modifier.padding(StarkTheme.dimens.spacingLarge))
        Text(
            style = MaterialTheme.typography.headlineSmall, text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.power_map))
                }
                append(" ")
                withStyle(style = SpanStyle(color = data.rideSettings.powerMap.getColor())) {
                    append(data.rideSettings.powerMap.name)
                }
            })
        if (data.diagnostics.warnings.isNotEmpty()) {
            Spacer(modifier = Modifier.padding(StarkTheme.dimens.spacingLarge))
            BikeWarningsComponent(data.diagnostics.warnings)
            Spacer(modifier = Modifier.padding(StarkTheme.dimens.spacingLarge))
        }
    }
}

@Composable
private fun ColumnScope.BikeWarningsComponent(warnings: List<Warning>) {
    Text(
        style = MaterialTheme.typography.headlineSmall,
        text = stringResource(R.string.warnings).uppercase()
    )
    LazyColumn(
        modifier = Modifier.weight(1f)
    ) {

        items(count = warnings.size) { index ->
            val warning = warnings[index]
            WarningCard(warning)
        }
    }
}

@Composable
private fun SpeedAndPower(
    currentSpeed: String, motorCurrentHp: String, settingsMaxPowerHp: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge,
                text = currentSpeed
            )
            Text(
                text = stringResource(R.string.kmh), style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.headlineSmall,
            text = stringResource(
                R.string.power_ratio, motorCurrentHp, settingsMaxPowerHp
            )
        )
    }
}

@Composable
private fun TopRowData(temperatureC: Double, batteryPct: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(StarkTheme.dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            text = stringResource(R.string.temperature, temperatureC)
        )
        BatteryGauge(width = 40.dp, height = 20.dp, percentage = batteryPct)
        Spacer(modifier = Modifier.padding(StarkTheme.dimens.spacingSmall))
        Text("$batteryPct %")
    }
}

@Composable
private fun WarningCard(warning: Warning) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(StarkTheme.dimens.spacingSmall),
        colors = CardDefaults.cardColors(
            containerColor = warning.severity.getColor().copy(alpha = 0.5f),
        ),

        ) {
        Column(
            modifier = Modifier.padding(StarkTheme.dimens.spacingLarge)
        ) {
            Text(
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                text = warning.code,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = warning.message, style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun WarningSeverity.getColor(): Color = when (this) {
    WarningSeverity.INFO -> Color(0xFF2196F3)
    WarningSeverity.WARNING -> Color(0xFFFFC107)
    WarningSeverity.CRITICAL -> Color(0xFFF44336)
}

private fun PowerMap.getColor(): Color = when (this) {
    PowerMap.ECO -> Color(0xFF4CAF50)
    PowerMap.ENDURO -> Color(0xFFFFC107)
    PowerMap.RALLY -> Color(0xFFFF5722)
    PowerMap.MX -> Color(0xFF9C27B0)
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
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = false, backgroundColor = 0xFFFFFF00)
@Composable
fun LiveContentPreview() {
    val previewTelemetry = BikeTelemetry(
        bike = Bike(
            model = "Stark VARG",
            variant = "EX",
            firmwareVersion = "v2.4.1",
            imageUrl = "https://cdn.starkfuture.com/varg-ex.png",
        ),
        timestamp = Instant.parse("2026-05-20T10:15:30Z"),
        battery = Battery(
            stateOfChargePct = 68,
            estimatedRangeKm = 52,
            temperatureC = 33.8,
            chargingState = ChargingState.DISCHARGING,
        ),
        motor = Motor(
            powerHp = 53.6,
            temperatureC = 71.2,
        ),
        rideSettings = RideSettings(
            powerMap = PowerMap.ENDURO,
            maxPowerHp = 60.0,
            engineBrakingPct = 35,
            regenPct = 28,
        ),
        session = Session(
            id = 42,
            durationS = 1364,
            distanceKm = 24.7,
            maxSpeedKmh = 93.4,
        ),
        diagnostics = Diagnostics(
            faultCodes = emptyList(),
            warnings = listOf(
                Warning(
                    code = "MTR_TEMP",
                    message = "Motor temperature elevated after prolonged climb.",
                    severity = WarningSeverity.WARNING,
                ),
                Warning(
                    code = "BAT_RNG",
                    message = "Estimated range dropping quickly in current power map.",
                    severity = WarningSeverity.INFO,
                ),
            ),
        ),
        currentSpeedKmh = 47.3,
    )

    StarkTheme {
        LiveContent(data = previewTelemetry, modifier = Modifier.background(Color.White))
    }
}

@Composable
private fun EmptyState(modifier: Modifier, message: String) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(StarkTheme.dimens.spacingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}
