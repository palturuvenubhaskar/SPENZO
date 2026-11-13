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

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp)) {

        item { Text("Analysis", style = MaterialTheme.typography.titleLarge) }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Savings rate", "%.0f%%".format(savingsRate),
                    if (savingsRate >= 0) AccentGreen else ExpenseRed, Modifier.weight(1f))
                StatCard("Avg daily spend", "$" + "%.0f".format(avgDaily), DarkCard, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Income", "$" + "%.0f".format(ie.income), AccentGreen, Modifier.weight(1f))
                StatCard("Expenses", "$" + "%.0f".format(ie.expense), ExpenseRed, Modifier.weight(1f))
            }
        }

        item { Text("Budgets — $ym", style = MaterialTheme.typography.titleMedium) }

        items(expenseCategories.size) { i ->
            val cat = expenseCategories[i]
            val spent = cats.firstOrNull { it.category == cat.name }?.total ?: 0.0
            val budget = BudgetStore.budget(context, cat.name).toDouble()
            val pct = (spent / budget).toFloat().coerceIn(0f, 1f)
            val barColor = when {
                pct >= 1f -> ExpenseRed
                pct >= 0.8f -> AccentOrange
                else -> AccentGreen
            }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface)
                .clickable {
                    editingBudgetFor = cat
                    budgetInput = budget.toInt().toString()
                }.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(34.dp).clip(CircleShape).background(IconBackground),
                        contentAlignment = Alignment.Center) {
                        Icon(cat.icon, cat.label, tint = cat.color, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(cat.label, modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("$" + "%.0f".format(spent) + " / $" + "%.0f".format(budget) +
                        "  · tap to edit", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50))
                    .background(ProgressTrack)) {
                    Box(Modifier.fillMaxWidth(pct).fillMaxHeight().clip(RoundedCornerShape(50))
                        .background(barColor))
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
                    value = budgetInput, onValueChange = { budgetInput = it.filter { c -> c.isDigit() } },
                    prefix = { Text("$ ") }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    budgetInput.toFloatOrNull()?.let { BudgetStore.setBudget(context, cat.name, it) }
                    editingBudgetFor = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingBudgetFor = null }) { Text("Cancel") } })
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Surface).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
