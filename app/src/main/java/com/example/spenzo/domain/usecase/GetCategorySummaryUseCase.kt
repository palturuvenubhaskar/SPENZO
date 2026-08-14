package com.example.spenzo.domain.usecase

import com.example.spenzo.domain.model.Category
import com.example.spenzo.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class CategorySummary(
    val category: Category,
    val totalAmount: Double,
    val percentage: Float
)

class GetCategorySummaryUseCase @Inject constructor() {
    operator fun invoke(expensesFlow: Flow<List<Expense>>): Flow<List<CategorySummary>> {
        return expensesFlow.map { expenses ->
            val total = expenses.sumOf { it.amount }
            if (total == 0.0) return@map emptyList()

            val grouped = expenses.groupBy { it.category }
            grouped.map { (category, categoryExpenses) ->
                val categoryTotal = categoryExpenses.sumOf { it.amount }
                CategorySummary(
                    category = category,
                    totalAmount = categoryTotal,
                    percentage = (categoryTotal / total).toFloat()
                )
            }.sortedByDescending { it.totalAmount }
        }
    }
}
