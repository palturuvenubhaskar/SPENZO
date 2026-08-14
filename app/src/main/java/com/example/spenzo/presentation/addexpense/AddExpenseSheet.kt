package com.example.spenzo.presentation.addexpense

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spenzo.domain.model.Category
import com.example.spenzo.presentation.components.getCategoryIcon
import com.example.spenzo.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    onDismiss: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onDismiss()
        }
    }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val isSaveEnabled = state.name.isNotBlank() && state.amount.toDoubleOrNull() != null

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onEvent(AddExpenseEvent.DateChanged(it))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(24.dp)
            .padding(bottom = 24.dp) // extra padding for bottom nav area
    ) {
        Text("Add Expense", style = Typography.headlineMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { viewModel.onEvent(AddExpenseEvent.NameChanged(it)) },
            label = { Text("Expense Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CardDark,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.amount,
            onValueChange = { viewModel.onEvent(AddExpenseEvent.AmountChanged(it)) },
            label = { Text("Amount (₹)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CardDark,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Category", style = Typography.labelMedium, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, TextSecondary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .clickable { showCategoryDropdown = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(getCategoryIcon(state.category), contentDescription = null, tint = TextPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(state.category.displayName, style = Typography.bodyLarge, color = TextPrimary)
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextPrimary)
            }

            DropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { showCategoryDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Category.values().forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName) },
                        leadingIcon = { Icon(getCategoryIcon(category), contentDescription = null) },
                        onClick = {
                            viewModel.onEvent(AddExpenseEvent.CategoryChanged(category))
                            showCategoryDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, TextSecondary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .clickable { showDatePicker = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = TextPrimary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(dateFormatter.format(Date(state.date)), style = Typography.bodyLarge, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text("Cancel", modifier = Modifier.padding(vertical = 8.dp))
            }
            
            Button(
                onClick = { viewModel.onEvent(AddExpenseEvent.SaveExpense) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                enabled = isSaveEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = CardDark, contentColor = TextOnDark)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TextOnDark, strokeWidth = 2.dp)
                } else {
                    Text("Save", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}
