package com.spenzo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

data class BarPoint(val label: String, val value: Float)

/**
 * Horizontally scrollable bar chart where every label is always visible.
 * Uses LazyRow for smooth scrolling and proper label rendering.
 * At least 10 bars fit on screen at once.
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

    val haptics = LocalHapticFeedback.current
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)
    val valueStyle = TextStyle(fontSize = 9.sp, color = labelColor, fontWeight = FontWeight.SemiBold)
    val maxValue = points.maxOf { it.value }.coerceAtLeast(1f)

    // Ensure at least 10 bars fit on screen: min bar width = screen width / 10 - spacing
    // With 4dp spacing and 8dp padding each side, available = width - 16 - (9 * 4) = width - 52
    // So min bar width ≈ (width - 52) / 10. Use fixed min of 28dp to guarantee 10 fit on typical screens.
    val barWidthDp = remember(points.size) {
        // Minimum 28dp per bar ensures 10 bars fit even on small screens (360dp width)
        // With 4dp spacing: 10 * 28 + 9 * 4 = 316dp, plus 16dp padding = 332dp < 360dp
        val minWidth = 28.dp
        val calculatedWidth = 320.dp / points.size.coerceAtLeast(1)
        if (calculatedWidth.value > minWidth.value) calculatedWidth else minWidth
    }

    Box(modifier
        .fillMaxWidth()
        .height(height)
    ) {
        // Scrollable bar chart - no axis line
        LazyRow(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            items(points, key = { it.label }) { point ->
                val index = points.indexOf(point)
                val alpha = if (selectedIndex < 0 || selectedIndex == index) 1f else 0.45f
                val barHeightRatio = if (point.value > 0f) (point.value / maxValue) else 0f

                Column(
                    Modifier
                        .width(barWidthDp)
                        .fillMaxHeight()
                        .pointerInput(point, onSelect) {
                            if (onSelect == null) return@pointerInput
                            detectTapGestures { _ ->
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelect(index)
                            }
                        }
                        .wrapContentHeight(Alignment.Bottom),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    // Value label on top of bar - show for all days when enabled
                    if (showValueLabels && point.value > 0f) {
                        Text(
                            text = valueLabel(point.value),
                            style = valueStyle,
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(2.dp))
                    }

                    // Bar
                    Box(
                        Modifier
                            .width((barWidthDp.value * 0.7f).dp.coerceIn(4.dp, 26.dp))
                            .height((height.value * barHeightRatio).dp)
                            .background(barColor.copy(alpha = alpha), RoundedCornerShape(999.dp))
                    )

                    // Label below bar - always visible
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Text(
                        text = point.label,
                        style = labelStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}