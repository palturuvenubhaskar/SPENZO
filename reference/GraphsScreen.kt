package com.spenzo.ui

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

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp)) {

        item { PeriodHeader(ym, { vm.currentMonth.value = shiftMonth(ym, -1) },
            { vm.currentMonth.value = shiftMonth(ym, +1) }) }

        item {
            CardSection("Spending per day") {
                BarChart(data = (1..daysInMonth(ym)).map { d ->
                    val key = "%s-%02d".format(ym, d)
                    d.toFloat() to daily.firstOrNull { it.day == key }?.total?.toFloat()
                }, maxValue = (daily.maxOfOrNull { it.total }?.toFloat() ?: 0f).coerceAtLeast(1f),
                    barColor = DarkCard, labelEvery = 5)
            }
        }

        item { PeriodHeader(year, { vm.currentYear.value = (year.toInt() - 1).toString() },
            { vm.currentYear.value = (year.toInt() + 1).toString() }) }

        item {
            CardSection("Spending per month") {
                BarChart(data = (1..12).map { m ->
                    val key = "%s-%02d".format(year, m)
                    m.toFloat() to monthly.firstOrNull { it.month == key }?.total?.toFloat()
                }, maxValue = (monthly.maxOfOrNull { it.total }?.toFloat() ?: 0f).coerceAtLeast(1f),
                    barColor = AccentOrange, labelEvery = 1)
            }
        }

        item { CardSection("By category") { DonutChart(cats) } }
    }
}

@Composable
private fun PeriodHeader(label: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row {
            IconButton(onClick = onPrev) { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, "Previous") }
            IconButton(onClick = onNext) { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, "Next") }
        }
    }
}

@Composable
private fun CardSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Surface).padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

// Canvas bar chart — no external chart library needed
@Composable
private fun BarChart(data: List<Pair<Float, Float?>>, maxValue: Float,
                     barColor: Color, labelEvery: Int) {
    Column {
        Canvas(Modifier.fillMaxWidth().height(150.dp)) {
            val barW = size.width / data.size
            val chartH = size.height - 20f
            data.forEachIndexed { i, (_, v) ->
                val h = ((v ?: 0f) / maxValue) * chartH
                drawRoundRect(
                    color = barColor.copy(alpha = if (v == null) 0.15f else 1f),
                    topLeft = Offset(i * barW + barW * 0.25f, chartH - h),
                    size = Size(barW * 0.5f, h),
                    cornerRadius = CornerRadius(6f, 6f))
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            data.forEachIndexed { i, (label, _) ->
                if (i % labelEvery == 0)
                    Text(label.toInt().toString(), fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}

// Donut chart for categories
@Composable
private fun DonutChart(cats: List<CategoryTotal>) {
    if (cats.isEmpty()) {
        Text("No expenses this period", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        return
    }
    val total = cats.sumOf { it.total }.toFloat()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                var start = -90f
                val stroke = 34f
                cats.forEach { ct ->
                    val sweep = (ct.total.toFloat() / total) * 360f
                    val col = Category.entries.firstOrNull { it.name == ct.category }?.color ?: TextSecondary
                    drawArc(col, start, sweep, useCenter = false,
                        topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke))
                    start += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$" + "%.0f".format(total), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text("total", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
        Spacer(Modifier.height(14.dp))
        cats.sortedByDescending { it.total }.forEach { ct ->
            val cat = Category.entries.firstOrNull { it.name == ct.category } ?: Category.OTHER
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(cat.color))
                Spacer(Modifier.width(8.dp))
                Text(cat.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text("$" + "%.2f".format(ct.total) + "  (" +
                    "%.0f".format(ct.total.toFloat() / total * 100) + "%)",
                    style = MaterialTheme.typography.labelSmall, color = TextSecondary)
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
