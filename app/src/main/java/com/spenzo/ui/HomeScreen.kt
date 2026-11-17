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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.components.EmptyState
import com.spenzo.ui.components.RowDivider
import com.spenzo.ui.components.SectionHeader
import com.spenzo.ui.components.SpCard
import com.spenzo.ui.components.SpPill
import com.spenzo.ui.components.Stat
import com.spenzo.ui.components.StatRow
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
            .statusBarsPadding()
            .padding(horizontal = Insets.gutter),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
        contentPadding = PaddingValues(top = Space.xxl, bottom = Insets.bottomContent),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        color = c.textPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        Dates.prettyToday(),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(c.card)
                        .border(BorderStroke(1.dp, c.border), CircleShape)
                        .clickable { nav.navigate("settings") },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Settings, "Settings", tint = c.textPrimary, modifier = Modifier.size(21.dp))
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
                spent = ie.expense,
                income = ie.income,
            )
        }

        item {
            SectionHeader(
                title = "Recent activity",
                actionLabel = if (transactions.isEmpty()) null else "View all",
                onAction = { nav.navigate("history") },
                modifier = Modifier.padding(top = Space.sm),
            )
        }

        if (transactions.isEmpty()) {
            item {
                SpCard(contentPadding = PaddingValues(0.dp)) {
                    EmptyState(
                        icon = Icons.Rounded.Receipt,
                        title = "No transactions yet",
                        message = "Add your first income or expense to see your balance, charts and budgets.",
                        actionLabel = "Add transaction",
                        onAction = { nav.navigate("add_edit?type=EXPENSE") },
                    )
                }
            }
        } else {
            item {
                SpCard(contentPadding = PaddingValues(horizontal = Space.xs, vertical = Space.xs)) {
                    transactions.take(8).forEachIndexed { index, tx ->
                        TransactionRow(
                            tx = tx,
                            onEdit = { nav.navigate("add_edit?txId=${tx.id}") },
                            onDelete = { pendingDelete = tx },
                        )
                        if (index < minOf(7, transactions.lastIndex)) {
                            RowDivider(startIndent = 60)
                        }
                    }
                }
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
    spent: Double,
    income: Double,
) {
    val c = SpTheme.colors
    val shape = RoundedCornerShape(Radius.xl)
    val spentLess = spentDelta <= 0.0
    val deltaText = when {
        !hasPrevious -> ""
        spentLess -> "${Money.format(-spentDelta, symbol)} less than last month"
        else -> "${Money.format(spentDelta, symbol)} more than last month"
    }
    val deltaColor = when {
        !hasPrevious -> c.heroMuted
        spentLess -> c.heroGlow
        else -> Color(0xFFFF8A8A)
    }
    val showDelta = hasPrevious

    // Softer, less vibrant colors for the hero card
    val spentColor = Color(0xFFFF6B6B)   // softer red
    val incomeColor = Color(0xFF6BCB77)  // softer green
    val balanceColor = Color.White

    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF0D1A2B), Color(0xFF14283E))))
            .border(BorderStroke(1.dp, c.heroBorder), shape),
    ) {
        // Soft accent glow
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(220.dp)
                .offset(x = 60.dp, y = (-70).dp)
                .background(
                    Brush.radialGradient(
                        listOf(c.heroGlow.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(Modifier.fillMaxWidth().padding(Space.xxl)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Total balance",
                    color = c.heroMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(horizontal = Space.sm, vertical = 3.dp),
                ) {
                    Text(
                        monthLabel,
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Spacer(Modifier.height(Space.lg))

            // SPENT — hero, centered, largest
            Text(
                "SPENT",
                color = c.heroMuted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(Space.xs))
            Text(
                Money.format(spent, symbol),
                color = spentColor,
                fontSize = 44.sp,
                lineHeight = 52.sp,
                letterSpacing = (-1).sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(Space.lg))

            // INCOME (left) | BALANCE (right) - smaller, no overlap
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Income", color = c.heroMuted, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        Money.format(income, symbol),
                        color = incomeColor,
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        letterSpacing = (-0.5).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Balance", color = c.heroMuted, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        Money.format(balance, symbol),
                        color = balanceColor,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        letterSpacing = (-1).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }

            if (showDelta) {
                Spacer(Modifier.height(Space.xl))
                SpPill(
                    text = deltaText,
                    color = deltaColor,
                    background = deltaColor.copy(alpha = 0.14f),
                    icon = when {
                        spentLess -> Icons.Rounded.ArrowDownward
                        else -> Icons.Rounded.ArrowUpward
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

/** Shared delete confirmation used by Home and History. */
@Composable
fun DeleteDialog(name: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val c = SpTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.cardElevated,
        shape = RoundedCornerShape(Radius.xl),
        title = { Text("Delete transaction?") },
        text = { Text("\"$name\" will be permanently removed.", color = c.textSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = c.expense) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = c.textSecondary) } },
    )
}
