package com.example.spenzo.domain.usecase

import com.example.spenzo.domain.model.Expense
import com.example.spenzo.domain.repository.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense) {
        repository.addExpense(expense)
    }
}
