package com.pal.starktest.features.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

sealed class AppDestination(val label: String, val icon: ImageVector) : NavKey {
    data object BikeLive : AppDestination("Bike live", Icons.Filled.PedalBike)
    data object BikeData : AppDestination("Bike data", Icons.Filled.DirectionsBike)
    data object UserData : AppDestination("User", Icons.Filled.Person)
    data object Settings : AppDestination("Settings", Icons.Filled.Settings)

    companion object {
        val bottomItems = listOf(BikeLive, BikeData, UserData, Settings)
    }
}
