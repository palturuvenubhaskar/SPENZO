package com.spenzo.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.components.BarChart
import com.spenzo.ui.components.BarPoint
import com.spenzo.ui.components.MonthStepper
import com.spenzo.ui.components.SegmentedToggle
import com.spenzo.ui.components.SpCard
import com.spenzo.ui.theme.*
import java.time.LocalDate
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
            .statusBarsPadding()
            .padding(horizontal = Insets.gutter),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
        contentPadding = PaddingValues(top = Space.xxl, bottom = Insets.bottomContent),
    ) {
        item {
            Column {
                Text("Graphs", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(
                    if (monthlyMode) "Daily rhythm of your spending" else "Your year at a glance",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                )
            }
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
                MonthStepper(
                    label = Dates.prettyMonth(ym),
                    subtitle = "Spending",
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
                MonthStepper(
                    label = year,
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
    val busiest = points.maxByOrNull { it.value }
    val today = Dates.today()
    val isCurrentMonth = month == YearMonth.from(today)
    val todayIndex = if (isCurrentMonth) today.dayOfMonth - 1 else -1

    // Open the chart centered on today (if current month) or busiest day,
    // so real data is on screen instead of an empty stretch of the month.
    val initialIndex = remember(points, todayIndex) {
        if (todayIndex >= 0) {
            (todayIndex - 4).coerceIn(0, (points.size - 1).coerceAtLeast(0))
        } else {
            val peak = points.indices.maxByOrNull { points[it].value } ?: 0
            if (points[peak].value > 0f) (peak - 3).coerceIn(0, (points.size - 1).coerceAtLeast(0)) else 0
        }
    }
    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    SpCard {
        Column {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("Daily spending", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        Money.format(total, symbol) + " total",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
                if (selected >= 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            Dates.prettyDay(Dates.toMillis(month.atDay(selected + 1))),
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textSecondary,
                        )
                        Text(
                            Money.format(points[selected].value.toDouble(), symbol),
                            style = MaterialTheme.typography.titleSmall,
                            color = c.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else if (busiest != null && busiest.value > 0f) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Peak day", style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                        Text(
                            Money.format(busiest.value.toDouble(), symbol),
                            style = MaterialTheme.typography.titleSmall,
                            color = c.textPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(Space.lg))

            // Horizontal scrollable chart — ~10 days visible at once, every day
            // of the month reachable, value labelled above each bar.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                val maxBarHeight = 132.dp
                val minBarHeight = 6.dp
                val maxVal = points.maxOf { it.value }.coerceAtLeast(1f)

                LazyRow(
                    state = scrollState,
                    contentPadding = PaddingValues(horizontal = Space.lg),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    items(points.size) { index ->
                        val point = points[index]
                        val isSelected = selected == index
                        val hasValue = point.value > 0f
                        val barHeight = if (hasValue) {
                            (maxBarHeight * (point.value / maxVal)).coerceAtLeast(minBarHeight)
                        } else minBarHeight

                        val barColor = when {
                            isSelected -> c.primary
                            hasValue -> c.chartBar
                            else -> c.chartBar.copy(alpha = 0.18f)
                        }

                        Column(
                            modifier = Modifier
                                .width(26.dp)
                                .fillMaxHeight()
                                .clickable { selected = if (isSelected) -1 else index }
                                .background(if (isSelected) c.primarySoft else Color.Transparent)
                                .clip(RoundedCornerShape(Radius.sm)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                        ) {
                            // Value above the bar — hide for zero days, use full format (e.g., ₹1,260)
                            if (hasValue) {
                                Text(
                                    Money.format(point.value.toDouble(), symbol),
                                    fontSize = 8.sp,
                                    lineHeight = 10.sp,
                                    color = if (isSelected) c.primary else c.textTertiary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            // Capsule bar (zero days get a flat pill on the baseline)
                            Box(
                                Modifier
                                    .width(16.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(barColor)
                            )
                            Spacer(Modifier.height(8.dp))
                            // Day label
                            Text(
                                point.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) c.primary else c.textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }

                // Edge fades signal scrollability; tint must match the card, not the page.
                val fadeLeft = Brush.horizontalGradient(listOf(c.card, c.card.copy(alpha = 0f)))
                val fadeRight = Brush.horizontalGradient(listOf(c.card.copy(alpha = 0f), c.card))
                if (scrollState.firstVisibleItemIndex > 0) {
                    Box(
                        Modifier
                            .align(Alignment.CenterStart)
                            .width(28.dp)
                            .fillMaxHeight()
                            .background(fadeLeft)
                    )
                }
                if (scrollState.firstVisibleItemIndex + 8 < points.size) {
                    Box(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .width(28.dp)
                            .fillMaxHeight()
                            .background(fadeRight)
                    )
                }
            }

            Spacer(Modifier.height(Space.sm))
            Text(
                if (selected >= 0) "Tap the bar again to clear" else "Scroll for all days · Tap to inspect",
                style = MaterialTheme.typography.labelSmall,
                color = c.textTertiary,
            )
        }
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
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (showIncome) "Monthly income" else "Monthly spending",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary,
                )
                Text(
                    Money.format(total, symbol) + " total",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                )
            }
            if (selected >= 0) {
                Text(
                    Money.format(points[selected].value.toDouble(), symbol),
                    style = MaterialTheme.typography.titleSmall,
                    color = c.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(Space.md))
        SegmentedToggle(
            options = listOf("Spending", "Income"),
            selectedIndex = if (showIncome) 1 else 0,
            onSelect = { showIncome = it == 1; selected = -1 },
        )
        Spacer(Modifier.height(Space.lg))
        BarChart(
            points = points,
            barColor = c.chartBar,
            accentColor = if (showIncome) c.income else c.primary,
            labelColor = c.textSecondary,
            gridColor = c.border,
            height = 210.dp,
            showValueLabels = true,
            valueLabel = { value -> Money.compact(value.toDouble(), symbol) },
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

        if (categories.isEmpty()) {
            Spacer(Modifier.height(Space.md))
            Text(
                "No expenses in this period",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary,
            )
            return@SpCard
        }

        val sorted = remember(categories) { categories.sortedByDescending { it.total } }
        val total = Money.sum(sorted.map { it.total }).toFloat().coerceAtLeast(0.01f)

        Spacer(Modifier.height(Space.lg))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(148.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 16.dp.toPx()
                    val inset = stroke / 2
                    drawArc(
                        color = c.track,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                    var start = -90f
                    sorted.forEach { item ->
                        val sweep = (item.total.toFloat() / total) * 360f
                        drawArc(
                            color = Category.fromName(item.category).color,
                            startAngle = start + 1.5f,
                            sweepAngle = (sweep - 3f).coerceAtLeast(1f),
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - stroke, size.height - stroke),
                            style = Stroke(stroke, cap = StrokeCap.Round),
                        )
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        Money.compact(total.toDouble(), symbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary,
                    )
                    Text("total", style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                }
            }
        }
        Spacer(Modifier.height(Space.lg))
        sorted.forEachIndexed { index, item ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(c.border.copy(alpha = 0.6f))
                )
            }
            val category = Category.fromName(item.category)
            Row(
                Modifier.fillMaxWidth().padding(vertical = Space.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(category.color))
                Spacer(Modifier.width(Space.sm))
                Text(
                    category.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    Money.format(item.total, symbol),
                    style = MaterialTheme.typography.labelLarge,
                    color = c.textPrimary,
                )
                Spacer(Modifier.width(Space.sm))
                Text(
                    "${"%.0f".format(item.total / total * 100)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textTertiary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(36.dp),
                )
            }
        }
    }
}
