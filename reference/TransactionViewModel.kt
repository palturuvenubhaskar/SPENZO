package com.spenzo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spenzo.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).dao()

    val transactions: Flow<List<TransactionEntity>> = dao.getAll()

    // ----- write operations -----
    fun add(t: Transaction) = viewModelScope.launch {
        dao.insert(t.toEntity())
    }
    fun update(t: Transaction) = viewModelScope.launch {
        dao.update(t.toEntity())
    }
    fun delete(t: TransactionEntity) = viewModelScope.launch {
        dao.delete(t)
    }

    suspend fun getById(id: Long): TransactionEntity? = dao.getById(id)

    // ----- queries for graphs / analysis -----
    private val fmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    val currentMonth = MutableStateFlow(fmt.format(Date()))      // "2026-10"
    val currentYear  = MutableStateFlow(SimpleDateFormat("yyyy", Locale.getDefault()).format(Date()))

    val dailyTotals: Flow<List<DayTotal>> = currentMonth.flatMapLatest { dao.dailyTotals(it) }
    val monthlyExpenseTotals: Flow<List<MonthTotal>> = currentYear.flatMapLatest { dao.monthlyTotals("EXPENSE", it) }
    val monthlyIncomeTotals: Flow<List<MonthTotal>>  = currentYear.flatMapLatest { dao.monthlyTotals("INCOME", it) }
    val categoryTotals: Flow<List<CategoryTotal>>    = currentMonth.flatMapLatest { dao.categoryTotals(it) }
    val monthIncomeExpense: Flow<IncomeExpense>      = currentMonth.flatMapLatest { dao.monthIncomeExpense(it) }

    // helpers
    fun toEntity(t: Transaction) = TransactionEntity(
        id = t.id, name = t.name, amount = t.amount, category = t.category,
        type = t.type.name, note = t.note, dayLabel = t.dayLabel, timestamp = t.timestamp
    )
    fun TransactionEntity.toTransaction() = Transaction(
        id = id, name = name, amount = amount,
        category = category,
        type = TxType.valueOf(type), note = note, dayLabel = dayLabel, timestamp = timestamp
    )
}
