package com.pal.starktest.features.bikelive

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.pal.starktest.R
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.FaultCode
import com.pal.starktest.domain.model.PowerMap
import com.pal.starktest.domain.model.Warning
import com.pal.starktest.domain.model.WarningSeverity
import com.pal.starktest.features.common.UiState
import com.pal.starktest.ui.theme.StarkTheme

@Composable
fun BikeLiveScreen(
    isRiding: Boolean,
    isLandscape: Boolean,
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
        telemetry is UiState.Success -> LiveContent(telemetry.data, isLandscape, modifier)
    }
}

@Composable
internal fun LiveContent(data: BikeTelemetry, isLandscape: Boolean, modifier: Modifier) {
    if (isLandscape) {
        LandscapeLayout(modifier, data)
    } else {
        PortraitLayout(modifier, data)
    }
}

@Composable
private fun LandscapeLayout(modifier: Modifier, data: BikeTelemetry) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(StarkTheme.dimens.spacingLarge),
    ) {
        TopRowData(data.motor.temperatureC, data.battery.stateOfChargePct)
        Row(modifier = Modifier.fillMaxSize()) {
            val hasWarnings: Boolean = data.diagnostics.warnings.isNotEmpty()
            val leftWeight = if (hasWarnings) 0.7f else 1f
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(leftWeight),
                verticalArrangement = Arrangement.Center,

                ) {
                SpeedAndPower(
                    data.currentSpeedKmh.toString(),
                    data.motor.powerHp.toString(),
                    data.rideSettings.maxPowerHp.toString()
                )
                Spacer(modifier = Modifier.padding(StarkTheme.dimens.spacingLarge))
                with(data.rideSettings.powerMap) {
                    PowerMapTextComponent(name, getColor())
                }
            }
            if (hasWarnings) {
                Column(
                    modifier = Modifier
                        .weight(0.3f)
                ) {
                    BikeIssuesComponent(
                        data.diagnostics,
                        titleAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun PortraitLayout(
    modifier: Modifier,
    data: BikeTelemetry
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(StarkTheme.dimens.spacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopRowData(data.motor.temperatureC, data.battery.stateOfChargePct)
        Spacer(modifier = Modifier.padding(StarkTheme.dimens.spacingHuge))
        SpeedAndPower(
            data.currentSpeedKmh.toString(),
            data.motor.powerHp.toString(),
            data.rideSettings.maxPowerHp.toString()
        )
        Spacer(modifier = Modifier.padding(StarkTheme.dimens.spacingLarge))
        with(data.rideSettings.powerMap) {
            PowerMapTextComponent(name, getColor())
        }
        if (data.diagnostics.warnings.isNotEmpty()) {
            Spacer(modifier = Modifier.padding(StarkTheme.dimens.spacingLarge))
            BikeIssuesComponent(
                data.diagnostics,
                titleAlign = TextAlign.Center,
                modifier.weight(1f)
            )
            Spacer(modifier = Modifier.padding(StarkTheme.dimens.spacingLarge))
        }
    }
}

@Composable
private fun PowerMapTextComponent(powerMap: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.headlineSmall, text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.power_map))
            }
            append(" ")
            withStyle(style = SpanStyle(color = color)) {
                append(powerMap)
            }
        }
    )
}

@Composable
private fun BikeIssuesComponent(
    diagnostics: Diagnostics,
    titleAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = titleAlign,
        style = MaterialTheme.typography.titleLarge,
        text = stringResource(R.string.warnings).uppercase()
    )
    LazyColumn(modifier = modifier) {
        items(count = diagnostics.faultCodes.size) { index ->
            val faultCode = diagnostics.faultCodes[index]
            ErrorCard(faultCode)
        }
        items(count = diagnostics.warnings.size) { index ->
            val warning = diagnostics.warnings[index]
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
private fun ErrorCard(faultCode: FaultCode) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(StarkTheme.dimens.spacingSmall),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF2614),
        ),

        ) {
        Column(
            modifier = Modifier.padding(StarkTheme.dimens.spacingLarge)
        ) {
            Text(
                text = faultCode.mapToUI(LocalContext.current),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun FaultCode.mapToUI(context: Context): String {
    return when (this) {
        FaultCode.MOTOR_OVERHEAT -> context.getString(R.string.motor_overheat_fault)
        FaultCode.SENSOR_FAILURE -> context.getString(R.string.sensor_failure_fault)
        FaultCode.UNKNOWN -> context.getString(R.string.unknown_failure_fault)
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
    WarningSeverity.CRITICAL -> Color(0xFFFD6559)
}

private fun PowerMap.getColor(): Color = when (this) {
    PowerMap.ECO -> Color(0xFF4CAF50)
    PowerMap.ENDURO -> Color(0xFFFFC107)
    PowerMap.RALLY -> Color(0xFFFF5722)
    PowerMap.MX -> Color(0xFF9C27B0)
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
        modifier = modifier
            .fillMaxSize()
            .padding(StarkTheme.dimens.spacingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}
