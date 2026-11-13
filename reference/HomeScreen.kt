package com.spenzo.ui

import androidx.compose.foundation.background
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
import java.util.*

@Composable
fun HomeScreen(nav: NavHostController, vm: TransactionViewModel = viewModel()) {
    val txList by vm.transactions.collectAsStateWithLifecycle(initial = emptyList())
    val monthIE by vm.monthIncomeExpense.collectAsStateWithLifecycle(IncomeExpense(0.0, 0.0))
    var pendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    val currencyFormat = formatCurrency()
    val dateFormat = dateFormat("dd MMM")

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Welcome back", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row {
                    IconButton(
                        onClick = { /* TODO: Settings */ },
                        contentDescription = "Settings"
                    ) {
                        Icon(Icons.Outlined.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(
                        onClick = { /* TODO: Notifications */ },
                        contentDescription = "Notifications"
                    ) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        item { BalanceCard(balance = monthIE.income - monthIE.expense, nav = nav, currencyFormat) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryChip("Income", monthIE.income, MaterialTheme.colorScheme.secondary, Modifier.weight(1f), currencyFormat)
                SummaryChip("Spent", monthIE.expense, MaterialTheme.colorScheme.error, Modifier.weight(1f), currencyFormat)
                SummaryChip("Saved", monthIE.income - monthIE.expense, MaterialTheme.colorScheme.primary, Modifier.weight(1f), currencyFormat)
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent transactions", style = MaterialTheme.typography.titleMedium)
                Text(
                    "View all",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { nav.navigate("history") }
                        .padding(16.dp)
                )
            }
        }
        if (txList.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No transactions yet",
                    subtitle = "Tap + to add your first transaction",
                    actionLabel = "Add transaction",
                    onAction = { nav.navigate("add_edit") }
                )
            }
        } else {
            items(txList.take(10), key = { it.id }) { tx ->
                TransactionRow(
                    tx = tx,
                    onEdit = { nav.navigate("add_edit?txId=${tx.id}") },
                    onDelete = { pendingDelete = tx },
                    currencyFormat = currencyFormat,
                    dateFormat = dateFormat
                )
            }
        }
    }

    pendingDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete transaction?") },
            text = { Text("\"${tx.name}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = { vm.delete(tx); pendingDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun BalanceCard(balance: Double, nav: NavHostController, format: (Double) -> String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("My Balance", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(6.dp))
            Text(format(balance), color = MaterialTheme.colorScheme.onPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ActionButton(Icons.Outlined.AddCircle, "Top up", MaterialTheme.colorScheme.onPrimary) { nav.navigate("add_edit") }
                ActionButton(Icons.Outlined.RemoveCircle, "Expense", MaterialTheme.colorScheme.onPrimary) { nav.navigate("add_edit") }
                ActionButton(Icons.Outlined.History, "History", MaterialTheme.colorScheme.onPrimary) { nav.navigate("history") }
                ActionButton(Icons.Outlined.PieChart, "Graphs", MaterialTheme.colorScheme.onPrimary) { nav.navigate("graphs") }
            }
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterVertically, modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 8.dp)
    ) {
        Box(
            Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = tint, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SummaryChip(title: String, value: Double, color: Color, modifier: Modifier = Modifier, format: (Double) -> String) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(6.dp))
        Text(format(value), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TransactionRow(
    tx: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    currencyFormat: (Double) -> String,
    dateFormat: (Long) -> String
) {
    var menuOpen by remember { mutableStateOf(false) }
    val cat = Category.entries.firstOrNull { it.name == tx.category } ?: Category.OTHER
    val isExpense = tx.type == TxType.EXPENSE.name

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
            .combinedClickable(
                onClick = { menuOpen = true },
                onLongClick = { /* Could show context menu */ }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(cat.icon, contentDescription = cat.label, tint = cat.color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.name, style = MaterialTheme.typography.titleMedium, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${cat.label} · ${dateFormat(tx.timestamp)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "${if (isExpense) "-" else "+"}${currencyFormat(tx.amount)}",
            color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                onClick = { menuOpen = false; onEdit() }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = { menuOpen = false; onDelete() }
            )
        }
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onAction) {
            Text(actionLabel)
        }
    }
}