package com.spenzo.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.components.EmptyState
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

    // Efficient filtering using derivedStateOf - only recomputes when inputs change
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
            .padding(horizontal = Space.xl),
    ) {
        Spacer(Modifier.height(Space.xxl))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("History", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)
            Spacer(Modifier.width(Space.sm))
            Text(
                "${filtered.size}",
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
            )
        }
        Spacer(Modifier.height(Space.md))

        // Optimized search bar with filter chips inline
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            // Search field
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search name, note, category", color = c.textSecondary) },
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = c.textSecondary) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton({ query = "" }) { Icon(Icons.Rounded.Close, null, tint = c.textPrimary) }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = c.card,
                    unfocusedContainerColor = c.card,
                    focusedBorderColor = c.income,
                    unfocusedBorderColor = c.border,
                    focusedTextColor = c.textPrimary,
                    unfocusedTextColor = c.textPrimary,
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(Radius.pill),
            )

            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TxFilter.entries.forEach { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { filter = option },
                        label = { Text(option.label) },
                        shape = RoundedCornerShape(Radius.pill),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = c.card,
                            labelColor = c.textSecondary,
                            selectedContainerColor = c.primary,
                            selectedLabelColor = c.onPrimary,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filter == option,
                            borderColor = c.border,
                            selectedBorderColor = c.primary,
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(Space.sm))

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
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                groups.forEach { (day, rows) ->
                    stickyHeader(key = "header_$day") {
                        DayHeader(day = day, rows = rows, symbol = symbol)
                    }
                    items(rows, key = { it.id }) { tx ->
                        TransactionRow(
                            tx = tx,
                            onEdit = { nav.navigate("add_edit?txId=${tx.id}") },
                            onDelete = { pendingDelete = tx },
                        )
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
private fun DayHeader(day: String, rows: List<TransactionEntity>, symbol: String) {
    val c = SpTheme.colors
    val spent = Money.sum(rows.filter { it.type == TxType.EXPENSE.name }.map { it.amount })
    Row(
        Modifier
            .fillMaxWidth()
            .background(c.background)
            .padding(top = Space.sm, bottom = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            Dates.groupLabel(day),
            style = MaterialTheme.typography.titleSmall,
            color = c.textPrimary,
        )
        Spacer(Modifier.weight(1f))
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