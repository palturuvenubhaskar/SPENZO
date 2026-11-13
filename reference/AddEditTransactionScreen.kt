package com.spenzo.ui

import android.app.DatePickerDialog
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
import androidx.compose.ui.graphics.Color
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
fun AddEditTransactionScreen(nav: NavHostController, txId: Long?,
                             vm: TransactionViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isExpense by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.FOOD.name) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val dayFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val niceFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editing) "Edit transaction" else "Add transaction") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                })
        },
        containerColor = Background
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // INCOME / EXPENSE toggle
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(IconBackground).padding(4.dp)) {
                listOf(true to "Expense", false to "Income").forEach { (exp, label) ->
                    val sel = isExpense == exp
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(50))
                        .background(if (sel) DarkCard else Color.Transparent)
                        .clickable { isExpense = exp }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center) {
                        Text(label, color = if (sel) Surface else TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Name") },
                placeholder = { Text(if (isExpense) "e.g. Lunch" else "e.g. Allowance") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))

            OutlinedTextField(
                value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount") }, prefix = { Text("$ ") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))

            Text("Category", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories, key = { it.name }) { cat ->
                    val sel = category == cat.name
                    Row(Modifier.clip(RoundedCornerShape(50))
                        .background(if (sel) DarkCard else Surface)
                        .clickable { category = cat.name }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(cat.icon, cat.label, tint = if (sel) Surface else cat.color,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(cat.label, color = if (sel) Surface else TextPrimary,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
            OutlinedButton(
                onClick = {
                    DatePickerDialog(context,
                        { _, y, m, d -> cal.set(y, m, d); dateMillis = cal.timeInMillis },
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(niceFmt.format(Date(dateMillis)))
            }

            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))

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
                        timestamp = dateMillis)
                    scope.launch {
                        if (editing) vm.update(tx) else vm.add(tx)
                        nav.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard)) {
                Text(if (editing) "Save changes" else "Add transaction",
                    color = Surface, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
