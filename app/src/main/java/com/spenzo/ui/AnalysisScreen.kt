package com.spenzo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.components.SpCard
import com.spenzo.ui.components.StatTile
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

    val monthBudgets = budgets[ym].orEmpty()
    val spentByCategory = remember(categoryTotals) { categoryTotals.associate { it.category to it.total } }
    val savingsRate = if (ie.income > 0) ((ie.income - ie.expense) / ie.income * 100) else 0.0
    val days = Dates.daysElapsed(ym)
    val avgDaily = if (days > 0) ie.expense / days else 0.0

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
            .padding(horizontal = Space.xl),
        verticalArrangement = Arrangement.spacedBy(Space.md),
        contentPadding = PaddingValues(top = Space.xl, bottom = 96.dp),
    ) {
        item {
            Column {
                Text("Analysis", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)
                Text(
                    "Average daily spend over $days day${if (days == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textSecondary,
                )
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton({ vm.shiftMonth(-1) }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Previous month", tint = c.textPrimary)
                }
                Text(
                    Dates.prettyMonth(ym),
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary,
                )
                IconButton({ vm.shiftMonth(1) }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Next month", tint = c.textPrimary)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                StatTile(
                    "Savings rate",
                    "${"%.0f".format(savingsRate)}%",
                    if (savingsRate >= 0) c.income else c.expense,
                    Modifier.weight(1f),
                )
                StatTile("Avg / day", Money.compact(avgDaily, symbol), c.warn, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                StatTile("Income", Money.compact(ie.income, symbol), c.income, Modifier.weight(1f))
                StatTile("Expenses", Money.compact(ie.expense, symbol), c.expense, Modifier.weight(1f))
            }
        }

        item {
            BudgetSummary(
                monthBudgets = monthBudgets,
                spentByCategory = spentByCategory,
                symbol = symbol,
            )
        }

        item {
            Text(
                "Budgets · ${Dates.prettyMonth(ym)}",
                style = MaterialTheme.typography.titleMedium,
                color = c.textPrimary,
                modifier = Modifier.padding(top = Space.sm),
            )
        }

        items(expenseCategories.size, key = { expenseCategories[it].name }) { index ->
            val category = expenseCategories[index]
            val spent = spentByCategory[category.name] ?: 0.0
            BudgetRow(
                category = category,
                spent = spent,
                budget = monthBudgets[category.name],
                symbol = symbol,
                onSetBudget = { editingBudgetFor = category },
            )
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
private fun BudgetSummary(
    monthBudgets: Map<String, Double>,
    spentByCategory: Map<String, Double>,
    symbol: String,
) {
    val c = SpTheme.colors
    if (monthBudgets.isEmpty()) {
        SpCard {
            Text("Set a budget", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
            Spacer(Modifier.height(Space.xs))
            Text(
                "Pick a monthly limit for the categories you care about — you can change it any time.",
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total budget", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
            Text(
                Money.format(remaining, symbol) + " left",
                style = MaterialTheme.typography.labelMedium,
                color = if (remaining < 0) c.expense else c.textSecondary,
            )
        }
        Spacer(Modifier.height(Space.sm))
        Text(
            Money.format(totalSpent, symbol) + " of " + Money.format(totalBudget, symbol),
            style = MaterialTheme.typography.labelMedium,
            color = c.textSecondary,
        )
        Spacer(Modifier.height(Space.md))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(Radius.pill))
                .background(c.track),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(barColor),
            )
        }
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
            .background(c.card)
            .border(BorderStroke(1.dp, c.border), RoundedCornerShape(Radius.md))
            .clickable(onClick = onSetBudget)
            .padding(Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape)
                .background(category.color.copy(alpha = if (c.isDark) 0.18f else 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(category.icon, category.label, tint = category.color, modifier = Modifier.size(16.dp))
        }
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
                    color = c.textSecondary,
                )
            }
            Spacer(Modifier.height(Space.sm))
            if (budget == null) {
                Text(
                    "Set a monthly budget",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.income,
                    fontWeight = FontWeight.Medium,
                )
            } else {
                val fraction = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
                val barColor = when {
                    budget > 0 && spent / budget >= 1.0 -> c.expense
                    budget > 0 && spent / budget >= 0.8 -> c.warn
                    else -> c.income
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(c.track),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(Radius.pill))
                            .background(barColor),
                    )
                }
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
        title = { Text("${category.label} budget") },
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
                    placeholder = { Text("e.g. 300", color = c.textSecondary) },
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
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (current != null) {
                    TextButton(onClick = { onSave(null) }) {
                        Text("Remove", color = c.expense)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}
