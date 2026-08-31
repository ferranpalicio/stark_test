package com.pal.starktest.features.bikelive

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.pal.starktest.R
import com.pal.starktest.domain.model.BikeTelemetry
import com.pal.starktest.domain.model.Diagnostics
import com.pal.starktest.domain.model.FaultCode
import com.pal.starktest.features.common.UiState
import com.pal.starktest.ui.theme.StarkTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Screen-level tests for [BikeLiveScreen]. The screen is stateless, so each test drives it
 * directly with the same `bikeTelemetry` fixture the `@Preview`s use — see `previews.kt`.
 */
class BikeLiveScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    /** Landscape phones only, matching the widthDp/heightDp of [LiveContentPreviewLandscape]. */
    private val landscapeViewport = DpSize(800.dp, 400.dp)

    private fun setScreen(
        isRiding: Boolean = true,
        isLandscape: Boolean = false,
        telemetry: UiState<BikeTelemetry> = UiState.Success(bikeTelemetry),
    ) = rule.setContent {
        val screen: @Composable () -> Unit = {
            StarkTheme {
                BikeLiveScreen(
                    isRiding = isRiding,
                    isLandscape = isLandscape,
                    telemetry = telemetry,
                )
            }
        }
        // Force the viewport rather than inherit the device's, so the landscape branch is
        // exercised on a portrait emulator and the bounds assertions stay device-independent.
        if (isLandscape) {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(landscapeViewport),
            ) { screen() }
        } else {
            screen()
        }
    }

    private fun string(resId: Int, vararg args: Any) = rule.activity.getString(resId, *args)

    // --- state branches -----------------------------------------------------------------------

    @Test
    fun notRidingShowsTheConnectPrompt() {
        setScreen(isRiding = false, telemetry = UiState.Empty)

        rule.onNodeWithText(
            "Not connected to bike. Enable riding in Settings to simulate a session."
        ).assertIsDisplayed()
    }

    @Test
    fun notRidingWinsOverTelemetryThatAlreadyArrived() {
        // The riding gate is checked before the UiState, so a stale Success must not leak through.
        setScreen(isRiding = false, telemetry = UiState.Success(bikeTelemetry))

        rule.onNodeWithText(bikeTelemetry.currentSpeedKmh.toString()).assertDoesNotExist()
        rule.onNodeWithText(
            "Not connected to bike. Enable riding in Settings to simulate a session."
        ).assertIsDisplayed()
    }

    @Test
    fun loadingShowsAnIndeterminateProgressIndicator() {
        setScreen(telemetry = UiState.Loading)

        rule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun emptyShowsTheWaitingMessage() {
        setScreen(telemetry = UiState.Empty)

        rule.onNodeWithText("Waiting for telemetry…").assertIsDisplayed()
    }

    @Test
    fun errorShowsTheFailureMessage() {
        setScreen(telemetry = UiState.Error("boom"))

        rule.onNodeWithText("Error: boom").assertIsDisplayed()
    }

    // --- success content ----------------------------------------------------------------------

    @Test
    fun successRendersEverySpeedAndPowerReadout() {
        setScreen()

        rule.onNodeWithText("47.3").assertIsDisplayed()
        rule.onNodeWithText(string(R.string.kmh)).assertIsDisplayed()
        rule.onNodeWithText(string(R.string.power_ratio, "53.6", "60.0")).assertIsDisplayed()
    }

    @Test
    fun successRendersTheTemperatureAndBatteryHeader() {
        setScreen()

        rule.onNodeWithText(string(R.string.temperature, bikeTelemetry.motor.temperatureC))
            .assertIsDisplayed()
        rule.onNodeWithText("68 %").assertIsDisplayed()
        // BatteryGauge is a Canvas, so its only identity is the semantics it publishes.
        rule.onNodeWithContentDescription("Battery 68 percent").assertIsDisplayed()
    }

    @Test
    fun successRendersThePowerMapAsOneLabel() {
        setScreen()

        // Built with buildAnnotatedString, so the label and the value are a single text node.
        rule.onNodeWithText("${string(R.string.power_map)} ENDURO").assertIsDisplayed()
    }

    // --- diagnostics --------------------------------------------------------------------------

    @Test
    fun diagnosticsListsFaultCodesAndWarnings() {
        setScreen()

        rule.onNodeWithText(string(R.string.warnings).uppercase()).assertIsDisplayed()
        rule.onNodeWithText(string(R.string.motor_overheat_fault)).assertIsDisplayed()
        rule.onNodeWithText("MTR_TEMP").assertIsDisplayed()

        // The list is a LazyColumn: the tail is not composed until it is scrolled into view.
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("BAT_LIMIT"))
        rule.onNodeWithText("BAT_LIMIT").assertIsDisplayed()
        rule.onNodeWithText("Battery state of charge below 5%, consider recharging soon.")
            .assertIsDisplayed()
    }

    @Test
    fun noDiagnosticsHidesTheWholeWarningsSection() {
        setScreen(
            telemetry = UiState.Success(
                bikeTelemetry.copy(diagnostics = Diagnostics(emptyList(), emptyList()))
            )
        )

        rule.onNodeWithText(string(R.string.warnings).uppercase()).assertDoesNotExist()
        // The readouts are still there — only the diagnostics block is conditional.
        rule.onNodeWithText("47.3").assertIsDisplayed()
    }

    @Test
    fun faultCodesWithoutWarningsRenderNothing() {
        setScreen(
            telemetry = UiState.Success(
                bikeTelemetry.copy(
                    diagnostics = Diagnostics(
                        faultCodes = listOf(FaultCode.MOTOR_OVERHEAT),
                        warnings = emptyList(),
                    )
                )
            )
        )

        // The section is gated on warnings only, so a fault code alone is invisible to the rider.
        rule.onNodeWithText(string(R.string.warnings).uppercase()).assertDoesNotExist()
        rule.onNodeWithText(string(R.string.motor_overheat_fault)).assertDoesNotExist()
    }

    // --- layout variants ----------------------------------------------------------------------

    @Test
    fun portraitStacksTheDiagnosticsBelowTheSpeed() {
        setScreen(isLandscape = false)

        val speed = rule.onNodeWithText("47.3").getUnclippedBoundsInRoot()
        val warnings = rule.onNodeWithText(string(R.string.warnings).uppercase())
            .getUnclippedBoundsInRoot()

        assertTrue(
            "Expected warnings below the speed, got speed=$speed warnings=$warnings",
            warnings.top > speed.bottom,
        )
        assertTrue(
            "Expected warnings to share the speed's column, got speed=$speed warnings=$warnings",
            warnings.left < speed.right,
        )
    }

    @Test
    fun landscapePutsTheDiagnosticsBesideTheSpeed() {
        setScreen(isLandscape = true)

        val speed = rule.onNodeWithText("47.3").getUnclippedBoundsInRoot()
        val warnings = rule.onNodeWithText(string(R.string.warnings).uppercase())
            .getUnclippedBoundsInRoot()

        assertTrue(
            "Expected warnings right of the speed, got speed=$speed warnings=$warnings",
            warnings.left > speed.right,
        )
        assertTrue(
            "Expected the two columns to overlap vertically, got speed=$speed warnings=$warnings",
            warnings.top < speed.bottom,
        )
    }

    @Test
    fun landscapeStillRendersEveryReadout() {
        setScreen(isLandscape = true)

        rule.onNodeWithText("47.3").assertIsDisplayed()
        rule.onNodeWithText(string(R.string.power_ratio, "53.6", "60.0")).assertIsDisplayed()
        rule.onNodeWithText("${string(R.string.power_map)} ENDURO").assertIsDisplayed()
        rule.onNodeWithContentDescription("Battery 68 percent").assertIsDisplayed()
    }
}
