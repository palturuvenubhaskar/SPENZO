package com.example.spenzo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val category: String, // Enum name
    val date: Long, // epoch millis
    val createdAt: Long = System.currentTimeMillis()
)
