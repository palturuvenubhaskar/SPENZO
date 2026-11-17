package com.spenzo.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.components.EmptyState
import com.spenzo.ui.components.RowDivider
import com.spenzo.ui.components.SpCard
import com.spenzo.ui.components.TransactionRow
import com.spenzo.ui.theme.*

private enum class TxFilter(val label: String) { ALL("All"), INCOME("Income"), EXPENSE("Expense") }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(nav: NavHostController, vm: TransactionViewModel) {
    val c = SpTheme.colors
    val symbol = LocalCurrencySymbol.current
    val all by vm.transactions.collectAsStateWithLifecycle(initialValue = emptyList())
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(TxFilter.ALL) }
    var pendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val filtered by remember(all, query, filter) {
        derivedStateOf {
            val q = query.trim()
            all.filter { tx ->
                val typeOk = when (filter) {
                    TxFilter.ALL -> true
                    TxFilter.INCOME -> tx.type == TxType.INCOME.name
                    TxFilter.EXPENSE -> tx.type == TxType.EXPENSE.name
                }
                val textOk = q.isBlank() ||
                    tx.name.contains(q, ignoreCase = true) ||
                    tx.note.contains(q, ignoreCase = true) ||
                    Category.fromName(tx.category).label.contains(q, ignoreCase = true)
                typeOk && textOk
            }
        }
    }
    val groups by remember(filtered) { derivedStateOf { filtered.groupBy { it.dayLabel } } }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .padding(horizontal = Insets.gutter),
    ) {
        Spacer(Modifier.height(Space.xxl))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("History", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)
            Spacer(Modifier.width(Space.sm))
            if (filtered.isNotEmpty()) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(c.cardSunken)
                        .padding(horizontal = Space.sm, vertical = 2.dp),
                ) {
                    Text(
                        "${filtered.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textSecondary,
                    )
                }
            }
        }
        Spacer(Modifier.height(Space.lg))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search name, note or category", color = c.textTertiary) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = c.textTertiary, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton({ query = "" }) {
                        Icon(Icons.Rounded.Close, "Clear search", tint = c.textSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            },
            colors = spSearchColors(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(Radius.pill),
            textStyle = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(Space.md))

        Row(
            horizontalArrangement = Arrangement.spacedBy(Space.sm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TxFilter.entries.forEach { option ->
                val selected = filter == option
                Box(
                    Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(if (selected) c.primarySoft else c.card)
                        .border(
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selected) c.primary.copy(alpha = 0.35f) else c.border,
                            ),
                            RoundedCornerShape(Radius.pill),
                        )
                        .clickable { filter = option }
                        .padding(horizontal = Space.lg, vertical = Space.sm),
                ) {
                    Text(
                        option.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) c.primary else c.textSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }

        Spacer(Modifier.height(Space.md))

        when {
            all.isEmpty() -> EmptyState(
                icon = Icons.Rounded.Receipt,
                title = "Nothing here yet",
                message = "Transactions you add will show up here, grouped by day.",
            )

            filtered.isEmpty() -> EmptyState(
                icon = Icons.Rounded.Search,
                title = "No matches",
                message = "Try a different name, note, category or filter.",
            )

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Space.sm),
                contentPadding = PaddingValues(bottom = Insets.bottomContent),
            ) {
                groups.forEach { (day, rows) ->
                    stickyHeader(key = "header_$day") {
                        DayHeader(day = day, rows = rows, symbol = symbol)
                    }
                    item(key = "card_$day") {
                        SpCard(contentPadding = PaddingValues(Space.xs)) {
                            rows.forEachIndexed { index, tx ->
                                TransactionRow(
                                    tx = tx,
                                    onEdit = { nav.navigate("add_edit?txId=${tx.id}") },
                                    onDelete = { pendingDelete = tx },
                                    showDate = false,
                                )
                                if (index < rows.lastIndex) RowDivider(startIndent = 60)
                            }
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
private fun spSearchColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SpTheme.colors.card,
    unfocusedContainerColor = SpTheme.colors.card,
    focusedBorderColor = SpTheme.colors.primary.copy(alpha = 0.45f),
    unfocusedBorderColor = SpTheme.colors.border,
    focusedTextColor = SpTheme.colors.textPrimary,
    unfocusedTextColor = SpTheme.colors.textPrimary,
    cursorColor = SpTheme.colors.primary,
)

@Composable
private fun DayHeader(day: String, rows: List<TransactionEntity>, symbol: String) {
    val c = SpTheme.colors
    val spent = Money.sum(rows.filter { it.type == TxType.EXPENSE.name }.map { it.amount })
    val income = Money.sum(rows.filter { it.type == TxType.INCOME.name }.map { it.amount })
    Row(
        Modifier
            .fillMaxWidth()
            .background(c.background)
            .padding(top = Space.md, bottom = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            Dates.groupLabel(day),
            style = MaterialTheme.typography.labelLarge,
            color = c.textSecondary,
        )
        Spacer(Modifier.weight(1f))
        if (income > 0.0) {
            Text(
                "+" + Money.format(income, symbol),
                style = MaterialTheme.typography.labelMedium,
                color = c.income,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(Space.sm))
        }
        if (spent > 0.0) {
            Text(
                "−" + Money.format(spent, symbol),
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
