package com.spenzo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spenzo.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * One instance is created in MainActivity and shared by every screen, so the
 * month selected in Graphs is the same month shown in Analysis and Home.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).dao()

    val transactions: Flow<List<TransactionEntity>> = dao.getAll()

    // ----- write operations -----
    fun add(t: Transaction) {
        viewModelScope.launch { dao.insert(t.toEntity()) }
    }

    fun update(t: Transaction) {
        viewModelScope.launch { dao.update(t.toEntity()) }
    }

    fun delete(t: TransactionEntity) {
        viewModelScope.launch { dao.delete(t) }
    }

    suspend fun getById(id: Long): TransactionEntity? = dao.getById(id)

    suspend fun snapshot(): List<TransactionEntity> = transactions.first()

    // ----- period selection -----
    val currentMonth = MutableStateFlow(Dates.currentMonthKey())
    val currentYear = MutableStateFlow(Dates.currentYearKey())

    fun setMonth(ym: String) {
        currentMonth.value = ym
        currentYear.value = Dates.yearOf(ym).toString()
    }

    fun shiftMonth(delta: Long) = setMonth(Dates.shiftMonth(currentMonth.value, delta))

    // ----- queries for graphs / analysis -----
    val dailyTotals: Flow<List<DayTotal>> = currentMonth.flatMapLatest { dao.dailyTotals(it) }
    val monthlyExpenseTotals: Flow<List<MonthTotal>> = currentYear.flatMapLatest { dao.monthlyTotals("EXPENSE", it) }
    val monthlyIncomeTotals: Flow<List<MonthTotal>>  = currentYear.flatMapLatest { dao.monthlyTotals("INCOME", it) }
    val categoryTotals: Flow<List<CategoryTotal>>    = currentMonth.flatMapLatest { dao.categoryTotals(it) }
    val monthIncomeExpense: Flow<IncomeExpense>      = currentMonth.flatMapLatest { dao.monthIncomeExpense(it) }

    val previousMonthIncomeExpense: Flow<IncomeExpense> =
        currentMonth.flatMapLatest { dao.monthIncomeExpense(Dates.shiftMonth(it, -1)) }

    /**
     * Home is a "right now" dashboard: it has no month switcher, so it must not
     * follow the month the user paged to on Graphs/Analysis. Otherwise paging
     * back to October there silently made Home's balance card show October
     * forever, with no way to get back to the current month from Home.
     */
    val thisMonthIncomeExpense: Flow<IncomeExpense> =
        dao.monthIncomeExpense(Dates.currentMonthKey())

    val lastMonthIncomeExpense: Flow<IncomeExpense> =
        dao.monthIncomeExpense(Dates.shiftMonth(Dates.currentMonthKey(), -1))

    // helpers
    fun TransactionEntity.toTransaction() = Transaction(
        id = id, name = name, amount = amount,
        category = category,
        type = TxType.fromName(type), note = note, dayLabel = dayLabel, timestamp = timestamp
    )
}
