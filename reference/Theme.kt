package com.spenzo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val SpenzoColors = lightColorScheme(
    background   = Background,
    surface      = Surface,
    primary      = DarkCard,
    onPrimary    = Surface,
    secondary    = AccentGreen,
    onBackground = TextPrimary,
    onSurface    = TextPrimary,
    surfaceVariant = IconBackground,
    onSurfaceVariant = TextSecondary
)

private val SpenzoTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyMedium     = TextStyle(fontSize = 14.sp),
    labelSmall     = TextStyle(fontSize = 12.sp, color = TextSecondary)
)

@Composable
fun SpenzoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpenzoColors,
        typography  = SpenzoTypography,
        content     = content
    )
}
