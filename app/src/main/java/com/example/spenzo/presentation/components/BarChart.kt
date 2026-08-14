package com.example.spenzo.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spenzo.presentation.theme.CardDark
import com.example.spenzo.presentation.theme.TextSecondary

data class BarData(
    val label: String,
    val value: Float,
    val displayValue: String
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun BarChart(
    data: List<BarData>,
    modifier: Modifier = Modifier,
    barColor: Color = CardDark
) {
    val textMeasurer = rememberTextMeasurer()
    val maxVal = data.maxOfOrNull { it.value } ?: 0f
    
    // Animation
    val progress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }
    
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val barWidth = size.width / (data.size * 2)
                    val spacing = barWidth
                    
                    var currentX = spacing / 2
                    for (i in data.indices) {
                        if (offset.x >= currentX && offset.x <= currentX + barWidth) {
                            selectedBarIndex = i
                            return@detectTapGestures
                        }
                        currentX += barWidth + spacing
                    }
                    selectedBarIndex = null
                }
            }
    ) {
        if (data.isEmpty() || maxVal == 0f) return@Canvas
        
        val width = size.width
        val height = size.height
        val textStyle = TextStyle(color = TextSecondary, fontSize = 10.sp)
        
        val barWidth = width / (data.size * 2)
        val spacing = barWidth
        
        var currentX = spacing / 2
        
        // draw baseline
        drawLine(
            color = Color(0x14000000),
            start = Offset(0f, height - 20.dp.toPx()),
            end = Offset(width, height - 20.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
        
        data.forEachIndexed { index, barData ->
            val barHeight = if (maxVal > 0) (barData.value / maxVal) * (height - 40.dp.toPx()) else 0f
            val animatedHeight = barHeight * progress.value
            
            val isSelected = index == selectedBarIndex
            val currentBarColor = if (barData.value == 0f) Color(0x0F000000) else barColor
            val actualHeight = if (barData.value == 0f) 10.dp.toPx() else animatedHeight
            
            drawRoundRect(
                color = currentBarColor,
                topLeft = Offset(
                    x = currentX,
                    y = height - 20.dp.toPx() - actualHeight
                ),
                size = Size(
                    width = if (isSelected) barWidth * 1.05f else barWidth,
                    height = actualHeight
                ),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            
            // Draw label
            val textLayoutResult = textMeasurer.measure(
                text = barData.label,
                style = textStyle
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = currentX + (barWidth - textLayoutResult.size.width) / 2,
                    y = height - 16.dp.toPx()
                )
            )
            
            // Draw tooltip if selected
            if (isSelected && barData.value > 0f) {
                val tooltipText = barData.displayValue
                val tooltipLayout = textMeasurer.measure(
                    text = tooltipText,
                    style = TextStyle(color = Color.White, fontSize = 12.sp)
                )
                val tooltipWidth = tooltipLayout.size.width + 16.dp.toPx()
                val tooltipHeight = tooltipLayout.size.height + 8.dp.toPx()
                
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.8f),
                    topLeft = Offset(
                        x = currentX + barWidth / 2 - tooltipWidth / 2,
                        y = height - 20.dp.toPx() - actualHeight - tooltipHeight - 8.dp.toPx()
                    ),
                    size = Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                
                drawText(
                    textLayoutResult = tooltipLayout,
                    topLeft = Offset(
                        x = currentX + barWidth / 2 - tooltipLayout.size.width / 2,
                        y = height - 20.dp.toPx() - actualHeight - tooltipHeight - 8.dp.toPx() + 4.dp.toPx()
                    )
                )
            }
            
            currentX += barWidth + spacing
        }
    }
}
