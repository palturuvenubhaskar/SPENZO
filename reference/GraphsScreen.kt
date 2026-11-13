package com.spenzo.ui

import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GraphsScreen(vm: TransactionViewModel = viewModel()) {
    val ym by vm.currentMonth.collectAsStateWithLifecycle()
    val year by vm.currentYear.collectAsStateWithLifecycle()
    val daily by vm.dailyTotals.collectAsStateWithLifecycle(initial = emptyList())
    val monthly by vm.monthlyExpenseTotals.collectAsStateWithLifecycle(initial = emptyList())
    val cats by vm.categoryTotals.collectAsStateWithLifecycle(initial = emptyList())

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp)
    ) {

        item { PeriodHeader(ym, { vm.currentMonth.value = shiftMonth(ym, -1) }, { vm.currentMonth.value = shiftMonth(ym, +1) }) }

        item {
            CardSection("Spending per day") {
                if (daily.isEmpty()) {
                    EmptyChartState("No expenses this month")
                } else {
                    BarChart(
                        data = (1..daysInMonth(ym)).map { d ->
                            val key = "%s-%02d".format(ym, d)
                            d.toFloat() to daily.firstOrNull { it.day == key }?.total?.toFloat()
                        },
                        maxValue = (daily.maxOfOrNull { it.total }?.toFloat() ?: 0f).coerceAtLeast(1f),
                        barColor = MaterialTheme.colorScheme.primary,
                        labelEvery = 5
                    )
                }
            }
        }

        item { PeriodHeader(year, { vm.currentYear.value = (year.toInt() - 1).toString() }, { vm.currentYear.value = (year.toInt() + 1).toString() }) }

        item {
            CardSection("Spending per month") {
                if (monthly.isEmpty()) {
                    EmptyChartState("No expenses this year")
                } else {
                    BarChart(
                        data = (1..12).map { m ->
                            val key = "%s-%02d".format(year, m)
                            m.toFloat() to monthly.firstOrNull { it.month == key }?.total?.toFloat()
                        },
                        maxValue = (monthly.maxOfOrNull { it.total }?.toFloat() ?: 0f).coerceAtLeast(1f),
                        barColor = MaterialTheme.colorScheme.tertiary,
                        labelEvery = 1
                    )
                }
            }
        }

        item { CardSection("By category") { DonutChart(cats) } }
    }
}

@Composable
private fun PeriodHeader(label: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row {
            IconButton(onClick = onPrev, contentDescription = "Previous period") {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null)
            }
            IconButton(onClick = onNext, contentDescription = "Next period") {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun CardSection(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun EmptyChartState(message: String) {
    Column(
        Modifier.fillMaxWidth().height(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.BarChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4),
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

// Canvas bar chart — no external chart library needed
@Composable
private fun BarChart(
    data: List<Pair<Float, Float?>>,
    maxValue: Float,
    barColor: Color,
    labelEvery: Int
) {
    val animatedData = data.map { (label, value) ->
        val animatedValue = animateFloatAsState(
            targetValue = (value ?: 0f) / maxValue,
            animationSpec = Spring(stiffness = 200f, dampingRatio = 1f)
        )
        label to animatedValue
    }

    Column {
        Canvas(Modifier.fillMaxWidth().height(150.dp)) {
            val barW = size.width / data.size.max(1)
            val chartH = size.height - 20f
            animatedData.forEachIndexed { i, (_, animatedRatio) ->
                val h = animatedRatio.value * chartH
                drawRoundRect(
                    color = barColor.copy(alpha = if (data[i].second == null) 0.15f else 1f),
                    topLeft = Offset(i * barW + barW * 0.25f, chartH - h),
                    size = Size(barW * 0.5f.coerceAtLeast(4f), h.coerceAtLeast(0f)),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            data.forEachIndexed { i, (label, _) ->
                if (i % labelEvery == 0)
                    Text(label.toInt().toString(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Donut chart for categories
@Composable
private fun DonutChart(cats: List<CategoryTotal>) {
    if (cats.isEmpty()) {
        EmptyChartState("No expenses this period")
        return
    }
    val total = cats.sumOf { it.total }.toFloat()
    val animatedSweeps = cats.map { ct ->
        animateFloatAsState(
            targetValue = (ct.total.toFloat() / total) * 360f,
            animationSpec = Spring(stiffness = 200f, dampingRatio = 1f)
        )
    }
    val animatedStart = animateFloatAsState(targetValue = -90f, animationSpec = Spring(stiffness = 200f, dampingRatio = 1f))

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                var start = animatedStart.value
                val stroke = 34f
                cats.forEachIndexed { i, ct ->
                    val sweep = animatedSweeps[i].value
                    val col = Category.entries.firstOrNull { it.name == ct.category }?.color ?: MaterialTheme.colorScheme.onSurfaceVariant
                    drawArc(
                        col,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke)
                    )
                    start += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(formatCurrency(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(14.dp))
        cats.sortedByDescending { it.total }.forEach { ct ->
            val cat = Category.entries.firstOrNull { it.name == ct.category } ?: Category.OTHER
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(cat.color))
                Spacer(Modifier.width(8.dp))
                Text(cat.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(
                    "${formatCurrency(ct.total)}  (%.0f%%".format(ct.total.toFloat() / total * 100) + ")",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Date helpers
private fun daysInMonth(ym: String): Int =
    Calendar.getInstance().apply {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(ym)?.let { time = it }
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

private fun shiftMonth(ym: String, delta: Int): String {
    val fmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val cal = Calendar.getInstance().apply { time = fmt.parse(ym)!! }
    cal.add(Calendar.MONTH, delta)
    return fmt.format(cal.time)
}