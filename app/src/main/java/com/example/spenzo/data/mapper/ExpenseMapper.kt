package com.example.spenzo.data.mapper

import com.example.spenzo.data.local.entity.ExpenseEntity
import com.example.spenzo.domain.model.Category
import com.example.spenzo.domain.model.Expense

fun ExpenseEntity.toDomainModel(): Expense {
    return Expense(
        id = id,
        name = name,
        amount = amount,
        category = try { Category.valueOf(category) } catch (e: Exception) { Category.OTHER },
        date = date,
        createdAt = createdAt
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        name = name,
        amount = amount,
        category = category.name,
        date = date,
        createdAt = createdAt
    )
}
