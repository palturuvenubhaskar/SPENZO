package com.example.spenzo.domain.model

import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val category: Category,
    val date: Long, // epoch millis
    val createdAt: Long = System.currentTimeMillis()
)
