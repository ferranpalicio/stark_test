package com.pal.starktest.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standardized spacing / sizing tokens. Use these via [AppTheme.dimens] instead of hardcoding
 * `dp` values in components so the whole app stays visually consistent.
 */
@Immutable
data class Dimensions(
    // Element sizes (icons, touch targets, fixed widths/heights).
    val sizeMicro: Dp,
    val sizeExtraTiny: Dp,
    val sizeTiny: Dp,
    val sizeExtraSmall: Dp,
    val sizeSmall: Dp,
    val sizeMedium: Dp,
    val sizeLarge: Dp,
    val sizeHuge: Dp,
    val sizeExtraLarge: Dp,
    val sizeExtraHuge: Dp,
    // Spacing (padding, gaps between elements).
    val spacingTiny: Dp,
    val spacingSmall: Dp,
    val spacingMedium: Dp,
    val spacingLarge: Dp,
    val spacingExtraLarge: Dp,
    val spacingHuge: Dp,
    // Border / stroke widths.
    val borderWidthTiny: Dp,
    val borderWidthSmall: Dp,
    val borderWidthMedium: Dp,
    val borderWidthLarge: Dp,
    val borderWidthHuge: Dp,
    val borderWidthFocusInner: Dp,
    val borderWidthFocusOuter: Dp,
    // Corner radii.
    val borderRadiusTiny: Dp,
    val borderRadiusSmall: Dp,
    val borderRadiusMedium: Dp,
    val borderRadiusLarge: Dp,
    val borderRadiusHuge: Dp,
    val borderRadiusButton: Dp,
    val borderRadiusInput: Dp,
)

/** Default token scale. Swap values here to retune the whole app. */
val defaultDimensions = Dimensions(
    sizeMicro = 4.dp,
    sizeExtraTiny = 8.dp,
    sizeTiny = 12.dp,
    sizeExtraSmall = 16.dp,
    sizeSmall = 24.dp,
    sizeMedium = 32.dp,
    sizeLarge = 40.dp,
    sizeHuge = 48.dp,
    sizeExtraLarge = 56.dp,
    sizeExtraHuge = 64.dp,
    spacingTiny = 2.dp,
    spacingSmall = 4.dp,
    spacingMedium = 8.dp,
    spacingLarge = 16.dp,
    spacingExtraLarge = 24.dp,
    spacingHuge = 32.dp,
    borderWidthTiny = 0.5.dp,
    borderWidthSmall = 1.dp,
    borderWidthMedium = 2.dp,
    borderWidthLarge = 4.dp,
    borderWidthHuge = 8.dp,
    borderWidthFocusInner = 2.dp,
    borderWidthFocusOuter = 4.dp,
    borderRadiusTiny = 2.dp,
    borderRadiusSmall = 4.dp,
    borderRadiusMedium = 8.dp,
    borderRadiusLarge = 16.dp,
    borderRadiusHuge = 28.dp,
    borderRadiusButton = 20.dp,
    borderRadiusInput = 4.dp,
)

val LocalDimensions = staticCompositionLocalOf { defaultDimensions }
