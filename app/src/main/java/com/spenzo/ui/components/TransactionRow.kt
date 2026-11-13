package com.spenzo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spenzo.data.Category
import com.spenzo.data.Dates
import com.spenzo.data.Money
import com.spenzo.data.TransactionEntity
import com.spenzo.data.TxType
import com.spenzo.ui.theme.LocalCurrencySymbol
import com.spenzo.ui.theme.Radius
import com.spenzo.ui.theme.Space
import com.spenzo.ui.theme.SpTheme

/**
 * Shared transaction row.
 *
 * Tapping the row edits it (the destination is now visible), and the overflow
 * button opens Edit/Delete — previously the whole row silently opened a menu
 * with no affordance at all.
 */
@Composable
fun TransactionRow(
    tx: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = SpTheme.colors
    val symbol = LocalCurrencySymbol.current
    val category = Category.fromName(tx.category)
    val isExpense = tx.type == TxType.EXPENSE.name
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(c.card)
            .border(BorderStroke(1.dp, c.border), RoundedCornerShape(Radius.md))
            .clickable(onClick = onEdit)
            .padding(start = Space.md, top = Space.sm, bottom = Space.sm, end = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape)
                .background(category.color.copy(alpha = if (c.isDark) 0.18f else 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(category.icon, category.label, tint = category.color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(Space.md))
        Column(Modifier.weight(1f)) {
            Text(
                tx.name,
                style = MaterialTheme.typography.titleSmall,
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                category.label + " · " + Dates.prettyDay(tx.timestamp),
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(Space.sm))
        Text(
            Money.signed(tx.amount, symbol, isExpense),
            color = if (isExpense) c.expense else c.income,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleSmall,
        )
        Box {
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.MoreVert, "More options", tint = c.textSecondary, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                    onClick = { menuOpen = false; onEdit() },
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = c.expense) },
                    leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = c.expense) },
                    onClick = { menuOpen = false; onDelete() },
                )
            }
        }
    }
}
