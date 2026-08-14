package com.example.spenzo.presentation.expenses

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spenzo.domain.model.Category
import com.example.spenzo.presentation.components.CategoryChip
import com.example.spenzo.presentation.components.GlassCard
import com.example.spenzo.presentation.components.TransactionItem
import com.example.spenzo.presentation.theme.*
import java.text.NumberFormat
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel = hiltViewModel(),
    onAddExpenseClick: () -> Unit,
    isSheetOpen: Boolean
) {
    val state by viewModel.state.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = SurfacePrimary,
                contentColor = TextPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        containerColor = BackgroundBase,
        modifier = Modifier.blur(if (isSheetOpen) 20.dp else 0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Month Navigator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = Pair(state.currentMonth, state.currentYear),
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }, label = "MonthNav"
                ) { (month, year) ->
                    val monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
                    Column {
                        Text(
                            text = monthName,
                            style = Typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = year.toString(),
                            style = Typography.titleLarge,
                            color = TextSecondary
                        )
                    }
                }

                Row {
                    IconButton(onClick = { viewModel.onEvent(ExpensesEvent.PreviousMonth) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous")
                    }
                    IconButton(onClick = { viewModel.onEvent(ExpensesEvent.NextMonth) }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hero Card
            GlassCard(
                backgroundColor = SurfacePrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(text = "Total Spent", style = Typography.bodyLarge, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatter.format(state.totalSpent),
                        style = Typography.displayLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${state.filteredExpenses.size} transactions",
                        style = Typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FILTER BY CATEGORY",
                style = Typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Categories
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(Category.values()) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = state.selectedCategory == category,
                        onClick = { viewModel.onEvent(ExpensesEvent.SelectCategory(category)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Transactions
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (state.filteredExpenses.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No expenses this month\nTap + to add one", style = Typography.bodyLarge, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                } else {
                    items(state.filteredExpenses, key = { it.id }) { expense ->
                        TransactionItem(
                            expense = expense,
                            onDelete = { viewModel.onEvent(ExpensesEvent.DeleteExpense(expense)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}
