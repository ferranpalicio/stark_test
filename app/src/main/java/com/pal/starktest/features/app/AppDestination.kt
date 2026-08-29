package com.pal.starktest.features.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Nav 3 back stack entries are serialized by [androidx.navigation3.runtime.rememberNavBackStack],
 * so every destination must be `@Serializable` and carry no constructor state. [label] and [icon]
 * are getters, not stored properties, precisely so they stay out of serialization.
 */
sealed interface AppDestination : NavKey {
    val label: String
    val icon: ImageVector

    @Serializable
    data object BikeLive : AppDestination {
        override val label get() = "Bike live"
        override val icon get() = Icons.Filled.PedalBike
    }

    @Serializable
    data object BikeData : AppDestination {
        override val label get() = "Bike data"
        override val icon get() = Icons.AutoMirrored.Filled.DirectionsBike
    }

    @Serializable
    data object UserData : AppDestination {
        override val label get() = "User"
        override val icon get() = Icons.Filled.Person
    }

    @Serializable
    data object Settings : AppDestination {
        override val label get() = "Settings"
        override val icon get() = Icons.Filled.Settings
    }

    companion object {
        val bottomItems = listOf(BikeLive, BikeData, UserData, Settings)
    }
}
