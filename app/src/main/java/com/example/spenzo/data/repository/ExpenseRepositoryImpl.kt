package com.example.spenzo.data.repository

import com.example.spenzo.data.local.ExpenseDao
import com.example.spenzo.data.mapper.toDomainModel
import com.example.spenzo.data.mapper.toEntity
import com.example.spenzo.domain.model.Expense
import com.example.spenzo.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> {
        return dao.getAllExpenses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getExpensesByMonth(year: Int, month: Int): Flow<List<Expense>> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atTime(23, 59, 59, 999999999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return dao.getExpensesByDateRange(startMillis, endMillis).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getExpensesByYear(year: Int): Flow<List<Expense>> {
        val startDate = LocalDate.of(year, 1, 1)
        val endDate = LocalDate.of(year, 12, 31)
        
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atTime(23, 59, 59, 999999999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return dao.getExpensesByDateRange(startMillis, endMillis).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun addExpense(expense: Expense) {
        dao.insertExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense) {
        dao.deleteExpense(expense.toEntity())
    }
}
