package com.spenzo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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

/** Hairline between rows inside a grouped card. */
@Composable
fun RowDivider(startIndent: Int = 0) {
    val c = SpTheme.colors
    HorizontalDivider(
        modifier = Modifier.padding(start = startIndent.dp),
        thickness = 1.dp,
        color = c.border.copy(alpha = 0.7f),
    )
}

/**
 * Shared transaction row.
 *
 * Borderless by design: rows live inside one grouped card, so eight outlined
 * cards in a row no longer fight each other. Tapping edits; the overflow menu
 * deletes.
 */
@Composable
fun TransactionRow(
    tx: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = true,
) {
    val c = SpTheme.colors
    val symbol = LocalCurrencySymbol.current
    val category = Category.fromName(tx.category)
    val isExpense = tx.type == TxType.EXPENSE.name
    var menuOpen by remember { mutableStateOf(false) }
    val subtitle = buildString {
        append(if (tx.note.isNotBlank()) tx.note else category.label)
        if (showDate) append(" · " + Dates.prettyDay(tx.timestamp))
    }

    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .clickable { onEdit() }
            .padding(start = Space.md, top = Space.sm, bottom = Space.sm, end = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(
            icon = category.icon,
            tint = category.color,
            size = 40.dp,
            iconSize = 19.dp,
        )
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
                subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(Space.sm))
        Text(
            Money.signed(tx.amount, symbol, isExpense),
            color = if (isExpense) c.expense else c.income,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
        )
        Box {
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Rounded.MoreHoriz,
                    "More options for ${tx.name}",
                    tint = c.textTertiary,
                    modifier = Modifier.size(19.dp),
                )
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
