package com.pal.starktest.features.bikelive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pal.starktest.ui.theme.StarkTheme

/** The gauge is a discrete readout: the charge is rounded up to one of six bars. */
private const val BAR_COUNT = 6

/** Fraction of the body width taken by the terminal nub sticking out on the right. */
private const val TERMINAL_WIDTH_RATIO = 0.06f

/** Fraction of the body height spanned by the terminal nub. */
private const val TERMINAL_HEIGHT_RATIO = 0.4f

/**
 * Battery charge drawn as a boxed battery with up to [BAR_COUNT] bars inside, one bar per sixth of
 * charge, rounded up — so any charge above zero shows at least one bar, and only 100% fills all six.
 *
 * Everything is drawn on a [Canvas] and sized off the layout the caller gives it, so the shape
 * scales with [size] rather than depending on hardcoded pixels.
 *
 * @param percentage state of charge, coerced into 0..100.
 * @param color bar colour; defaults to a red/amber/green traffic light off the remaining charge.
 */
@Composable
fun BatteryGauge(
    percentage: Int,
    modifier: Modifier = Modifier,
    width: Dp = StarkTheme.dimens.sizeExtraHuge,
    height: Dp = StarkTheme.dimens.sizeMedium,
    color: Color = batteryColor(percentage),
    outlineColor: Color = color,
) {
    val charge = percentage.coerceIn(0, 100)
    // Round up: 1% must still light a bar, and only a full battery lights all six.
    val filledBars = ((charge * BAR_COUNT) + 99) / 100

    Canvas(
        modifier = modifier
            .size(width, height)
            .semantics { contentDescription = "Battery $charge percent" },
    ) {
        val stroke = size.height * 0.08f
        val terminalWidth = size.width * TERMINAL_WIDTH_RATIO
        val bodyWidth = size.width - terminalWidth
        val corner = CornerRadius(size.height * 0.18f)

        // Terminal nub, vertically centred against the right edge of the body.
        val terminalHeight = size.height * TERMINAL_HEIGHT_RATIO
        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(bodyWidth - 1, (size.height - terminalHeight) / 2),
            size = Size(terminalWidth, terminalHeight),
        )

        // Body: stroked from half the stroke width in, so the outline stays inside the bounds.
        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(bodyWidth - stroke, size.height - stroke),
            cornerRadius = corner,
            style = Stroke(width = stroke),
        )

        drawBars(filledBars, color, bodyWidth, stroke)
    }
}

/**
 * Lays the six bar slots out across the inner width and fills the first [filledBars] of them. The
 * slots are always the same size, so the bars stay put as the charge drops.
 */
private fun DrawScope.drawBars(
    filledBars: Int,
    color: Color,
    bodyWidth: Float,
    stroke: Float,
) {
    if (filledBars <= 0) return

    val inset = stroke * 2
    val innerWidth = bodyWidth - inset * 2
    val innerHeight = size.height - inset * 2
    val gap = innerWidth * 0.04f
    val barWidth = (innerWidth - gap * (BAR_COUNT - 1)) / BAR_COUNT

    repeat(filledBars) { index ->
        drawRoundRect(
            color = color,
            topLeft = Offset(inset + index * (barWidth + gap), inset),
            size = Size(barWidth, innerHeight),
            cornerRadius = CornerRadius(barWidth * 0.2f),
        )
    }
}

/** Traffic light on remaining charge: the gauge is glanceable without reading the number. */
private fun batteryColor(percentage: Int): Color = when {
    percentage <= 15 -> Color(0xFFF44336)
    percentage <= 35 -> Color(0xFFFFC107)
    else -> Color(0xFF4CAF50)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun BatteryGaugePreview() {
    StarkTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(100, 83, 68, 50, 34, 10, 0).forEach { BatteryGauge(percentage = it) }
        }
    }
}
