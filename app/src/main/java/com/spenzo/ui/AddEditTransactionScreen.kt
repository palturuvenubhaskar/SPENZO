package com.spenzo.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.components.SegmentedToggle
import com.spenzo.ui.components.spFieldColors
import com.spenzo.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    nav: NavHostController,
    vm: TransactionViewModel,
    txId: Long?,
    initialType: String? = null,
) {
    val context = LocalContext.current
    val c = SpTheme.colors
    val symbol = LocalCurrencySymbol.current
    val editing = txId != null && txId > 0

    var isExpense by remember { mutableStateOf(initialType != TxType.INCOME.name) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.FOOD.name) }
    var date by remember { mutableStateOf(Dates.today()) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(!editing) }

    LaunchedEffect(txId) {
        if (editing && txId != null) {
            vm.getById(txId)?.let { entity ->
                isExpense = entity.type == TxType.EXPENSE.name
                name = entity.name
                amount = formatForEditing(entity.amount)
                note = entity.note
                category = entity.category
                date = Dates.toLocalDate(entity.timestamp)
            }
            loaded = true
        }
    }

    val categories = if (isExpense) expenseCategories else incomeCategories
    LaunchedEffect(isExpense, loaded) {
        if (loaded && categories.none { it.name == category }) category = categories.first().name
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editing) "Edit transaction" else "Add transaction") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background),
            )
        },
        containerColor = c.background,
        bottomBar = {
            Surface(color = c.background) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = Space.xl, vertical = Space.md),
                ) {
                    Button(
                        onClick = {
                            val parsed = Money.parse(amount)
                            nameError = if (name.isBlank()) "Give this transaction a name" else null
                            amountError = when {
                                parsed == null -> "Enter a valid amount"
                                parsed <= 0.0 -> "Amount must be greater than 0"
                                else -> null
                            }
                            if (nameError != null || amountError != null || parsed == null) return@Button

                            val tx = Transaction(
                                id = txId ?: 0,
                                name = name.trim(),
                                amount = parsed,
                                category = category,
                                type = if (isExpense) TxType.EXPENSE else TxType.INCOME,
                                note = note.trim(),
                                dayLabel = Dates.dayLabel(date),
                                timestamp = Dates.toMillis(date),
                            )
                            if (editing) vm.update(tx) else vm.add(tx)
                            nav.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(Radius.pill),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = c.primary,
                            contentColor = c.onPrimary,
                        ),
                    ) {
                        Text(
                            if (editing) "Save changes" else "Add transaction",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (!loaded) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.income)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.xl),
            verticalArrangement = Arrangement.spacedBy(Space.lg),
        ) {
            SegmentedToggle(
                options = listOf("Expense", "Income"),
                selectedIndex = if (isExpense) 0 else 1,
                onSelect = { isExpense = it == 0 },
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(60); nameError = null },
                label = { Text("Name") },
                placeholder = { Text(if (isExpense) "e.g. Lunch" else "e.g. Allowance") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(Radius.md),
                colors = spFieldColors(),
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { raw ->
                    val filtered = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                    if (filtered.count { it == '.' || it == ',' } <= 1) {
                        amount = filtered
                        amountError = null
                    }
                },
                label = { Text("Amount") },
                prefix = { Text("$symbol ") },
                isError = amountError != null,
                supportingText = amountError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(Radius.md),
                colors = spFieldColors(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                Text(
                    "Category",
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textPrimary,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    items(categories, key = { it.name }) { option ->
                        val selected = category == option.name
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(Radius.pill))
                                .background(if (selected) c.primary else c.card)
                                .border(
                                    BorderStroke(1.dp, if (selected) c.primary else c.border),
                                    RoundedCornerShape(Radius.pill),
                                )
                                .clickable { category = option.name }
                                .padding(horizontal = Space.md, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                option.icon,
                                option.label,
                                tint = if (selected) c.onPrimary else option.color,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(Space.xs))
                            Text(
                                option.label,
                                color = if (selected) c.onPrimary else c.textPrimary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            date = LocalDate.of(year, month + 1, day)
                        },
                        date.year,
                        date.monthValue - 1,
                        date.dayOfMonth,
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.md),
            ) {
                Icon(Icons.Rounded.CalendarMonth, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Space.sm))
                Text(
                    if (date == Dates.today()) "Today · ${Dates.prettyDayLong(Dates.toMillis(date))}"
                    else Dates.prettyDayLong(Dates.toMillis(date)),
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(200) },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(Radius.md),
                colors = spFieldColors(),
            )

            Spacer(Modifier.height(Space.xl))
        }
    }
}

/** Shows `12.5` instead of `12.499999` when reopening a stored amount. */
private fun formatForEditing(value: Double): String = Money.toInput(value)
