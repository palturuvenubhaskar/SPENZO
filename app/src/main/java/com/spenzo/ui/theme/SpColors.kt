package com.spenzo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Single semantic color palette for the whole app.
 * Screens must read colors from here instead of re-deriving them from
 * `isDarkTheme` — that duplication is what caused the visual drift
 * between Home/Graphs/Analysis.
 */
@Immutable
data class SpColors(
    val isDark: Boolean,
    val background: Color,
    val card: Color,
    val cardElevated: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val income: Color,
    val expense: Color,
    val warn: Color,
    val primary: Color,
    val onPrimary: Color,
    val track: Color,
    val iconBg: Color,
)

val LightSpColors = SpColors(
    isDark = false,
    background = Background,
    card = Surface,
    cardElevated = Color(0xFFF1F3F7),
    border = BorderColor,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    income = AccentGreen,
    expense = ExpenseRed,
    warn = AccentOrange,
    primary = DarkCard,
    onPrimary = Surface,
    track = ProgressTrack,
    iconBg = IconBackground,
)

val DarkSpColors = SpColors(
    isDark = true,
    background = DarkBackground,
    card = DarkSurface,
    cardElevated = DarkCardElevated,
    border = DarkBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    income = AccentGreen,
    expense = ExpenseRed,
    warn = AccentOrange,
    primary = DarkTextPrimary,
    onPrimary = DarkBackground,
    track = DarkProgressTrack,
    iconBg = DarkIconBackground,
)

val LocalSpColors = staticCompositionLocalOf { LightSpColors }
val LocalIsDark = staticCompositionLocalOf { false }

object SpTheme {
    val colors: SpColors
        @Composable @ReadOnlyComposable get() = LocalSpColors.current
}

/** 4dp spacing scale — no more one-off 14.dp / 18.dp paddings. */
object Space {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
}

object Radius {
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 26.dp
    val pill = 100.dp
}
