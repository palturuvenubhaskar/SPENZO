package com.example.spenzo.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spenzo.domain.usecase.CategorySummary
import com.example.spenzo.domain.usecase.GetCategorySummaryUseCase
import com.example.spenzo.domain.usecase.GetMonthlyExpensesUseCase
import com.example.spenzo.domain.usecase.GetYearlyExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class TimePeriod {
    MONTHLY, YEARLY
}

data class AnalysisState(
    val period: TimePeriod = TimePeriod.MONTHLY,
    val currentMonth: Int = LocalDate.now().monthValue,
    val currentYear: Int = LocalDate.now().year,
    val totalSpent: Double = 0.0,
    val transactionCount: Int = 0,
    val categorySummaries: List<CategorySummary> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val getMonthlyExpensesUseCase: GetMonthlyExpensesUseCase,
    private val getYearlyExpensesUseCase: GetYearlyExpensesUseCase,
    private val getCategorySummaryUseCase: GetCategorySummaryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AnalysisState())
    val state: StateFlow<AnalysisState> = _state.asStateFlow()

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
                    
                    state.copy(
                        totalSpent = total,
                        transactionCount = expenses.size,
                        categorySummaries = categories
                    )
                }
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun onEvent(event: AnalysisEvent) {
        when (event) {
            is AnalysisEvent.TogglePeriod -> {
                _state.value = _state.value.copy(period = event.period)
            }
            is AnalysisEvent.NextPeriod -> {
                if (_state.value.period == TimePeriod.MONTHLY) {
                    var month = _state.value.currentMonth + 1
                    var year = _state.value.currentYear
                    if (month > 12) { month = 1; year += 1 }
                    _state.value = _state.value.copy(currentMonth = month, currentYear = year)
                } else {
                    _state.value = _state.value.copy(currentYear = _state.value.currentYear + 1)
                }
            }
            is AnalysisEvent.PreviousPeriod -> {
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

sealed class AnalysisEvent {
    data class TogglePeriod(val period: TimePeriod) : AnalysisEvent()
    object NextPeriod : AnalysisEvent()
    object PreviousPeriod : AnalysisEvent()
}
