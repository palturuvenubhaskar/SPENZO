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

    val filtered = all.filter { query.isBlank() || it.name.contains(query, true) }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("History", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query, onValueChange = { query = it },
            placeholder = { Text("Search by name") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = {
                if (query.isNotBlank()) IconButton({ query = "" }) { Icon(Icons.Outlined.Close, null) }
            },
            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(50))

        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)) {
            items(filtered, key = { it.id }) { tx ->
                TransactionRow(
                    tx = tx,
                    onEdit = { nav.navigate("add_edit?txId=${tx.id}") },
                    onDelete = { pendingDelete = tx })
            }
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
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } })
    }
}
