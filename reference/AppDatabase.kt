package com.spenzo.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transactions", indices = [Index("dayLabel")])
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: String,
    val type: String,          // TxType.name
    val note: String,
    val dayLabel: String,
    val timestamp: Long
)

data class DayTotal(val day: String, val total: Double)
data class MonthTotal(val month: String, val total: Double)
data class CategoryTotal(val category: String, val total: Double)
data class IncomeExpense(val income: Double, val expense: Double)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: TransactionEntity): Long

    @Update
    suspend fun update(t: TransactionEntity)

    @Delete
    suspend fun delete(t: TransactionEntity)

    // per-day totals for a month, e.g. ym = "2026-09"
    @Query("SELECT dayLabel AS day, SUM(amount) AS total FROM transactions " +
           "WHERE type = 'EXPENSE' AND dayLabel LIKE :ym || '%' GROUP BY dayLabel ORDER BY day")
    fun dailyTotals(ym: String): Flow<List<DayTotal>>

    // per-month totals for a year, e.g. year = "2026"
    @Query("SELECT substr(dayLabel,1,7) AS month, SUM(amount) AS total FROM transactions " +
           "WHERE type = :type AND dayLabel LIKE :year || '%' GROUP BY month ORDER BY month")
    fun monthlyTotals(type: String, year: String): Flow<List<MonthTotal>>

    // category breakdown for a month (or year with :ym = "2026")
    @Query("SELECT category, SUM(amount) AS total FROM transactions " +
           "WHERE type = 'EXPENSE' AND dayLabel LIKE :prefix || '%' GROUP BY category")
    fun categoryTotals(prefix: String): Flow<List<CategoryTotal>>

    @Query("SELECT COALESCE(SUM(CASE WHEN type='INCOME' THEN amount ELSE 0 END),0) AS income, " +
           "COALESCE(SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END),0) AS expense " +
           "FROM transactions WHERE dayLabel LIKE :ym || '%'")
    fun monthIncomeExpense(ym: String): Flow<IncomeExpense>
}

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "spenzo.db").build()
                    .also { INSTANCE = it }
            }
    }
}
