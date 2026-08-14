package com.example.spenzo.domain.repository

import com.example.spenzo.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByMonth(year: Int, month: Int): Flow<List<Expense>>
    fun getExpensesByYear(year: Int): Flow<List<Expense>>
    suspend fun addExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
}
