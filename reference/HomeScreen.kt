package com.spenzo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(nav: NavHostController, vm: TransactionViewModel = viewModel()) {
    val txList by vm.transactions.collectAsStateWithLifecycle(initial = emptyList())
    val monthIE by vm.monthIncomeExpense.collectAsStateWithLifecycle(IncomeExpense(0.0, 0.0))
    var pendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Welcome back", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                Row {
                    IconButton(onClick = { }) { Icon(Icons.Outlined.Settings, "Settings", tint = TextPrimary) }
                    IconButton(onClick = { }) { Icon(Icons.Outlined.Notifications, "Alerts", tint = TextPrimary) }
                }
            }
        }
        item { BalanceCard(balance = monthIE.income - monthIE.expense, nav = nav) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryChip("Income", monthIE.income, AccentGreen, Modifier.weight(1f))
                SummaryChip("Spent", monthIE.expense, ExpenseRed, Modifier.weight(1f))
                SummaryChip("Saved", monthIE.income - monthIE.expense, DarkCard, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Recent transactions", style = MaterialTheme.typography.titleMedium)
                Text("View all", color = TextSecondary, style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.clickable { nav.navigate("history") })
            }
        }
        items(txList.take(10), key = { it.id }) { tx ->
            TransactionRow(
                tx = tx,
                onEdit = { nav.navigate("add_edit?txId=${tx.id}") },
                onDelete = { pendingDelete = tx }
            )
        }
    }

    pendingDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete transaction?") },
            text = { Text("\"" + tx.name + "\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = { vm.delete(tx); pendingDelete = null }) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun BalanceCard(balance: Double, nav: NavHostController) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(DarkCard).padding(24.dp)) {
        Column(horizontalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("My Balance", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(6.dp))
            Text("$" + "%.2f".format(balance), color = Surface, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ActionButton(Icons.Outlined.AddCircle, "Top up")   { nav.navigate("add_edit") }
                ActionButton(Icons.Outlined.RemoveCircle, "Expense") { nav.navigate("add_edit") }
                ActionButton(Icons.Outlined.History, "History")    { nav.navigate("history") }
                ActionButton(Icons.Outlined.PieChart, "Graphs")    { nav.navigate("graphs") }
            }
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onClick)) {
        Box(Modifier.size(46.dp).clip(CircleShape).background(Surface.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = Surface, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Surface, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SummaryChip(title: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Spacer(Modifier.height(6.dp))
        Text("$" + "%.0f".format(value), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

// Shared row used by Home + History — tap to open Edit/Delete menu
@Composable
fun TransactionRow(tx: TransactionEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    val cat = Category.entries.firstOrNull { it.name == tx.category } ?: Category.OTHER
    val isExpense = tx.type == TxType.EXPENSE.name
    val dateFmt = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface)
        .clickable { menuOpen = true }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(IconBackground),
            contentAlignment = Alignment.Center) {
            Icon(cat.icon, cat.label, tint = cat.color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.name, style = MaterialTheme.typography.titleMedium, fontSize = 15.sp)
            Text(cat.label + " · " + dateFmt.format(Date(tx.timestamp)),
                style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Text((if (isExpense) "-$" else "+$") + "%.2f".format(tx.amount),
            color = if (isExpense) ExpenseRed else AccentGreen,
            fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Box {
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                    onClick = { menuOpen = false; onEdit() })
                DropdownMenuItem(
                    text = { Text("Delete", color = ExpenseRed) },
                    leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = ExpenseRed) },
                    onClick = { menuOpen = false; onDelete() })
            }
        }
    }
}
