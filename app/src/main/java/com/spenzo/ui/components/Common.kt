package com.spenzo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spenzo.ui.theme.Radius
import com.spenzo.ui.theme.Space
import com.spenzo.ui.theme.SpTheme

/** The one card surface used across every screen. */
@Composable
fun SpCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Space.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = SpTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(c.card)
            .border(BorderStroke(1.dp, c.border), RoundedCornerShape(Radius.lg))
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = SpTheme.colors
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .clickable(onClick = onAction)
                    .padding(horizontal = Space.sm, vertical = Space.xs),
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = SpTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = Space.xxl, horizontal = Space.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(c.iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = c.textSecondary, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(Space.lg))
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        Spacer(Modifier.height(Space.xs))
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Space.lg))
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = c.income,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .clickable(onClick = onAction)
                    .padding(horizontal = Space.lg, vertical = Space.sm),
            )
        }
    }
}

@Composable
fun StatTile(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val c = SpTheme.colors
    SpCard(modifier, PaddingValues(Space.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(Space.sm))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(Space.sm))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = c.textPrimary,
            maxLines = 1,
        )
    }
}

/**
 * Text field colors for every form field in the app.
 *
 * The default M3 outline is `colorScheme.outline`, which is our card border —
 * nearly invisible on a dark background, so fields read as floating labels.
 */
@Composable
fun spFieldColors(): TextFieldColors {
    val c = SpTheme.colors
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = c.income,
        unfocusedBorderColor = c.textSecondary.copy(alpha = 0.45f),
        focusedTextColor = c.textPrimary,
        unfocusedTextColor = c.textPrimary,
        focusedLabelColor = c.income,
        unfocusedLabelColor = c.textSecondary,
        focusedPlaceholderColor = c.textSecondary,
        unfocusedPlaceholderColor = c.textSecondary,
        cursorColor = c.income,
    )
}

/** Two-or-more option pill switch (Expense/Income, Monthly/Yearly). */@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = SpTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.pill))
            .background(c.cardElevated)
            .border(BorderStroke(1.dp, c.border), RoundedCornerShape(Radius.pill))
            .padding(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(if (selected) c.primary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    color = if (selected) c.onPrimary else c.textSecondary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}
