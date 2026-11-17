package com.spenzo.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// Brand
// ─────────────────────────────────────────────────────────────
/** Light-mode accent: deep emerald. 5.4:1 against white — safe for text and filled buttons. */
val Emerald700 = Color(0xFF06795A)
/** Dark-mode accent: mint. 8.9:1 as text, 8.9:1 against its own dark label. */
val Mint400 = Color(0xFF3DDC97)
val Mint300 = Color(0xFF6FE7B4)

val Rose600 = Color(0xFFDC2626)
val Rose400 = Color(0xFFFB7185)
val Amber600 = Color(0xFFD97706)
val Amber400 = Color(0xFFFBBF24)

// ─────────────────────────────────────────────────────────────
// Light palette
// ─────────────────────────────────────────────────────────────
val Background = Color(0xFFF4F6F9) // cool off-white, lets white cards float
val Surface = Color(0xFFFFFFFF) // cards
val SurfaceSunken = Color(0xFFEDF0F5) // tracks, inputs, segmented rails
val TextPrimary = Color(0xFF0D1117)
val TextSecondary = Color(0xFF5A6172) // 6.4:1 on white
val TextTertiary = Color(0xFF767D8D) // hints only, 4.6:1 on white
val AccentGreen = Color(0xFF06795A) // income / positive
val AccentOrange = Color(0xFFD97706) // warnings, secondary accent
val ExpenseRed = Color(0xFFDC2626)
val ProgressTrack = Color(0xFFE7EBF1)
val IconBackground = Color(0xFFF1F4F8)
val BorderColor = Color(0xFFE3E7EE)
val BorderStrong = Color(0xFFD2D8E2)

// ─────────────────────────────────────────────────────────────
// Dark palette — layered charcoal, never pure black.
// Each step up is a real elevation step so cards read as cards.
// ─────────────────────────────────────────────────────────────
val DarkBackground = Color(0xFF000000) // app canvas — true black
val DarkSurface = Color(0xFF0A0A0A) // cards — barely above black
val DarkCardElevated = Color(0xFF141414) // raised rows, thumbs
val DarkSurfaceSunken = Color(0xFF050505) // tracks, inputs
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFB0B0B0) // readable on near-black
val DarkTextTertiary = Color(0xFF888888)
val DarkProgressTrack = Color(0xFF1A1A1A)
val DarkIconBackground = Color(0xFF121212)
val DarkBorder = Color(0xFF222222) // hairline
val DarkBorderStrong = Color(0xFF2A2A2A)

// ─────────────────────────────────────────────────────────────
// Hero balance card (signature surface, same identity in both themes)
// ─────────────────────────────────────────────────────────────
val HeroTopLight = Color(0xFF1A2330)
val HeroBottomLight = Color(0xFF0E141B)
val HeroTopDark = Color(0xFF1C2634)
val HeroBottomDark = Color(0xFF10151D)
val HeroGlowLight = Color(0xFF2BBE8F)
val HeroGlowDark = Color(0xFF3DDC97)
val HeroBorderLight = Color(0xFF2C3542)
val HeroBorderDark = Color(0xFF333E4E)
