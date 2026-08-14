package com.example.spenzo.domain.usecase

import com.example.spenzo.domain.model.Expense
import com.example.spenzo.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetYearlyExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(year: Int): Flow<List<Expense>> {
        return repository.getExpensesByYear(year)
    }
}
