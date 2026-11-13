package com.spenzo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.components.EmptyState
import com.spenzo.ui.components.SectionHeader
import com.spenzo.ui.components.StatTile
import com.spenzo.ui.components.TransactionRow
import com.spenzo.ui.theme.*
import java.time.LocalTime

@Composable
fun HomeScreen(nav: NavHostController, vm: TransactionViewModel) {
    val c = SpTheme.colors
    val symbol = LocalCurrencySymbol.current
    val transactions by vm.transactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val ie by vm.thisMonthIncomeExpense.collectAsStateWithLifecycle(initialValue = IncomeExpense(0.0, 0.0))
    val previous by vm.lastMonthIncomeExpense.collectAsStateWithLifecycle(initialValue = IncomeExpense(0.0, 0.0))
    val month = Dates.currentMonthKey()
    var pendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val greeting = remember {
        when (LocalTime.now().hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .padding(horizontal = Space.xl),
        verticalArrangement = Arrangement.spacedBy(Space.md),
        contentPadding = PaddingValues(top = Space.xxl, bottom = 96.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(greeting, style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
                    Text(Dates.prettyToday(), style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                }
                IconButton(onClick = { nav.navigate("settings") }) {
                    Icon(Icons.Rounded.Settings, "Settings", tint = c.textPrimary)
                }
            }
        }

        item {
            BalanceCard(
                balance = ie.income - ie.expense,
                spentDelta = ie.expense - previous.expense,
                hasPrevious = previous.expense > 0.0,
                monthLabel = Dates.prettyMonth(month),
                symbol = symbol,
                onTopUp = { nav.navigate("add_edit?type=INCOME") },
                onExpense = { nav.navigate("add_edit?type=EXPENSE") },
                onHistory = { nav.navigate("history") },
            )
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                StatTile("Income", Money.compact(ie.income, symbol), c.income, Modifier.weight(1f))
                StatTile("Spent", Money.compact(ie.expense, symbol), c.expense, Modifier.weight(1f))
                // `c.primary` is near-black in light mode, so the dot read as a
                // smudge; savings are green when positive and red when negative.
                StatTile(
                    "Saved",
                    Money.compact(ie.income - ie.expense, symbol),
                    if (ie.income - ie.expense >= 0) c.income else c.expense,
                    Modifier.weight(1f),
                )
            }
        }

        item {
            SectionHeader(
                title = "Recent transactions",
                actionLabel = if (transactions.isEmpty()) null else "View all",
                onAction = { nav.navigate("history") },
                modifier = Modifier.padding(top = Space.sm),
            )
        }

        if (transactions.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.Receipt,
                    title = "No transactions yet",
                    message = "Add your first income or expense to see your balance, charts and budgets.",
                    actionLabel = "Add transaction",
                    onAction = { nav.navigate("add_edit?type=EXPENSE") },
                )
            }
        } else {
            items(transactions.take(10), key = { it.id }) { tx ->
                TransactionRow(
                    tx = tx,
                    onEdit = { nav.navigate("add_edit?txId=${tx.id}") },
                    onDelete = { pendingDelete = tx },
                )
            }
        }
    }

    pendingDelete?.let { tx ->
        DeleteDialog(
            name = tx.name,
            onConfirm = { vm.delete(tx); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun BalanceCard(
    balance: Double,
    spentDelta: Double,
    hasPrevious: Boolean,
    monthLabel: String,
    symbol: String,
    onTopUp: () -> Unit,
    onExpense: () -> Unit,
    onHistory: () -> Unit,
) {
    val brush = Brush.verticalGradient(listOf(Color(0xFF1E2130), Color(0xFF111217)))
    val deltaText = when {
        !hasPrevious -> "First month of tracking"
        spentDelta <= 0.0 -> "${Money.format(-spentDelta, symbol)} less spent than last month"
        else -> "${Money.format(spentDelta, symbol)} more spent than last month"
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.xl))
            .background(brush)
            .border(BorderStroke(1.dp, Color(0xFF2A2D3A)), RoundedCornerShape(Radius.xl))
            .padding(Space.xxl),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                "Balance · $monthLabel",
                color = Color.White.copy(alpha = 0.62f),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(Modifier.height(Space.xs))
            Text(
                Money.format(balance, symbol),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Spacer(Modifier.height(Space.xs))
            Text(
                deltaText,
                color = Color.White.copy(alpha = 0.55f),
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.height(Space.xxl))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CardAction(Icons.Rounded.AddCircle, "Add income", onTopUp)
                CardAction(Icons.Rounded.RemoveCircle, "Add expense", onExpense)
                CardAction(Icons.Rounded.History, "History", onHistory)
            }
        }
    }
}

@Composable
private fun CardAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.md))
            .clickable(onClick = onClick)
            .padding(horizontal = Space.sm, vertical = Space.xs),
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(Space.xs))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

/** Shared delete confirmation used by Home and History. */
@Composable
fun DeleteDialog(name: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val c = SpTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.cardElevated,
        title = { Text("Delete transaction?") },
        text = { Text("\"$name\" will be permanently removed.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = c.expense) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
