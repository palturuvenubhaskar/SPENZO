package com.example.spenzo.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spenzo.domain.usecase.CategorySummary
import com.example.spenzo.presentation.theme.TextPrimary
import com.example.spenzo.presentation.theme.Typography
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.atan2

@Composable
fun DonutChart(
    data: List<CategorySummary>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        if (angle < 0) angle += 360f

                        // Map angle to segment starting from -90
                        var startAngle = -90f
                        for (i in data.indices) {
                            val sweepAngle = data[i].percentage * 360f
                            val endAngle = startAngle + sweepAngle
                            
                            var normalizedStart = startAngle % 360
                            if(normalizedStart < 0) normalizedStart += 360
                            
                            var normalizedEnd = endAngle % 360
                            if(normalizedEnd < 0) normalizedEnd += 360

                            if (normalizedStart < normalizedEnd) {
                                if (angle in normalizedStart..normalizedEnd) {
                                    selectedIndex = if (selectedIndex == i) null else i
                                    return@detectTapGestures
                                }
                            } else {
                                if (angle >= normalizedStart || angle <= normalizedEnd) {
                                    selectedIndex = if (selectedIndex == i) null else i
                                    return@detectTapGestures
                                }
                            }
                            startAngle += sweepAngle
                        }
                    }
                }
        ) {
            val strokeWidth = 32.dp.toPx()
            val spacingAngle = 2f
            var startAngle = -90f

            data.forEachIndexed { index, item ->
                val sweepAngle = item.percentage * 360f * progress.value
                val isSelected = index == selectedIndex
                
                val currentStrokeWidth = if (isSelected) strokeWidth + 8.dp.toPx() else strokeWidth

                if (sweepAngle > spacingAngle) {
                    drawArc(
                        color = getCategoryColor(item.category),
                        startAngle = startAngle + spacingAngle / 2,
                        sweepAngle = sweepAngle - spacingAngle,
                        useCenter = false,
                        style = Stroke(width = currentStrokeWidth, cap = StrokeCap.Round),
                        size = Size(size.width - currentStrokeWidth, size.height - currentStrokeWidth),
                        topLeft = Offset(currentStrokeWidth / 2, currentStrokeWidth / 2)
                    )
                }
                startAngle += item.percentage * 360f
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total",
                style = Typography.labelMedium,
                color = TextPrimary.copy(alpha = 0.5f)
            )
            Text(
                text = formatter.format(totalAmount),
                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }
    }
}
