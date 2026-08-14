package com.example.spenzo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.spenzo.domain.model.Category
import com.example.spenzo.presentation.theme.*

fun getCategoryIcon(category: Category): ImageVector {
    return when(category) {
        Category.FOOD -> Icons.Outlined.Restaurant
        Category.TRANSPORT -> Icons.Outlined.DirectionsCar
        Category.SHOPPING -> Icons.Outlined.ShoppingBag
        Category.BILLS -> Icons.Outlined.Receipt
        Category.ENTERTAINMENT -> Icons.Outlined.Theaters
        Category.HEALTH -> Icons.Outlined.FavoriteBorder
        Category.EDUCATION -> Icons.Outlined.School
        Category.OTHER -> Icons.Outlined.Archive
    }
}

fun getCategoryColor(category: Category): Color {
    return Color(android.graphics.Color.parseColor(category.colorHex))
}

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) CardDark else Color.Transparent
    val contentColor = if (isSelected) TextOnDark else TextPrimary
    val borderColor = if (isSelected) Color.Transparent else Color(0x14000000)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = getCategoryIcon(category),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = contentColor
        )
        Text(
            text = category.displayName,
            style = Typography.labelMedium,
            color = contentColor
        )
    }
}
