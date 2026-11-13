package com.spenzo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightSpenzoColors = lightColorScheme(
    background   = LightBackground,
    surface      = LightSurface,
    primary      = LightDarkCard,
    onPrimary    = LightSurface,
    secondary    = LightAccentGreen,
    onBackground = LightTextPrimary,
    onSurface    = LightTextPrimary,
    surfaceVariant = LightIconBackground,
    onSurfaceVariant = LightTextSecondary,
    error        = LightExpenseRed,
    onError      = LightSurface,
)

private val DarkSpenzoColors = darkColorScheme(
    background   = DarkBackground,
    surface      = DarkSurface,
    primary      = DarkDarkCard,
    onPrimary    = DarkBackground,
    secondary    = DarkAccentGreen,
    onBackground = DarkTextPrimary,
    onSurface    = DarkTextPrimary,
    surfaceVariant = DarkIconBackground,
    onSurfaceVariant = DarkTextSecondary,
    error        = DarkExpenseRed,
    onError      = DarkBackground,
)

private val SpenzoTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyMedium     = TextStyle(fontSize = 14.sp),
    labelSmall     = TextStyle(fontSize = 12.sp, color = TextSecondary)
)

var isDarkTheme by mutableStateOf(false)
    private set

@Composable
fun SpenzoTheme(
    darkTheme: Boolean = isDarkTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkSpenzoColors else LightSpenzoColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = SpenzoTypography,
        content     = content
    )
}

@Composable
fun ToggleTheme() {
    isDarkTheme = !isDarkTheme
}

@Composable
fun CurrentThemeIsDark(): Boolean = isDarkTheme