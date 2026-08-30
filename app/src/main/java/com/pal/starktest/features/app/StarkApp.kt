package com.pal.starktest.features.app

    import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.waterfall
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.pal.starktest.features.bikedata.BikeDataScreen
import com.pal.starktest.features.bikelive.BikeLiveScreen
import com.pal.starktest.features.sessions.SessionsScreen
import com.pal.starktest.features.settings.SettingsScreen
import com.pal.starktest.features.userdata.UserDataScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun StarkApp(viewModel: AppViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // The app always launches not riding, and BikeLive is not in the bar then.
    val backStack = rememberNavBackStack(AppDestination.BikeData)
    val current = backStack.lastOrNull() as? AppDestination ?: AppDestination.BikeData
    val items = AppDestination.itemsFor(uiState.isRiding)
    // Compact height == below the 480dp medium breakpoint, i.e. landscape phones. Rail there,
    // bottom bar everywhere else.
    val isLandscape = !currentWindowAdaptiveInfoV2().windowSizeClass
        .isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

    fun navigateTo(destination: AppDestination) {
        backStack.clear()
        backStack.add(destination)
    }

    // Toggling the ride drops one tab from the bar; move off it if that is where we are. This also
    // covers process restore: the back stack is saved but isRiding is not, so a restore onto
    // BikeLive lands on Sessions.
    LaunchedEffect(uiState.isRiding, current) {
        when {
            uiState.isRiding && current == AppDestination.Sessions ->
                navigateTo(AppDestination.BikeLive)

            !uiState.isRiding && current == AppDestination.BikeLive ->
                navigateTo(AppDestination.Sessions)
        }
    }

    Scaffold(
        bottomBar = {
            if (!isLandscape) {
                NavigationBar {
                    items.forEach { destination ->
                        NavigationBarItem(
                            selected = destination == current,
                            onClick = { navigateTo(destination) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Row(modifier = Modifier.padding(padding)) {
            if (isLandscape) {
                NavigationRail(
                    windowInsets = WindowInsets.waterfall
                ) {
                    Spacer(Modifier.weight(1f))
                    items.forEach { destination ->
                        NavigationRailItem(
                            selected = destination == current,
                            onClick = { navigateTo(destination) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
            NavDisplay(
                backStack = backStack,
                entryProvider = entryProvider {
                    entry<AppDestination.BikeLive> {
                        BikeLiveScreen(isRiding = uiState.isRiding, telemetry = uiState.liveTelemetry)
                    }
                    entry<AppDestination.BikeData> {
                        BikeDataScreen(overview = uiState.bikeOverview)
                    }
                    entry<AppDestination.Sessions> {
                        SessionsScreen(sessions = uiState.sessions)
                    }
                    entry<AppDestination.UserData> {
                        UserDataScreen(user = uiState.user)
                    }
                    entry<AppDestination.Settings> {
                        SettingsScreen(isRiding = uiState.isRiding, onRidingChanged = viewModel::setRiding)
                    }
                },
            )
        }
    }
}
