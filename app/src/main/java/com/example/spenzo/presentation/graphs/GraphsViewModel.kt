package com.example.spenzo.presentation.graphs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spenzo.domain.usecase.CategorySummary
import com.example.spenzo.domain.usecase.GetCategorySummaryUseCase
import com.example.spenzo.domain.usecase.GetMonthlyExpensesUseCase
import com.example.spenzo.domain.usecase.GetYearlyExpensesUseCase
import com.example.spenzo.presentation.components.BarData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

enum class TimePeriod {
    MONTHLY, YEARLY
}

data class GraphsState(
    val period: TimePeriod = TimePeriod.MONTHLY,
    val currentMonth: Int = LocalDate.now().monthValue,
    val currentYear: Int = LocalDate.now().year,
    val totalSpent: Double = 0.0,
    val barData: List<BarData> = emptyList(),
    val categorySummaries: List<CategorySummary> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GraphsViewModel @Inject constructor(
    private val getMonthlyExpensesUseCase: GetMonthlyExpensesUseCase,
    private val getYearlyExpensesUseCase: GetYearlyExpensesUseCase,
    private val getCategorySummaryUseCase: GetCategorySummaryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GraphsState())
    val state: StateFlow<GraphsState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.flatMapLatest { state ->
                val expensesFlow = if (state.period == TimePeriod.MONTHLY) {
                    getMonthlyExpensesUseCase(state.currentYear, state.currentMonth)
                } else {
                    getYearlyExpensesUseCase(state.currentYear)
                }
                
                combine(expensesFlow, getCategorySummaryUseCase(expensesFlow)) { expenses, categories ->
                    val total = expenses.sumOf { it.amount }
                    
                    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
                    
                    val barData = if (state.period == TimePeriod.MONTHLY) {
                        val daysInMonth = YearMonth.of(state.currentYear, state.currentMonth).lengthOfMonth()
                        val grouped = expenses.groupBy { 
                            LocalDate.ofInstant(java.time.Instant.ofEpochMilli(it.date), ZoneId.systemDefault()).dayOfMonth 
                        }
                        (1..daysInMonth).map { day ->
                            val dayTotal = grouped[day]?.sumOf { it.amount } ?: 0.0
                            BarData(
                                label = day.toString(),
                                value = dayTotal.toFloat(),
                                displayValue = formatter.format(dayTotal)
                            )
                        }
                    } else {
                        val grouped = expenses.groupBy { 
                            LocalDate.ofInstant(java.time.Instant.ofEpochMilli(it.date), ZoneId.systemDefault()).monthValue 
                        }
                        val monthNames = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
                        (1..12).map { month ->
                            val monthTotal = grouped[month]?.sumOf { it.amount } ?: 0.0
                            BarData(
                                label = monthNames[month - 1],
                                value = monthTotal.toFloat(),
                                displayValue = formatter.format(monthTotal)
                            )
                        }
                    }
                    
                    state.copy(
                        totalSpent = total,
                        barData = barData,
                        categorySummaries = categories
                    )
                }
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun onEvent(event: GraphsEvent) {
        when (event) {
            is GraphsEvent.TogglePeriod -> {
                _state.value = _state.value.copy(period = event.period)
            }
            is GraphsEvent.NextPeriod -> {
                if (_state.value.period == TimePeriod.MONTHLY) {
                    var month = _state.value.currentMonth + 1
                    var year = _state.value.currentYear
                    if (month > 12) { month = 1; year += 1 }
                    _state.value = _state.value.copy(currentMonth = month, currentYear = year)
                } else {
                    _state.value = _state.value.copy(currentYear = _state.value.currentYear + 1)
                }
            }
            is GraphsEvent.PreviousPeriod -> {
                if (_state.value.period == TimePeriod.MONTHLY) {
                    var month = _state.value.currentMonth - 1
                    var year = _state.value.currentYear
                    if (month < 1) { month = 12; year -= 1 }
                    _state.value = _state.value.copy(currentMonth = month, currentYear = year)
                } else {
                    _state.value = _state.value.copy(currentYear = _state.value.currentYear - 1)
                }
            }
        }
    }
}

sealed class GraphsEvent {
    data class TogglePeriod(val period: TimePeriod) : GraphsEvent()
    object NextPeriod : GraphsEvent()
    object PreviousPeriod : GraphsEvent()
}
