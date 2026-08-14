package com.example.spenzo.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spenzo.domain.model.Category
import com.example.spenzo.domain.model.Expense
import com.example.spenzo.domain.usecase.DeleteExpenseUseCase
import com.example.spenzo.domain.usecase.GetMonthlyExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ExpensesState(
    val expenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val selectedCategory: Category? = null,
    val currentMonth: Int = LocalDate.now().monthValue,
    val currentYear: Int = LocalDate.now().year,
    val totalSpent: Double = 0.0
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val getMonthlyExpensesUseCase: GetMonthlyExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExpensesState())
    val state: StateFlow<ExpensesState> = _state.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            getMonthlyExpensesUseCase(_state.value.currentYear, _state.value.currentMonth)
                .collectLatest { expenses ->
                    val filtered = if (_state.value.selectedCategory != null) {
                        expenses.filter { it.category == _state.value.selectedCategory }
                    } else {
                        expenses
                    }
                    _state.value = _state.value.copy(
                        expenses = expenses,
                        filteredExpenses = filtered,
                        totalSpent = expenses.sumOf { it.amount }
                    )
                }
        }
    }

    fun onEvent(event: ExpensesEvent) {
        when (event) {
            is ExpensesEvent.NextMonth -> {
                var month = _state.value.currentMonth + 1
                var year = _state.value.currentYear
                if (month > 12) {
                    month = 1
                    year += 1
                }
                _state.value = _state.value.copy(currentMonth = month, currentYear = year)
                loadExpenses()
            }
            is ExpensesEvent.PreviousMonth -> {
                var month = _state.value.currentMonth - 1
                var year = _state.value.currentYear
                if (month < 1) {
                    month = 12
                    year -= 1
                }
                _state.value = _state.value.copy(currentMonth = month, currentYear = year)
                loadExpenses()
            }
            is ExpensesEvent.SelectCategory -> {
                val newCategory = if (_state.value.selectedCategory == event.category) null else event.category
                val filtered = if (newCategory != null) {
                    _state.value.expenses.filter { it.category == newCategory }
                } else {
                    _state.value.expenses
                }
                _state.value = _state.value.copy(
                    selectedCategory = newCategory,
                    filteredExpenses = filtered
                )
            }
            is ExpensesEvent.DeleteExpense -> {
                viewModelScope.launch {
                    deleteExpenseUseCase(event.expense)
                }
            }
        }
    }
}

sealed class ExpensesEvent {
    object NextMonth : ExpensesEvent()
    object PreviousMonth : ExpensesEvent()
    data class SelectCategory(val category: Category) : ExpensesEvent()
    data class DeleteExpense(val expense: Expense) : ExpensesEvent()
}
