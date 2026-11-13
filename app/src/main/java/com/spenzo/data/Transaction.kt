package com.spenzo.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.spenzo.ui.theme.AccentGreen
import com.spenzo.ui.theme.AccentOrange
import com.spenzo.ui.theme.ExpenseRed

enum class TxType {
    INCOME, EXPENSE;

    companion object {
        /** Tolerates unknown/corrupt values instead of throwing from the UI. */
        fun fromName(name: String): TxType = entries.firstOrNull { it.name == name } ?: EXPENSE
    }
}

// Icon-based categories — sleek rounded icons matching modern design standards
enum class Category(val label: String, val icon: ImageVector, val color: Color) {
    FOOD("Food",          Icons.Rounded.Restaurant,    AccentOrange),
    TRANSPORT("Transport",Icons.Rounded.DirectionsCar, Color(0xFF3E7BFA)),
    ENTERTAINMENT("Fun",  Icons.Rounded.SportsEsports, Color(0xFF9B5DE5)),
    SHOPPING("Shopping",  Icons.Rounded.ShoppingBag,   Color(0xFFF15BB5)),
    EDUCATION("Education",Icons.Rounded.School,        Color(0xFF00B4D8)),
    HEALTH("Health",      Icons.Rounded.Favorite,      Color(0xFFEF476F)),
    BILLS("Bills",        Icons.Rounded.Receipt,       Color(0xFF6C757D)),
    OTHER("Other",        Icons.Rounded.Category,      Color(0xFF8A8D98)),
    ALLOWANCE("Allowance",Icons.Rounded.Payments,      AccentGreen),
    SALARY("Salary",      Icons.Rounded.Work,          AccentGreen),
    GIFT("Gift",          Icons.Rounded.CardGiftcard,  AccentGreen),
    OTHER_INCOME("Other income", Icons.Rounded.Savings, AccentGreen);

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
