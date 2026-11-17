package com.spenzo.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.spenzo.TransactionViewModel
import com.spenzo.data.*
import com.spenzo.ui.components.SegmentedToggle
import com.spenzo.ui.components.SpCard
import com.spenzo.ui.components.spFieldColors
import com.spenzo.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    fun submit() {
        val parsed = Money.parse(amount)
        nameError = if (name.isBlank()) "Give this transaction a name" else null
        amountError = when {
            parsed == null -> "Enter a valid amount"
            parsed <= 0.0 -> "Amount must be greater than 0"
            else -> null
        }
        if (nameError != null || amountError != null || parsed == null) return

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
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editing) "Edit transaction" else if (isExpense) "New expense" else "New income",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    Box(
                        Modifier
                            .padding(start = Space.sm)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(c.card)
                            .border(BorderStroke(1.dp, c.border), CircleShape)
                            .clickable { nav.popBackStack() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            "Back",
                            tint = c.textPrimary,
                            modifier = Modifier.size(20.dp),
                        )
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
                        .padding(horizontal = Insets.gutter, vertical = Space.md),
                ) {
                    Button(
                        onClick = { submit() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(Radius.pill),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = c.primary,
                            contentColor = c.onPrimary,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Text(
                            if (editing) "Save changes" else "Add transaction",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (!loaded) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.primary, strokeWidth = 2.dp)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Insets.gutter),
            verticalArrangement = Arrangement.spacedBy(Space.lg),
        ) {
            SpCard(contentPadding = PaddingValues(Space.lg)) {
                SegmentedToggle(
                    options = listOf("Expense", "Income"),
                    selectedIndex = if (isExpense) 0 else 1,
                    onSelect = { isExpense = it == 0 },
                )
                Spacer(Modifier.height(Space.xl))
                Text("Amount", style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                Spacer(Modifier.height(Space.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        symbol,
                        style = MaterialTheme.typography.headlineSmall,
                        color = c.textSecondary,
                        modifier = Modifier.padding(end = Space.sm, top = 6.dp),
                    )
                    BasicTextField(
                        value = amount,
                        onValueChange = { raw ->
                            val filtered = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                            if (filtered.count { it == '.' || it == ',' } <= 1) {
                                amount = filtered
                                amountError = null
                            }
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 40.sp,
                            lineHeight = 46.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp,
                            color = if (isExpense) c.textPrimary else c.textPrimary,
                        ),
                        cursorBrush = SolidColor(c.primary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (amount.isEmpty()) {
                                Text(
                                    "0.00",
                                    fontSize = 40.sp,
                                    lineHeight = 46.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-1).sp,
                                    color = c.textTertiary,
                                )
                            }
                            inner()
                        },
                    )
                }
                Spacer(Modifier.height(Space.sm))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (amountError != null) c.expense else c.border)
                )
                if (amountError != null) {
                    Spacer(Modifier.height(Space.sm))
                    Text(
                        amountError!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = c.expense,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                Text("Category", style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    verticalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    categories.forEach { option ->
                        val selected = category == option.name
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(Radius.pill))
                                .background(if (selected) c.primarySoft else c.card)
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (selected) c.primary.copy(alpha = 0.45f) else c.border,
                                    ),
                                    RoundedCornerShape(Radius.pill),
                                )
                                .clickable { category = option.name }
                                .padding(horizontal = Space.md, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                option.icon,
                                null,
                                tint = if (selected) c.primary else option.color,
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                option.label,
                                color = if (selected) c.primary else c.textPrimary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            SpCard(contentPadding = PaddingValues(Space.lg)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(60); nameError = null },
                    label = { Text("Name") },
                    placeholder = { Text(if (isExpense) "e.g. Lunch" else "e.g. Allowance", color = c.textTertiary) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(Radius.md),
                    colors = spFieldColors(),
                )
                Spacer(Modifier.height(Space.md))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(c.cardSunken)
                        .border(BorderStroke(1.dp, c.border), RoundedCornerShape(Radius.md))
                        .clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, day -> date = LocalDate.of(year, month + 1, day) },
                                date.year,
                                date.monthValue - 1,
                                date.dayOfMonth,
                            ).show()
                        }
                        .padding(horizontal = Space.md, vertical = Space.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.CalendarMonth, null, tint = c.textSecondary, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        if (date == Dates.today()) "Today · ${Dates.prettyDayLong(Dates.toMillis(date))}"
                        else Dates.prettyDayLong(Dates.toMillis(date)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(Icons.Rounded.ChevronRight, null, tint = c.textTertiary, modifier = Modifier.size(19.dp))
                }

                Spacer(Modifier.height(Space.md))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(200) },
                    label = { Text("Note (optional)") },
                    placeholder = { Text("What was this for?", color = c.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(Radius.md),
                    colors = spFieldColors(),
                )
            }

            Spacer(Modifier.height(Space.sm))
        }
    }
}

/** Shows `12.5` instead of `12.499999` when reopening a stored amount. */
private fun formatForEditing(value: Double): String = Money.toInput(value)
