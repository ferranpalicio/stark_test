package com.pal.starktest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.pal.starktest.R

/**
 * Epilogue ships as two variable fonts — one upright, one italic — each exposing a `wght` axis
 * covering 100..900. Rather than bundling a file per weight, every entry points at the same two
 * files and pins the axis via [FontVariation]. Variable-axis support needs API 26; `minSdk` is 30.
 */
private fun epilogue(weight: FontWeight, style: FontStyle = FontStyle.Normal) = Font(
    resId = if (style == FontStyle.Italic) R.font.epilogue_variable_italic else R.font.epilogue_variable,
    weight = weight,
    style = style,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val Epilogue = FontFamily(
    epilogue(FontWeight.Light),
    epilogue(FontWeight.Light, FontStyle.Italic),
    epilogue(FontWeight.Normal),
    epilogue(FontWeight.Normal, FontStyle.Italic),
    epilogue(FontWeight.Medium),
    epilogue(FontWeight.Medium, FontStyle.Italic),
    epilogue(FontWeight.SemiBold),
    epilogue(FontWeight.SemiBold, FontStyle.Italic),
    epilogue(FontWeight.Bold),
    epilogue(FontWeight.Bold, FontStyle.Italic),
)

private val default = Typography()

/**
 * The Material 3 type scale with its default sizes, weights and spacing, restyled in [Epilogue].
 * Every style is remapped rather than a chosen few, so no screen falls back to the system font.
 */
val Typography = Typography(
    displayLarge = default.displayLarge.copy(fontFamily = Epilogue),
    displayMedium = default.displayMedium.copy(fontFamily = Epilogue),
    displaySmall = default.displaySmall.copy(fontFamily = Epilogue),
    headlineLarge = default.headlineLarge.copy(fontFamily = Epilogue),
    headlineMedium = default.headlineMedium.copy(fontFamily = Epilogue),
    headlineSmall = default.headlineSmall.copy(fontFamily = Epilogue),
    titleLarge = default.titleLarge.copy(fontFamily = Epilogue),
    titleMedium = default.titleMedium.copy(fontFamily = Epilogue),
    titleSmall = default.titleSmall.copy(fontFamily = Epilogue),
    bodyLarge = default.bodyLarge.copy(fontFamily = Epilogue),
    bodyMedium = default.bodyMedium.copy(fontFamily = Epilogue),
    bodySmall = default.bodySmall.copy(fontFamily = Epilogue),
    labelLarge = default.labelLarge.copy(fontFamily = Epilogue),
    labelMedium = default.labelMedium.copy(fontFamily = Epilogue),
    labelSmall = default.labelSmall.copy(fontFamily = Epilogue),
)
