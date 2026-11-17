package com.spenzo.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TxType {
    INCOME, EXPENSE;

    companion object {
        /** Tolerates unknown/corrupt values instead of throwing from the UI. */
        fun fromName(name: String): TxType = entries.firstOrNull { it.name == name } ?: EXPENSE
    }
}

// Icon-based categories — one hue per category, tuned to read on both themes.
// Mid-tone (500) colors stay legible as icon tints on white and on charcoal.
enum class Category(val label: String, val icon: ImageVector, val color: Color) {
    FOOD("Food",          Icons.Rounded.Restaurant,    Color(0xFFF59E0B)),
    TRANSPORT("Transport",Icons.Rounded.DirectionsCar, Color(0xFF3B82F6)),
    ENTERTAINMENT("Fun",  Icons.Rounded.SportsEsports, Color(0xFF8B5CF6)),
    SHOPPING("Shopping",  Icons.Rounded.ShoppingBag,   Color(0xFFEC4899)),
    EDUCATION("Education",Icons.Rounded.School,        Color(0xFF06B6D4)),
    HEALTH("Health",      Icons.Rounded.Favorite,      Color(0xFFF43F5E)),
    BILLS("Bills",        Icons.Rounded.Receipt,       Color(0xFF64748B)),
    OTHER("Other",        Icons.Rounded.Category,      Color(0xFF94A3B8)),
    ALLOWANCE("Allowance",Icons.Rounded.Payments,      Color(0xFF10B981)),
    SALARY("Salary",      Icons.Rounded.Work,          Color(0xFF10B981)),
    GIFT("Gift",          Icons.Rounded.CardGiftcard,  Color(0xFF10B981)),
    OTHER_INCOME("Other income", Icons.Rounded.Savings, Color(0xFF10B981));

    companion object {
        fun fromName(name: String): Category = entries.firstOrNull { it.name == name } ?: OTHER
    }
}

/** Explicit order — the old `entries.filter { it in listOf(...) }` was O(n²) and fragile. */
val expenseCategories = listOf(
    Category.FOOD, Category.TRANSPORT, Category.ENTERTAINMENT, Category.SHOPPING,
    Category.EDUCATION, Category.HEALTH, Category.BILLS, Category.OTHER
)

val incomeCategories = listOf(
    Category.ALLOWANCE, Category.SALARY, Category.GIFT, Category.OTHER_INCOME
)

data class Transaction(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: String,      // Category.name
    val type: TxType,
    val note: String = "",
    val dayLabel: String,      // "yyyy-MM-dd" — enables fast monthly queries
    val timestamp: Long
) {
    fun toEntity() = TransactionEntity(
        id = id, name = name, amount = amount, category = category,
        type = type.name, note = note, dayLabel = dayLabel, timestamp = timestamp
    )
}
