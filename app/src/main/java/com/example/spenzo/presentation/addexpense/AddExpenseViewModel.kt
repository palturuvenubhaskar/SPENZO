package com.example.spenzo.presentation.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spenzo.domain.model.Category
import com.example.spenzo.domain.model.Expense
import com.example.spenzo.domain.usecase.AddExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddExpenseState(
    val name: String = "",
    val amount: String = "",
    val category: Category = Category.OTHER,
    val date: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddExpenseState())
    val state: StateFlow<AddExpenseState> = _state.asStateFlow()

    fun onEvent(event: AddExpenseEvent) {
        when (event) {
            is AddExpenseEvent.NameChanged -> _state.value = _state.value.copy(name = event.name)
            is AddExpenseEvent.AmountChanged -> _state.value = _state.value.copy(amount = event.amount)
            is AddExpenseEvent.CategoryChanged -> _state.value = _state.value.copy(category = event.category)
            is AddExpenseEvent.DateChanged -> _state.value = _state.value.copy(date = event.date)
            is AddExpenseEvent.SaveExpense -> {
                viewModelScope.launch {
                    _state.value = _state.value.copy(isSaving = true)
                    val amountDouble = _state.value.amount.toDoubleOrNull() ?: 0.0
                    val expense = Expense(
                        name = _state.value.name,
                        amount = amountDouble,
                        category = _state.value.category,
                        date = _state.value.date
                    )
                    addExpenseUseCase(expense)
                    // simulate brief loading spinner
                    kotlinx.coroutines.delay(500)
                    _state.value = _state.value.copy(isSaving = false, isSaved = true)
                }
            }
        }
    }
}

sealed class AddExpenseEvent {
    data class NameChanged(val name: String) : AddExpenseEvent()
    data class AmountChanged(val amount: String) : AddExpenseEvent()
    data class CategoryChanged(val category: Category) : AddExpenseEvent()
    data class DateChanged(val date: Long) : AddExpenseEvent()
    object SaveExpense : AddExpenseEvent()
}
