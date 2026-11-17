package com.spenzo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BarPoint(val label: String, val value: Float)

/**
 * Full-width bar chart drawn on a Canvas.
 *
 * Every bar fits on screen — a whole month is visible at a glance instead of
 * scrolling through ten days at a time. Bars share one baseline and one scale,
 * gridlines carry the scale, and only the peak (or the selected bar) gets a
 * value label so 31 daily bars stay readable.
 */
@Composable
fun BarChart(
    points: List<BarPoint>,
    barColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    showValueLabels: Boolean = false,
    /** Draws the max value on the top gridline — the only y-axis label the chart needs. */
    showScale: Boolean = true,
    valueLabel: (Float) -> String = { it.toInt().toString() },
    selectedIndex: Int = -1,
    onSelect: ((Int) -> Unit)? = null,
    accentColor: Color = barColor,
    gridColor: Color = labelColor.copy(alpha = 0.10f),
) {
    if (points.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val haptics = LocalHapticFeedback.current
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)
    val valueStyle = TextStyle(fontSize = 10.sp, color = labelColor, fontWeight = FontWeight.SemiBold)
    val maxValue = points.maxOf { it.value }.coerceAtLeast(1f)
    val peakIndex = remember(points) { points.indices.maxByOrNull { points[it].value } ?: 0 }
    val labelStep = when {
        points.size <= 14 -> 1
        points.size <= 20 -> 2
        else -> 5
    }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(points, onSelect) {
                if (onSelect == null) return@pointerInput
                detectTapGestures { offset ->
                    val labelRow = 20.dp.toPx()
                    val plotHeight = size.height - labelRow
                    if (offset.y <= plotHeight) {
                        val slot = size.width / points.size
                        val index = (offset.x / slot).toInt().coerceIn(0, points.lastIndex)
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(index)
                    }
                }
            },
    ) {
        val labelRowHeight = 20.dp.toPx()
        // Gutter above the plot holds the scale label and stops the tallest bar
        // from touching the top gridline.
        val topGutter = if (showScale) 18.dp.toPx() else 0f
        val plotHeight = (size.height - labelRowHeight - topGutter).coerceAtLeast(1f)
        val baseline = topGutter + plotHeight
        val slot = size.width / points.size
        val barWidth = (slot * 0.62f).coerceIn(4f, 26f)
        val corner = (barWidth / 2f).coerceAtMost(4.dp.toPx())
        val maxBarHeight = plotHeight * 0.94f

        // Horizontal scale lines
        for (i in 0..4) {
            val y = topGutter + plotHeight * i / 4f
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
        }

        // The top gridline is the max, so label it once instead of repeating a
        // label on every line. Without it bar heights were unreadable.
        if (showScale) {
            val layout = textMeasurer.measure(AnnotatedString(valueLabel(maxValue)), labelStyle)
            drawText(
                layout,
                topLeft = Offset(0f, (topGutter - layout.size.height - 2.dp.toPx()).coerceAtLeast(0f)),
            )
        }

        points.forEachIndexed { index, point ->
            val centerX = slot * index + slot / 2f

            // Days with no spend get no bar. The old 1.5dp stub drew a dashed
            // line along the baseline that read as data instead of absence.
            if (point.value > 0f) {
                val ratio = (point.value / maxValue).coerceIn(0f, 1f)
                val barHeight = (maxBarHeight * ratio).coerceAtLeast(3.dp.toPx())
                val left = centerX - barWidth / 2f
                val right = centerX + barWidth / 2f
                val top = baseline - barHeight

                val color = when {
                    selectedIndex == index -> accentColor
                    selectedIndex >= 0 -> barColor.copy(alpha = 0.35f)
                    else -> barColor
                }

                val path = Path().apply {
                    moveTo(left, baseline)
                    lineTo(left, top + corner)
                    quadraticBezierTo(left, top, left + corner, top)
                    lineTo(right - corner, top)
                    quadraticBezierTo(right, top, right, top + corner)
                    lineTo(right, baseline)
                    close()
                }
                drawPath(path, color)
            }

            if (index % labelStep == 0 || index == points.lastIndex) {
                val layout = textMeasurer.measure(
                    AnnotatedString(point.label),
                    if (selectedIndex == index) labelStyle.copy(fontWeight = FontWeight.Bold) else labelStyle,
                )
                val x = (centerX - layout.size.width / 2f)
                    .coerceIn(0f, (size.width - layout.size.width).coerceAtLeast(0f))
                drawText(layout, topLeft = Offset(x, baseline + 4.dp.toPx()))
            }
        }

        if (showValueLabels) {
            val index = if (selectedIndex >= 0) selectedIndex else peakIndex
            val value = points[index].value
            if (value > 0f) {
                val layout = textMeasurer.measure(AnnotatedString(valueLabel(value)), valueStyle)
                val ratio = (value / maxValue).coerceIn(0f, 1f)
                val barTop = baseline - (maxBarHeight * ratio)
                val centerX = slot * index + slot / 2f
                drawText(
                    layout,
                    topLeft = Offset(
                        (centerX - layout.size.width / 2f)
                            .coerceIn(0f, (size.width - layout.size.width).coerceAtLeast(0f)),
                        (barTop - layout.size.height - 2.dp.toPx()).coerceAtLeast(0f),
                    ),
                )
            }
        }
    }
}
