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

    // Surfaces, back to front
    val background: Color,
    val card: Color,
    val cardElevated: Color,
    val cardSunken: Color,

    // Lines
    val border: Color,
    val borderStrong: Color,

    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,

    // Meaning
    val income: Color,
    val expense: Color,
    val warn: Color,

    // Action
    val primary: Color,
    val onPrimary: Color,
    val primarySoft: Color,

    // Support
    val track: Color,
    val iconBg: Color,
    val chartBar: Color,

    // Hero balance card
    val heroTop: Color,
    val heroBottom: Color,
    val heroGlow: Color,
    val heroBorder: Color,
) {
    /** White-on-hero body text. */
    val heroText: Color get() = Color(0xFFF7FAFC)
    val heroMuted: Color get() = Color(0xFFAEB9C9)
}

val LightSpColors = SpColors(
    isDark = false,
    background = Background,
    card = Surface,
    cardElevated = Surface,
    cardSunken = SurfaceSunken,
    border = BorderColor,
    borderStrong = BorderStrong,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textTertiary = TextTertiary,
    income = AccentGreen,
    expense = ExpenseRed,
    warn = AccentOrange,
    primary = Emerald700,
    onPrimary = Surface,
    primarySoft = Emerald700.copy(alpha = 0.10f),
    track = ProgressTrack,
    iconBg = IconBackground,
    chartBar = Color(0xFF1B2130),
    heroTop = HeroTopLight,
    heroBottom = HeroBottomLight,
    heroGlow = HeroGlowLight,
    heroBorder = HeroBorderLight,
)

val DarkSpColors = SpColors(
    isDark = true,
    background = DarkBackground,
    card = DarkSurface,
    cardElevated = DarkCardElevated,
    cardSunken = DarkSurfaceSunken,
    border = DarkBorder,
    borderStrong = DarkBorderStrong,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    income = Mint400,
    expense = Rose400,
    warn = Amber400,
    primary = Mint400,
    onPrimary = Color(0xFF06281B),
    primarySoft = Mint400.copy(alpha = 0.14f),
    track = DarkProgressTrack,
    iconBg = DarkIconBackground,
    chartBar = Color(0xFFD5DCE8),
    heroTop = HeroTopDark,
    heroBottom = HeroBottomDark,
    heroGlow = HeroGlowDark,
    heroBorder = HeroBorderDark,
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
    val xxxl = 32.dp
}

object Radius {
    val sm = 10.dp
    val md = 14.dp
    val lg = 20.dp
    val xl = 26.dp
    val pill = 100.dp
}

/** Screen gutter + bottom breathing room for every scrolling tab. */
object Insets {
    val gutter = Space.xl
    /** Tail padding inside scrolling tabs — enough room to scroll the last
     *  item clear of the floating bottom dock (dock ≈ 66dp + nav-bar inset). */
    val bottomContent = 108.dp
}
