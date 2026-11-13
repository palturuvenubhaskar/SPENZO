package com.spenzo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// CompositionLocal to provide currency symbol throughout the tree
val LocalCurrencySymbol = compositionLocalOf { "$" }

private val LightColors = lightColorScheme(
    background   = Background,
    surface      = Surface,
    primary      = DarkCard,
    onPrimary    = Surface,
    secondary    = AccentGreen,
    onBackground = TextPrimary,
    onSurface    = TextPrimary,
    surfaceVariant = IconBackground,
    onSurfaceVariant = TextSecondary,
    outline      = BorderColor,
    error        = ExpenseRed,
)

private val DarkColors = darkColorScheme(
    background   = DarkBackground,
    surface      = DarkSurface,
    primary      = DarkTextPrimary,
    onPrimary    = DarkBackground,
    secondary    = AccentGreen,
    onBackground = DarkTextPrimary,
    onSurface    = DarkTextPrimary,
    surfaceVariant = DarkIconBackground,
    onSurfaceVariant = DarkTextSecondary,
    outline      = DarkBorder,
    error        = ExpenseRed,
)

private val SpenzoTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodyMedium     = TextStyle(fontSize = 14.sp),
    bodySmall      = TextStyle(fontSize = 13.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall     = TextStyle(fontSize = 12.sp),
)

@Composable
fun SpenzoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(
        LocalSpColors provides if (darkTheme) DarkSpColors else LightSpColors,
        LocalIsDark provides darkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = SpenzoTypography,
            content     = content
        )
    }
}

/** Kept for compatibility; prefer `SpTheme.colors` in new code. */
val isDarkTheme: Boolean
    @Composable get() = LocalIsDark.current
