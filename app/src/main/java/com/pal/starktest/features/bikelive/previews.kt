package com.pal.starktest.features.bikelive

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.pal.starktest.domain.model.Battery
import com.pal.starktest.domain.model.Bike
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.ChargingState
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.FaultCode
import com.pal.starktest.domain.model.Motor
import com.pal.starktest.domain.model.PowerMap
import com.pal.starktest.domain.model.RideSettings
import com.pal.starktest.domain.model.Session
import com.pal.starktest.domain.model.Warning
import com.pal.starktest.domain.model.WarningSeverity
import com.pal.starktest.ui.theme.StarkTheme
import java.time.Instant

val bikeTelemetry: BikeTelemetry = BikeTelemetry(
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
        faultCodes = listOf(FaultCode.MOTOR_OVERHEAT),
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
            Warning(
                code = "BAT_LIMIT",
                message = "Battery state of charge below 5%, consider recharging soon.",
                severity = WarningSeverity.CRITICAL,
            ),
        ),
    ),
    currentSpeedKmh = 47.3,
)

@Preview(showBackground = false, backgroundColor = 0xFFFFFF00)
@Composable
fun LiveContentPreviewPortrait() {
    StarkTheme {
        LiveContent(
            data = bikeTelemetry,
            isLandscape = false,
            modifier = Modifier.background(Color.White)
        )
    }
}

@Preview(showBackground = false, backgroundColor = 0xFFFFFF00, widthDp = 800, heightDp = 400)
@Composable
fun LiveContentPreviewLandscape(){
    StarkTheme {
        LiveContent(
            data = bikeTelemetry,
            isLandscape = true,
            modifier = Modifier.background(Color.White)
        )
    }
}