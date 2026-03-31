package com.application.jomato.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.application.jomato.Prefs

object JomatoTheme {

    val isDark @Composable get(): Boolean {
        val mode by Prefs.themeMode.collectAsState()
        return when (mode) {
            "dark" -> true
            "light" -> false
            else -> isSystemInDarkTheme()
        }
    }

    // ── Brand ────────────────────────────────────────────────────────────────
    val Brand @Composable get() = if (isDark) Color(0xFFD18BAA) else Color(0xFF6F304F)
    val BrandLight @Composable get() = if (isDark) Color(0xFF311B27) else Color(0xFFF3E5F5)
    val BrandBlack @Composable get() = if (isDark) Color(0xFFF0F0F0) else Color(0xFF1C1C1C)

    // ── Surfaces ─────────────────────────────────────────────────────────────
    val Background @Composable get() = if (isDark) Color(0xFF0E0E0E) else Color(0xFFF9F9F9)
    val CardBg @Composable get() = if (isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val SecondaryBg @Composable get() = if (isDark) Color(0xFF242424) else Color(0xFFF4F4F4)

    // ── Text & Dividers ──────────────────────────────────────────────────────
    val TextGray @Composable get() = if (isDark) Color(0xFFAAAAAA) else Color(0xFF696969)
    val TextMuted @Composable get() = if (isDark) Color(0xFF666666) else Color(0xFFB0B0B0)
    val Divider @Composable get() = if (isDark) Color(0xFF2A2A2A) else Color(0xFFE8E8E8)

    // ── State Colors ─────────────────────────────────────────────────────────
    val Success @Composable get() = if (isDark) Color(0xFF81C784) else Color(0xFF388E3C)
    val Warning @Composable get() = if (isDark) Color(0xFFFFAB40) else Color(0xFFE65100)
    val Error @Composable get() = if (isDark) Color(0xFFFF5252) else Color(0xFFB00020)

    // ── Accent (for active/live states) ──────────────────────────────────────
    val ActiveGreen @Composable get() = if (isDark) Color(0xFF4ADE80) else Color(0xFF22C55E)

    // ── Gradients ────────────────────────────────────────────────────────────

    /** Subtle brand gradient for hero cards and premium surfaces */
    val BrandGradient @Composable get() = Brush.linearGradient(
        colors = if (isDark) {
            listOf(Color(0xFF2D1525), Color(0xFF1A1A1A))
        } else {
            listOf(Color(0xFFF8EDF3), Color(0xFFFFFFFF))
        }
    )

    /** Warm accent gradient for feature card icons */
    val AccentGradient @Composable get() = Brush.linearGradient(
        colors = if (isDark) {
            listOf(Color(0xFF3D1F30), Color(0xFF2A1520))
        } else {
            listOf(Color(0xFFF3E0EC), Color(0xFFE8D0DE))
        }
    )

    /** Glass-like border for elevated cards */
    val GlassBorder @Composable get() = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)
}
