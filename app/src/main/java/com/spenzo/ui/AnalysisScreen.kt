package com.spenzo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.components.IconBadge
import com.spenzo.ui.components.MonthStepper
import com.spenzo.ui.components.SpCard
import com.spenzo.ui.components.SpPill
import com.spenzo.ui.components.SpProgress
import com.spenzo.ui.components.spFieldColors
import com.spenzo.ui.theme.*

@Composable
fun AnalysisScreen(vm: TransactionViewModel) {
    val c = SpTheme.colors
    val symbol = LocalCurrencySymbol.current
    val ie by vm.monthIncomeExpense.collectAsStateWithLifecycle(initialValue = IncomeExpense(0.0, 0.0))
    val categoryTotals by vm.categoryTotals.collectAsStateWithLifecycle(initialValue = emptyList())
    val ym by vm.currentMonth.collectAsStateWithLifecycle()
    val budgets by BudgetStore.budgets.collectAsState()

    var editingBudgetFor by remember { mutableStateOf<Category?>(null) }
    var showAllCategories by remember(ym) { mutableStateOf(false) }

    val monthBudgets = budgets[ym].orEmpty()
    val spentByCategory = remember(categoryTotals) { categoryTotals.associate { it.category to it.total } }
    val savingsRate = if (ie.income > 0) ((ie.income - ie.expense) / ie.income * 100) else 0.0
    val days = Dates.daysElapsed(ym)
    val avgDaily = if (days > 0) ie.expense / days else 0.0
    val saved = ie.income - ie.expense

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .padding(horizontal = Insets.gutter),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
        contentPadding = PaddingValues(top = Space.xxl, bottom = Insets.bottomContent),
    ) {
        item {
            Column {
                Text("Analysis", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Averaged over $days day${if (days == 1) "" else "s"} this month",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                )
            }
        }

        item {
            MonthStepper(
                label = Dates.prettyMonth(ym),
                onPrevious = { vm.shiftMonth(-1) },
                onNext = { vm.shiftMonth(1) },
            )
        }

        item {
            val incomeValue = if (ie.income < 10_000) Money.format(ie.income, symbol) else Money.compact(ie.income, symbol)
            val expenseValue = if (ie.expense < 10_000) Money.format(ie.expense, symbol) else Money.compact(ie.expense, symbol)
            val avgDailyValue = if (avgDaily < 10_000) Money.format(avgDaily, symbol) else Money.compact(avgDaily, symbol)
            SpCard {
                Row(Modifier.fillMaxWidth()) {
                    MetricCell("Income", incomeValue, c.income, Modifier.weight(1f))
                    MetricCell("Expenses", expenseValue, c.expense, Modifier.weight(1f))
                }
                Spacer(Modifier.height(Space.lg))
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                Spacer(Modifier.height(Space.lg))
                Row(Modifier.fillMaxWidth()) {
                    MetricCell(
                        "Savings rate",
                        "${"%.0f".format(savingsRate)}%",
                        if (savingsRate >= 0) c.income else c.expense,
                        Modifier.weight(1f),
                    )
                    MetricCell("Average / day", avgDailyValue, c.textPrimary, Modifier.weight(1f))
                }
            }
        }

        item {
            BudgetSummary(
                monthBudgets = monthBudgets,
                spentByCategory = spentByCategory,
                symbol = symbol,
                daysLeft = (Dates.parseMonth(ym).lengthOfMonth() - days).coerceAtLeast(0),
            )
        }

        item {
            Row(
                Modifier.fillMaxWidth().padding(top = Space.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary,
                )
                // The empty state already says how to set a budget, so the
                // header hint only earns its place once budgets exist (then it
                // tells you the rows are tappable to edit).
                if (monthBudgets.isNotEmpty()) {
                    Text(
                        "Tap to set a budget",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textTertiary,
                    )
                }
            }
        }

        item {
            // Only show categories that actually matter this month - a wall of
            // eight empty rows was the noisiest part of this screen.
            val relevant = expenseCategories.filter { category ->
                monthBudgets[category.name] != null || (spentByCategory[category.name] ?: 0.0) > 0.0
            }
            val hidden = expenseCategories.size - relevant.size
            val visible = if (showAllCategories || relevant.isEmpty()) expenseCategories else relevant

            SpCard(contentPadding = PaddingValues(Space.xs)) {
                visible.forEachIndexed { index, category ->
                    val spent = spentByCategory[category.name] ?: 0.0
                    BudgetRow(
                        category = category,
                        spent = spent,
                        budget = monthBudgets[category.name],
                        symbol = symbol,
                        onSetBudget = { editingBudgetFor = category },
                    )
                    if (index < visible.lastIndex) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 60.dp)
                                .height(1.dp)
                                .background(c.border.copy(alpha = 0.7f))
                        )
                    }
                }
                if (hidden > 0) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(c.border.copy(alpha = 0.7f))
                    )
                    Text(
                        if (showAllCategories) "Show less" else "Show $hidden more",
                        style = MaterialTheme.typography.labelLarge,
                        color = c.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .clickable { showAllCategories = !showAllCategories }
                            .padding(horizontal = Space.md, vertical = Space.md),
                    )
                }
            }
        }
    }

    editingBudgetFor?.let { category ->
        BudgetDialog(
            category = category,
            monthKey = ym,
            current = monthBudgets[category.name],
            symbol = symbol,
            onDismiss = { editingBudgetFor = null },
            onSave = { value ->
                BudgetStore.setBudget(ym, category.name, value)
                editingBudgetFor = null
            },
        )
    }
}

@Composable
private fun MetricCell(label: String, value: String, valueColor: Color, modifier: Modifier) {
    val c = SpTheme.colors
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun BudgetSummary(
    monthBudgets: Map<String, Double>,
    spentByCategory: Map<String, Double>,
    symbol: String,
    daysLeft: Int,
) {
    val c = SpTheme.colors
    if (monthBudgets.isEmpty()) {
        SpCard {
            Text("No budgets yet", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
            Spacer(Modifier.height(Space.xs))
            Text(
                "Tap a category below to give it a monthly limit.",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary,
            )
        }
        return
    }

    val totalBudget = Money.sum(monthBudgets.values)
    val totalSpent = Money.sum(monthBudgets.keys.map { spentByCategory[it] ?: 0.0 })
    val remaining = totalBudget - totalSpent
    val fraction = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f
    val barColor = when {
        fraction >= 1f -> c.expense
        fraction >= 0.8f -> c.warn
        else -> c.income
    }

    SpCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Monthly budget", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(
                    "$daysLeft day${if (daysLeft == 1) "" else "s"} left",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textSecondary,
                )
            }
            SpPill(
                text = if (remaining < 0) "Over by " + Money.compact(kotlin.math.abs(remaining), symbol)
                else Money.compact(remaining, symbol) + " left",
                color = if (remaining < 0) c.expense else c.income,
            )
        }
        Spacer(Modifier.height(Space.lg))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                Money.format(totalSpent, symbol),
                style = MaterialTheme.typography.headlineSmall,
                color = c.textPrimary,
            )
            Spacer(Modifier.width(Space.xs))
            Text(
                "of ${Money.format(totalBudget, symbol)}",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary,
                modifier = Modifier.padding(bottom = 3.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${"%.0f".format(fraction * 100)}%",
                style = MaterialTheme.typography.labelLarge,
                color = barColor,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(Space.md))
        SpProgress(fraction = fraction, color = barColor, height = 10.dp)
    }
}

@Composable
private fun BudgetRow(
    category: Category,
    spent: Double,
    budget: Double?,
    symbol: String,
    onSetBudget: () -> Unit,
) {
    val c = SpTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .clickable(onClick = onSetBudget)
            .padding(horizontal = Space.sm, vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon = category.icon, tint = category.color, size = 36.dp, iconSize = 18.dp)
        Spacer(Modifier.width(Space.md))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    category.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    if (budget == null) Money.format(spent, symbol)
                    else Money.format(spent, symbol) + " / " + Money.format(budget, symbol),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (budget == null) c.textSecondary else c.textPrimary,
                )
            }
            // Rows without a budget stay single-line: the "Categories" header
            // already says "Tap to set a budget", so repeating it eight times
            // was both noise and an uneven row height.
            if (budget != null) {
                Spacer(Modifier.height(Space.sm))
                val fraction = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
                val ratio = if (budget > 0) spent / budget else 0.0
                val barColor = when {
                    ratio >= 1.0 -> c.expense
                    ratio >= 0.8 -> c.warn
                    else -> c.income
                }
                SpProgress(fraction = fraction, color = barColor, height = 6.dp)
            }
        }
    }
}

@Composable
private fun BudgetDialog(
    category: Category,
    monthKey: String,
    current: Double?,
    symbol: String,
    onDismiss: () -> Unit,
    onSave: (Double?) -> Unit,
) {
    val c = SpTheme.colors
    var input by remember(category, current) {
        mutableStateOf(current?.let { Money.toInput(it) } ?: "")
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.cardElevated,
        shape = RoundedCornerShape(Radius.xl),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = category.icon, tint = category.color, size = 34.dp, iconSize = 17.dp)
                Spacer(Modifier.width(Space.sm))
                Text("${category.label} budget")
            }
        },
        text = {
            Column {
                Text(
                    "Monthly limit for ${Dates.prettyMonth(monthKey)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textSecondary,
                )
                Spacer(Modifier.height(Space.md))
                OutlinedTextField(
                    value = input,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(10)
                        if (filtered.count { it == '.' || it == ',' } <= 1) {
                            input = filtered
                            error = null
                        }
                    },
                    prefix = { Text("$symbol ") },
                    placeholder = { Text("e.g. 300", color = c.textTertiary) },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    singleLine = true,
                    shape = RoundedCornerShape(Radius.md),
                    colors = spFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (input.isBlank()) {
                    onSave(null)
                } else {
                    val value = Money.parse(input)
                    if (value == null || value <= 0.0) error = "Enter a positive amount" else onSave(value)
                }
            }) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            Row {
                if (current != null) {
                    TextButton(onClick = { onSave(null) }) {
                        Text("Remove", color = c.expense)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel", color = c.textSecondary) }
            }
        },
    )
}
