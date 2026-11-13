package com.spenzo.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Light palette ───
val Background         = Color(0xFFF7F8FA)  // screen background, clean off-white
val Surface            = Color(0xFFFFFFFF)  // cards
val DarkCard           = Color(0xFF13141B)  // balance card / primary dark
val TextPrimary        = Color(0xFF111218)
// 4.9:1 on white — was #7E8292 (3.8:1) which failed WCAG AA for small text
val TextSecondary      = Color(0xFF6B7180)
val AccentGreen        = Color(0xFF10B981)  // vibrant emerald income, progress
val AccentOrange       = Color(0xFFF59E0B)  // second accent (goals/warnings)
val ExpenseRed         = Color(0xFFEF4444)  // expenses
val ProgressTrack      = Color(0xFFEBECEF)  // progress bar background
val IconBackground     = Color(0xFFF2F3F6)  // rounded icon boxes
val BorderColor        = Color(0xFFE5E7EB)  // subtle card border

// ─── Dark palette (OLED Midnight Obsidian) ───
val DarkBackground     = Color(0xFF090A0F)  // Deepest obsidian black (no muddy wash)
val DarkSurface        = Color(0xFF141620)  // Elevated crisp dark surface
val DarkCardElevated   = Color(0xFF1C1F2E)  // Higher elevation card
val DarkDarkCard       = Color(0xFF181B26)  // Rich deep balance card
val DarkTextPrimary    = Color(0xFFF8F9FC)  // Pristine white text, high readability
val DarkTextSecondary  = Color(0xFF9CA2B4)  // Clear, legible silver-slate (never muddy)
val DarkProgressTrack  = Color(0xFF202330)
val DarkIconBackground = Color(0xFF1C1E2A)
val DarkBorder         = Color(0xFF26293A)  // Hairline precision border
