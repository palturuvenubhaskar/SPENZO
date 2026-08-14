package com.example.spenzo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.spenzo.data.local.entity.ExpenseEntity

@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract val expenseDao: ExpenseDao
}
