package com.spenzo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import kotlin.math.ceil
import kotlin.math.max

data class BarPoint(val label: String, val value: Float)

/**
 * Rounded bar chart drawn with Compose text, so labels scale with the device,
 * font settings and never collide.
 *
 * The previous charts used `android.graphics.Paint` with raw pixel sizes and
 * `Color.hashCode()` as the paint color; labels were ~7dp tall on real phones
 * and 31 daily bars printed every value on top of each other.
 */
@Composable
fun BarChart(
    points: List<BarPoint>,
    barColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    showValueLabels: Boolean = false,
    valueLabel: (Float) -> String = { it.toInt().toString() },
    selectedIndex: Int = -1,
    onSelect: ((Int) -> Unit)? = null,
) {
    if (points.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val haptics = LocalHapticFeedback.current
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)
    val valueStyle = TextStyle(fontSize = 9.sp, color = labelColor, fontWeight = FontWeight.SemiBold)
    val maxValue = points.maxOf { it.value }.coerceAtLeast(1f)
    // Which bar can show its value when there is no room to label them all.
    val tallest = points.indexOfFirst { it.value == maxValue }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(points, onSelect) {
                if (onSelect == null) return@pointerInput
                detectTapGestures { offset ->
                    val slot = size.width.toFloat() / points.size
                    val index = (offset.x / slot).toInt().coerceIn(0, points.lastIndex)
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelect(index)
                }
            }
    ) {
        val slot = size.width / points.size
        val barWidth = (slot * 0.62f).coerceIn(4.dp.toPx(), 26.dp.toPx())
        val topPad = if (showValueLabels) 20.dp.toPx() else 6.dp.toPx()
        val bottomPad = 20.dp.toPx()
        val chartHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
        val sampleLabelWidth = textMeasurer
            .measure(AnnotatedString(points.first().label), labelStyle).size.width
        val labelStep = max(1, ceil((sampleLabelWidth + 8.dp.toPx()) / slot).toInt())

        drawLine(
            color = labelColor.copy(alpha = 0.16f),
            start = Offset(0f, topPad + chartHeight),
            end = Offset(size.width, topPad + chartHeight),
            strokeWidth = 1.dp.toPx(),
        )

        var lastLabelRight = Float.NEGATIVE_INFINITY

        points.forEachIndexed { index, point ->
            val centerX = index * slot + slot / 2f
            val alpha = if (selectedIndex < 0 || selectedIndex == index) 1f else 0.45f
            val barHeight = if (point.value > 0f) (point.value / maxValue) * chartHeight else 0f

            if (barHeight > 0f) {
                drawRoundRect(
                    color = barColor.copy(alpha = alpha),
                    topLeft = Offset(centerX - barWidth / 2f, topPad + chartHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                )
            }
            // Empty days used to draw a 3dp tick each, which turned the axis
            // into a dotted line that looked like data.

            // Always label the tallest bar: without an axis the chart would
            // otherwise have no scale at all.
            val showValue = showValueLabels && barHeight > 0f &&
                (points.size <= 15 || selectedIndex == index || index == tallest)
            if (showValue) {
                val layout = textMeasurer.measure(AnnotatedString(valueLabel(point.value)), valueStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        centerX - layout.size.width / 2f,
                        topPad + chartHeight - barHeight - layout.size.height - 2.dp.toPx(),
                    ),
                )
            }

            if (index % labelStep == 0 || index == points.lastIndex) {
                val layout = textMeasurer.measure(AnnotatedString(point.label), labelStyle)
                // Keep the axis readable: clamp to the canvas and skip a label
                // that would print on top of the previous one (31 bars used to
                // end with "2930" as a single number).
                val left = (centerX - layout.size.width / 2f)
                    .coerceIn(0f, (size.width - layout.size.width).coerceAtLeast(0f))
                if (left > lastLabelRight + 4.dp.toPx()) {
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(left, size.height - layout.size.height),
                    )
                    lastLabelRight = left + layout.size.width
                }
            }
        }
    }
}
