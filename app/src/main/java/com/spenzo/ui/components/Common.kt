package com.spenzo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spenzo.ui.theme.LocalIsDark
import com.spenzo.ui.theme.Radius
import com.spenzo.ui.theme.Space
import com.spenzo.ui.theme.SpTheme

/**
 * The one card surface used across every screen.
 *
 * Elevation is honest: a real shadow in light mode, a lighter surface + hairline
 * border in dark mode (shadows are invisible on near-black).
 */
@Composable
fun SpCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Space.lg),
    elevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = SpTheme.colors
    val shape = RoundedCornerShape(Radius.lg)
    var m = modifier
        .fillMaxWidth()
        .then(
            if (elevated && !c.isDark) {
                Modifier.shadow(elevation = 10.dp, shape = shape, clip = false)
            } else {
                Modifier
            }
        )
        .clip(shape)
        .background(if (elevated) c.cardElevated else c.card)
        .border(BorderStroke(1.dp, c.border), shape)

    if (onClick != null) {
        m = m.clickable(onClick = onClick)
    }

    Column(m.padding(contentPadding), content = content)
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
        modifier
            .fillMaxWidth()
            .padding(top = Space.xs, bottom = Space.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = c.textPrimary,
        )
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = c.primary,
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
            .padding(vertical = Space.xxxl, horizontal = Space.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(c.primarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = c.primary, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(Space.lg))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Space.xs))
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Space.lg))
            Box(
                Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(c.primary)
                    .clickable(onClick = onAction)
                    .padding(horizontal = Space.xl, vertical = Space.sm),
            ) {
                Text(
                    actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = c.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** One metric inside a [StatRow]. */
data class Stat(val label: String, val value: String, val color: Color)

/**
 * Metrics grouped in a single card with hairline separators — replaces the
 * three floating mini-cards that made Home look like a spreadsheet.
 */
@Composable
fun StatRow(
    stats: List<Stat>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = Space.lg, horizontal = Space.sm),
) {
    val c = SpTheme.colors
    SpCard(modifier, contentPadding) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            stats.forEachIndexed { index, stat ->
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stat.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textSecondary,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(Space.xs))
                    Text(
                        stat.value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = stat.color,
                        maxLines = 1,
                    )
                }
                if (index < stats.lastIndex) {
                    VerticalDivider(
                        Modifier.height(30.dp),
                        thickness = 1.dp,
                        color = c.border,
                    )
                }
            }
        }
    }
}

/** Compact card metric, kept for screens that need a standalone tile. */
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
            Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
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

/** Rounded icon container used for categories, settings rows and actions. */
@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    container: Color = tint.copy(alpha = if (LocalIsDark.current) 0.22f else 0.12f),
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Thin, rounded progress bar with a track. */
@Composable
fun SpProgress(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    track: Color = SpTheme.colors.track,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(Radius.pill))
            .background(track),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(Radius.pill))
                .background(color),
        )
    }
}

/**
 * Text field colors for every form field in the app.
 *
 * Filled-looking containers (sunken surface) read as inputs in both themes;
 * the old near-invisible `outline` border made fields look like floating labels.
 */
@Composable
fun spFieldColors(): TextFieldColors {
    val c = SpTheme.colors
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = c.primary,
        unfocusedBorderColor = c.border,
        focusedContainerColor = c.cardSunken,
        unfocusedContainerColor = c.cardSunken,
        focusedTextColor = c.textPrimary,
        unfocusedTextColor = c.textPrimary,
        focusedLabelColor = c.primary,
        unfocusedLabelColor = c.textSecondary,
        focusedPlaceholderColor = c.textTertiary,
        unfocusedPlaceholderColor = c.textTertiary,
        cursorColor = c.primary,
    )
}

/**
 * Two-or-more option pill switch (Expense/Income, Monthly/Yearly).
 *
 * The selected thumb is a raised *surface*, not the text color: a white pill in
 * dark mode was the single most jarring thing on screen.
 */
@Composable
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
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(Radius.pill))
            .background(c.cardSunken)
            .border(BorderStroke(1.dp, c.border), RoundedCornerShape(Radius.pill))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(
                targetValue = if (selected) c.cardElevated else Color.Transparent,
                animationSpec = tween(180),
                label = "segBg$index",
            )
            val fg by animateColorAsState(
                targetValue = if (selected) c.textPrimary else c.textSecondary,
                animationSpec = tween(180),
                label = "segFg$index",
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(34.dp)
                    .then(
                        if (selected && !c.isDark) {
                            Modifier.shadow(3.dp, RoundedCornerShape(Radius.pill), clip = false)
                        } else {
                            Modifier
                        }
                    )
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(bg)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    color = fg,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

/** Centered period switcher used by Graphs, Analysis and Home. */
@Composable
fun MonthStepper(
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onLabelClick: (() -> Unit)? = null,
) {
    val c = SpTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.pill))
            .background(c.card)
            .border(BorderStroke(1.dp, c.border), RoundedCornerShape(Radius.pill))
            .padding(horizontal = Space.xs, vertical = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                "Previous period",
                tint = c.textPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(
            Modifier
                .weight(1f)
                .then(
                    if (onLabelClick != null) {
                        Modifier
                            .clip(RoundedCornerShape(Radius.pill))
                            .clickable(onClick = onLabelClick)
                    } else {
                        Modifier
                    }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
            }
        }
        IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "Next period",
                tint = c.textPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** Tiny rounded label used for deltas and counts. */
@Composable
fun SpPill(
    text: String,
    color: Color,
    background: Color = color.copy(alpha = if (SpTheme.colors.isDark) 0.16f else 0.10f),
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(background)
            .padding(horizontal = Space.sm, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(3.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}
