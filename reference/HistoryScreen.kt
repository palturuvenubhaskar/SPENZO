package com.spenzo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.spenzo.TransactionViewModel
import com.spenzo.data.TransactionEntity
import com.spenzo.ui.theme.*

@Composable
fun HistoryScreen(nav: NavHostController, vm: TransactionViewModel = viewModel()) {
    val all by vm.transactions.collectAsStateWithLifecycle(initial = emptyList())
    var query by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val filtered = all.filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
    val currencyFormat = formatCurrency()
    val dateFormat = dateFormat("dd MMM")

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("History", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search transactions") },
            placeholder = { Text("Search by name") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotBlank())
                    IconButton(onClick = { query = "" }, contentDescription = "Clear search") {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(50),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search)
        )

        Spacer(Modifier.height(10.dp))
        if (filtered.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = if (query.isBlank()) "No transactions yet" else "No matches found",
                subtitle = if (query.isBlank()) "Tap + on Home to add your first transaction" else "Try a different search term",
                actionLabel = if (query.isBlank()) "Add transaction" else "Clear search",
                onAction = if (query.isBlank()) { nav.navigate("add_edit") } else { query = "" }
            )
        } else {
            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered, key = { it.id }) { tx ->
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
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } })
    }
}