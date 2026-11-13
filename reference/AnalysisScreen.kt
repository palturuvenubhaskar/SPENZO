package com.spenzo.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.theme.*

// Budgets stored in SharedPreferences (simple, no extra DB table)
object BudgetStore {
    private const val PREF = "spenzo_budgets"
    private fun prefs(c: Context) = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
    fun budget(c: Context, category: String): Float = prefs(c).getFloat(category, 500f)
    fun setBudget(c: Context, category: String, value: Float) =
        prefs(c).edit().putFloat(category, value).apply()
}

@Composable
fun AnalysisScreen(vm: TransactionViewModel = viewModel()) {
    val context = LocalContext.current
    val ie by vm.monthIncomeExpense.collectAsStateWithLifecycle(IncomeExpense(0.0, 0.0))
    val cats by vm.categoryTotals.collectAsStateWithLifecycle(initial = emptyList())
    val ym by vm.currentMonth.collectAsStateWithLifecycle()

    var editingBudgetFor by remember { mutableStateOf<Category?>(null) }
    var budgetInput by remember { mutableStateOf("") }

    val savingsRate = if (ie.income > 0) ((ie.income - ie.expense) / ie.income * 100) else 0.0
    val avgDaily = ie.expense / 30.0

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp)
    ) {

        item { Text("Analysis", style = MaterialTheme.typography.titleLarge) }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Savings rate", formatPercent(savingsRate), if (savingsRate >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error, Modifier.weight(1f))
                StatCard("Avg daily spend", formatCurrency(avgDaily), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Income", formatCurrency(ie.income), MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                StatCard("Expenses", formatCurrency(ie.expense), MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
        }

        item { Text("Budgets — $ym", style = MaterialTheme.typography.titleMedium) }

        items(expenseCategories.size) { i ->
            val cat = expenseCategories[i]
            val spent = cats.firstOrNull { it.category == cat.name }?.total ?: 0.0
            val budget = BudgetStore.budget(context, cat.name).toDouble()
            val pct = if (budget > 0) (spent / budget).coerceIn(0.0, 1.0) else 0.0
            val barColor = when {
                pct >= 1.0 -> MaterialTheme.colorScheme.error
                pct >= 0.8 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.secondary
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .padding(14.dp),
                onClick = {
                    editingBudgetFor = cat
                    budgetInput = budget.toInt().toString()
                },
                role = Role.Button
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(cat.icon, contentDescription = cat.label, tint = cat.color, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            cat.label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${formatCurrency(spent)} / ${formatCurrency(budget)}  · tap to edit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    // Accessible progress bar
                    ProgressIndicator(
                        progress = pct.toFloat(),
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        contentDescription = "${cat.label} budget: ${(pct * 100).toInt()}% used"
                    )
                }
            }
        }
    }

    editingBudgetFor?.let { cat ->
        AlertDialog(
            onDismissRequest = { editingBudgetFor = null },
            title = { Text("Monthly budget for ${cat.label}") },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it.filter { it.isDigit() } },
                    label = { Text("Budget amount") },
                    prefix = { Text("$ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    budgetInput.toFloatOrNull()?.let { BudgetStore.setBudget(context, cat.name, it) }
                    editingBudgetFor = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingBudgetFor = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}