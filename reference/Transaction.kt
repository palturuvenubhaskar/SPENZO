package com.spenzo.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.spenzo.ui.theme.AccentGreen
import com.spenzo.ui.theme.AccentOrange
import com.spenzo.ui.theme.ExpenseRed

enum class TxType { INCOME, EXPENSE }

// Icon-based categories — no emojis, matching the reference style
enum class Category(val label: String, val icon: ImageVector, val color: Color) {
    FOOD("Food",          Icons.Outlined.Restaurant,    AccentOrange),
    TRANSPORT("Transport",Icons.Outlined.DirectionsCar, Color(0xFF3E7BFA)),
    ENTERTAINMENT("Fun",  Icons.Outlined.SportsEsports, Color(0xFF9B5DE5)),
    SHOPPING("Shopping",  Icons.Outlined.ShoppingBag,   Color(0xFFF15BB5)),
    EDUCATION("Education",Icons.Outlined.School,        Color(0xFF00B4D8)),
    HEALTH("Health",      Icons.Outlined.Favorite,      Color(0xFFEF476F)),
    BILLS("Bills",        Icons.Outlined.Receipt,       Color(0xFF6C757D)),
    OTHER("Other",        Icons.Outlined.Category,      Color(0xFF8A8D98)),
    ALLOWANCE("Allowance",Icons.Outlined.Payments,      AccentGreen),
    SALARY("Salary",      Icons.Outlined.Work,          AccentGreen),
    GIFT("Gift",          Icons.Outlined.CardGiftcard,  AccentGreen),
    OTHER_INCOME("Other", Icons.Outlined.Savings,       AccentGreen)
}

val expenseCategories = Category.entries.filter {
    it in listOf(Category.FOOD, Category.TRANSPORT, Category.ENTERTAINMENT,
        Category.SHOPPING, Category.EDUCATION, Category.HEALTH, Category.BILLS, Category.OTHER)
}
val incomeCategories = Category.entries.filter {
    it in listOf(Category.ALLOWANCE, Category.SALARY, Category.GIFT, Category.OTHER_INCOME)
}

data class Transaction(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: String,      // Category.name
    val type: TxType,
    val note: String = "",
    val dayLabel: String,      // "yyyy-MM-dd" — enables fast monthly queries
    val timestamp: Long
)
