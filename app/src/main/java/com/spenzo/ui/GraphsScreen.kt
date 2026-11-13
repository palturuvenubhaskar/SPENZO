package com.spenzo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.components.BarChart
import com.spenzo.ui.components.BarPoint
import com.spenzo.ui.components.SegmentedToggle
import com.spenzo.ui.components.SpCard
import com.spenzo.ui.theme.*
import java.time.YearMonth
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun GraphsScreen(vm: TransactionViewModel) {
    val c = SpTheme.colors
    val symbol = LocalCurrencySymbol.current
    val ym by vm.currentMonth.collectAsStateWithLifecycle()
    val year by vm.currentYear.collectAsStateWithLifecycle()
    val daily by vm.dailyTotals.collectAsStateWithLifecycle(initialValue = emptyList())
    val monthlyExpense by vm.monthlyExpenseTotals.collectAsStateWithLifecycle(initialValue = emptyList())
    val monthlyIncome by vm.monthlyIncomeTotals.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories by vm.categoryTotals.collectAsStateWithLifecycle(initialValue = emptyList())

    var monthlyMode by remember { mutableStateOf(true) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .padding(horizontal = Space.xl),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
        contentPadding = PaddingValues(top = Space.xxl, bottom = 96.dp),
    ) {
        item {
            Text("Graphs", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)
        }

        item {
            SegmentedToggle(
                options = listOf("Monthly", "Yearly"),
                selectedIndex = if (monthlyMode) 0 else 1,
                onSelect = { monthlyMode = it == 0 },
            )
        }

        if (monthlyMode) {
            item {
                PeriodHeader(
                    title = Dates.prettyMonth(ym),
                    subtitle = "Daily spending",
                    onPrevious = { vm.shiftMonth(-1) },
                    onNext = { vm.shiftMonth(1) },
                )
            }

            item {
                DailySpendingCard(ym = ym, daily = daily, symbol = symbol)
            }

            item {
                CategoryDonutCard(categories = categories, symbol = symbol)
            }
        } else {
            item {
                PeriodHeader(
                    title = year,
                    subtitle = "Monthly totals",
                    onPrevious = { vm.currentYear.value = (year.toIntOrNull() ?: 0).minus(1).toString() },
                    onNext = { vm.currentYear.value = (year.toIntOrNull() ?: 0).plus(1).toString() },
                )
            }

            item {
                MonthlyTotalsCard(
                    year = year,
                    expenses = monthlyExpense,
                    income = monthlyIncome,
                    symbol = symbol,
                )
            }
        }
    }
}

@Composable
private fun PeriodHeader(
    title: String,
    subtitle: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val c = SpTheme.colors
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Previous", tint = c.textPrimary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
        }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Next", tint = c.textPrimary)
        }
    }
}

@Composable
private fun DailySpendingCard(ym: String, daily: List<DayTotal>, symbol: String) {
    val c = SpTheme.colors
    var selected by remember(ym) { mutableStateOf(-1) }

    val month = Dates.parseMonth(ym)
    val totalsByDay = remember(daily) { daily.associate { it.day to it.total } }
    val points = remember(ym, totalsByDay) {
        (1..month.lengthOfMonth()).map { day ->
            val key = month.atDay(day).toString()
            BarPoint(day.toString(), (totalsByDay[key] ?: 0.0).toFloat())
        }
    }
    val total = Money.sum(totalsByDay.values)

    SpCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Daily spending", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                Text("Total ${Money.format(total, symbol)}", style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
            }
            if (selected >= 0) {
                val day = month.atDay(selected + 1)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        Dates.prettyDay(Dates.toMillis(day)),
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textSecondary,
                    )
                    Text(
                        Money.format(points[selected].value.toDouble(), symbol),
                        style = MaterialTheme.typography.titleSmall,
                        color = c.textPrimary,
                    )
                }
            }
        }
        Spacer(Modifier.height(Space.lg))

        BarChart(
            points = points,
            barColor = if (c.isDark) c.income else c.primary,
            labelColor = c.textSecondary,
            height = 200.dp,
            showValueLabels = true,
            valueLabel = { value -> "$symbol${value.roundToInt()}" },
            selectedIndex = selected,
            onSelect = { index -> selected = if (selected == index) -1 else index },
        )
        Spacer(Modifier.height(Space.sm))
        Text(
            if (selected >= 0) "Tap again to clear" else "Tap a bar to see that day",
            style = MaterialTheme.typography.labelSmall,
            color = c.textSecondary,
        )
    }
}

@Composable
private fun MonthlyTotalsCard(
    year: String,
    expenses: List<MonthTotal>,
    income: List<MonthTotal>,
    symbol: String,
) {
    val c = SpTheme.colors
    var showIncome by remember { mutableStateOf(false) }
    var selected by remember(year) { mutableStateOf(-1) }

    val source = if (showIncome) income else expenses
    val byMonth = remember(source) { source.associate { it.month to it.total } }
    val yearInt = year.toIntOrNull() ?: 0
    val points = remember(year, byMonth) {
        (1..12).map { month ->
            val key = YearMonth.of(yearInt, month).toString()
            BarPoint(
                label = YearMonth.of(yearInt, month).month.getDisplayName(
                    java.time.format.TextStyle.SHORT, Locale.getDefault()
                ),
                value = (byMonth[key] ?: 0.0).toFloat(),
            )
        }
    }
    val total = Money.sum(byMonth.values)

    SpCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(
                    if (showIncome) "Monthly income" else "Monthly spending",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary,
                )
                Text("Total ${Money.format(total, symbol)}", style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
            }
            if (selected >= 0) {
                Text(
                    Money.format(points[selected].value.toDouble(), symbol),
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textPrimary,
                )
            }
        }
        Spacer(Modifier.height(Space.md))
        SegmentedToggle(
            options = listOf("Spending", "Income"),
            selectedIndex = if (showIncome) 1 else 0,
            onSelect = { showIncome = it == 1; selected = -1 },
        )
        Spacer(Modifier.height(Space.md))
        BarChart(
            points = points,
            barColor = if (showIncome) c.income else (if (c.isDark) c.income else c.primary),
            labelColor = c.textSecondary,
            height = 220.dp,
            showValueLabels = false,
            selectedIndex = selected,
            onSelect = { index -> selected = if (selected == index) -1 else index },
        )
    }
}

@Composable
private fun CategoryDonutCard(
    categories: List<CategoryTotal>,
    symbol: String,
    title: String = "By category",
) {
    val c = SpTheme.colors
    SpCard {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        Spacer(Modifier.height(Space.lg))

        if (categories.isEmpty()) {
            Text("No expenses in this period", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            return@SpCard
        }

        val sorted = remember(categories) { categories.sortedByDescending { it.total } }
        val total = Money.sum(sorted.map { it.total }).toFloat().coerceAtLeast(0.01f)

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    var start = -90f
                    val stroke = 34f
                    val gap = 2f
                    sorted.forEach { item ->
                        val sweep = (item.total.toFloat() / total) * 360f
                        val color = Category.fromName(item.category).color
                        drawArc(
                            color = color,
                            startAngle = start + gap / 2f,
                            sweepAngle = (sweep - gap).coerceAtLeast(0.5f),
                            useCenter = false,
                            topLeft = Offset(stroke / 2, stroke / 2),
                            size = Size(size.width - stroke, size.height - stroke),
                            style = Stroke(stroke),
                        )
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        Money.format(total.toDouble(), symbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary,
                    )
                    Text("total", style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                }
            }
            Spacer(Modifier.height(Space.lg))

            sorted.forEach { item ->
                val category = Category.fromName(item.category)
                val percent = item.total / total * 100
                Row(
                    Modifier.fillMaxWidth().padding(vertical = Space.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(category.color))
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        category.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        Money.format(item.total, symbol),
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textPrimary,
                    )
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        "${"%.0f".format(percent)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textSecondary,
                        modifier = Modifier.width(36.dp),
                    )
                }
            }
        }
    }
}
