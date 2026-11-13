package com.spenzo.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(nav: NavHostController, txId: Long?, vm: TransactionViewModel = viewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()

    var isExpense by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.FOOD.name) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dayFmt = remember { SimpleDateFormat("yyyy-MM-dd", configuration.locale) }
    val niceFmt = remember { SimpleDateFormat("dd MMM yyyy", configuration.locale) }
    val editing = txId != null && txId > 0

    // Load existing transaction when editing
    LaunchedEffect(txId) {
        if (editing) {
            vm.getById(txId!!)?.let { e ->
                isExpense = e.type == TxType.EXPENSE.name
                name = e.name
                amount = e.amount.toString()
                note = e.note
                category = e.category
                dateMillis = e.timestamp
            }
        }
    }

    val categories = if (isExpense) expenseCategories else incomeCategories
    LaunchedEffect(isExpense) {
        if (categories.none { it.name == category }) category = categories.first().name
    }

    val initialName = remember { mutableStateOf(name) }
    val initialAmount = remember { mutableStateOf(amount) }
    val initialCategory = remember { mutableStateOf(category) }
    val initialDate = remember { mutableStateOf(dateMillis) }
    val initialNote = remember { mutableStateOf(note) }
    val initialIsExpense = remember { mutableStateOf(isExpense) }

    LaunchedEffect(editing) {
        if (editing) {
            initialName.value = name
            initialAmount.value = amount
            initialCategory.value = category
            initialDate.value = dateMillis
            initialNote.value = note
            initialIsExpense.value = isExpense
        }
    }

    val hasUnsavedChanges = remember(name, amount, category, dateMillis, note, isExpense) {
        derivedStateOf {
            name != initialName.value ||
            amount != initialAmount.value ||
            category != initialCategory.value ||
            dateMillis != initialDate.value ||
            note != initialNote.value ||
            isExpense != initialIsExpense.value
        }
    }

    val isFormValid = remember(name, amount) {
        derivedStateOf {
            name.isNotBlank() && amount.toDoubleOrNull() != null
        }
    }

    // Handle back press with unsaved changes confirmation
    BackHandler(enabled = hasUnsavedChanges) {
        // Could show a confirmation dialog here
        // For now, allow back but could be enhanced
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editing) "Edit transaction" else "Add transaction") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedChanges) {
                                // Show confirmation dialog
                                // For now just navigate back
                                nav.popBackStack()
                            } else {
                                nav.popBackStack()
                            }
                        },
                        contentDescription = "Back"
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // INCOME / EXPENSE toggle using SegmentedButton
            SegmentedButton(
                selectedIndex = if (isExpense) 0 else 1,
                onClick = { index -> isExpense = index == 0 },
                modifier = Modifier.fillMaxWidth(),
                singleChoice = true
            ) {
                SegmentedButtonItem(
                    label = { Text("Expense") },
                    selected = isExpense,
                    onClick = { isExpense = true },
                    colors = SegmentedButtonDefaults.segmentedButtonItemColors(
                        containerColor = if (isExpense) MaterialTheme.colorScheme.primary else Color.Transparent,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        contentColor = if (isExpense) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                SegmentedButtonItem(
                    label = { Text("Income") },
                    selected = !isExpense,
                    onClick = { isExpense = false },
                    colors = SegmentedButtonDefaults.segmentedButtonItemColors(
                        containerColor = if (!isExpense) MaterialTheme.colorScheme.primary else Color.Transparent,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        contentColor = if (!isExpense) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = it.isBlank() },
                label = { Text("Name") },
                placeholder = { Text(if (isExpense) "e.g. Lunch" else "e.g. Allowance") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                isError = nameError,
                supportingText = { if (nameError) Text("Name is required", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
            )

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    val filtered = it.filter { c -> c.isDigit() || c == '.' }
                    val parts = filtered.split('.')
                    amount = if (parts.size > 2) parts[0] + "." + parts.drop(1).joinToString("") else filtered
                    amountError = amount.toDoubleOrNull() == null
                },
                label = { Text("Amount") },
                prefix = { Text("$ ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amountError && amount.isNotBlank(),
                supportingText = { if (amountError && amount.isNotBlank()) Text("Enter a valid amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
            )

            Text("Category", style = MaterialTheme.typography.titleMedium)
            LazyRow(
                Modifier.animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories, key = { it.name }) { cat ->
                    val sel = category == cat.name
                    FilterChip(
                        selected = sel,
                        onClick = { category = cat.name },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(cat.icon, contentDescription = null, tint = if (sel) MaterialTheme.colorScheme.onPrimary else cat.color, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(cat.label, color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        leadingIcon = if (sel) { Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary) } else null,
                        colors = FilterChipDefaults.colors(
                            containerColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            labelColor = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            leadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            // Date picker using Compose Material3 DatePickerDialog
            val datePickerState = remember { DatePickerDialog().state }

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(niceFmt.format(Date(dateMillis)))
                }
            }

            DatePickerDialog(
                state = datePickerState,
                onDismissRequest = { showDatePicker = false },
                confirmButton = { Text("OK") },
                dismissButton = { Text("Cancel") }
            ) { showDatePicker }

            LaunchedEffect(datePickerState.date) {
                datePickerState.date?.let { localDate ->
                    val cal = Calendar.getInstance().apply {
                        set(localDate.year, localDate.monthValue - 1, localDate.dayOfMonth)
                    }
                    dateMillis = cal.timeInMillis
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    val tx = Transaction(
                        id = txId ?: 0,
                        name = name.ifBlank { "Transaction" },
                        amount = amt,
                        category = category,
                        type = if (isExpense) TxType.EXPENSE else TxType.INCOME,
                        note = note,
                        dayLabel = dayFmt.format(Date(dateMillis)),
                        timestamp = dateMillis
                    )
                    scope.launch {
                        if (editing) vm.update(tx) else vm.add(tx)
                        nav.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (editing) "Save changes" else "Add transaction", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}