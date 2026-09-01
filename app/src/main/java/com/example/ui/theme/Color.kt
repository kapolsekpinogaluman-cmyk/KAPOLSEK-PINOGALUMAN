package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Modern Executive Gold & Obsidian Palette
val ModernDarkCanvas = Color(0xFF0F172A)
val ModernDarkCard = Color(0xFF1E293B)
val ModernDarkCardElevated = Color(0xFF334155)

val ModernLightCanvas = Color(0xFFF8FAFC)
val ModernLightCard = Color(0xFFFFFFFF)
val ModernLightCardElevated = Color(0xFFF1F5F9)
val ModernBorderLight = Color(0xFFE2E8F0)
val ModernBorderSubtle = Color(0xFFCBD5E1)

// Radiant Gold Spectrum
val GoldPrimary = Color(0xFFD97706) // Rich Amber Gold
val GoldAccent = Color(0xFFF59E0B) // Bright Gold Glow
val GoldLight = Color(0xFFFEF3C7) // Champagne Gold Light
val GoldContainerLight = Color(0xFFFFFBEB)
val GoldContainerDark = Color(0xFF451A03)
val OnGoldContainerLight = Color(0xFF78350F)
val OnGoldContainerDark = Color(0xFFFDE68A)

// Modern Hero Gradients
val ModernHeroGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF0F172A),
        Color(0xFF1E1B4B),
        Color(0xFF2E1065)
    )
)

val ModernGoldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF59E0B),
        Color(0xFFD97706),
        Color(0xFFB45309)
    )
)

val ModernEmeraldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF10B981),
        Color(0xFF059669)
    )
)

val ModernRoseGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF43F5E),
        Color(0xFFBE123C)
    )
)

// Status & Accents
val SuccessGreen = Color(0xFF10B981)
val SuccessGreenLight = Color(0xFFD1FAE5)
val SuccessGreenDark = Color(0xFF065F46)

val DangerRed = Color(0xFFEF4444)
val DangerRedLight = Color(0xFFFEE2E2)
val DangerRedDark = Color(0xFF991B1B)

val WarningOrange = Color(0xFFF59E0B)
val WarningOrangeLight = Color(0xFFFEF3C7)

val InfoBlue = Color(0xFF3B82F6)
val InfoBlueLight = Color(0xFFDBEAFE)

val PurpleModern = Color(0xFF8B5CF6)
val PurpleModernLight = Color(0xFFEDE9FE)

// Immersive Tokens (Backward compatibility & M3)
val ImmersivePrimary = Color(0xFFD97706)
val ImmersivePrimaryContainer = Color(0xFFFEF3C7)
val ImmersiveOnPrimaryContainer = Color(0xFF78350F)
val ImmersiveHeroRose = Color(0xFFFFE4E6)
val ImmersiveBadgeBlue = Color(0xFFDBEAFE)
val ImmersiveBadgeBlueText = Color(0xFF1E40AF)
val ImmersiveBadgePink = Color(0xFFFCE7F3)
val ImmersiveBadgePinkText = Color(0xFF9D174D)
val ImmersiveBadgeGreen = Color(0xFFD1FAE5)
val ImmersiveBadgeGreenText = Color(0xFF065F46)
val ImmersiveBadgeYellow = Color(0xFFFEF3C7)
val ImmersiveBadgeYellowText = Color(0xFF92400E)

val SlateNavyDark = Color(0xFF0F172A)
val SlateNavyMedium = Color(0xFF1E293B)
val SlateNavyLight = Color(0xFF334155)

// Material 3 Light Theme Colors
val md_theme_light_primary = Color(0xFFD97706)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFFEF3C7)
val md_theme_light_onPrimaryContainer = Color(0xFF78350F)
val md_theme_light_secondary = Color(0xFF475569)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFF1F5F9)
val md_theme_light_onSecondaryContainer = Color(0xFF1E293B)
val md_theme_light_tertiary = Color(0xFF8B5CF6)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFEDE9FE)
val md_theme_light_onTertiaryContainer = Color(0xFF4C1D95)
val md_theme_light_error = Color(0xFFEF4444)
val md_theme_light_errorContainer = Color(0xFFFEE2E2)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF7F1D1D)
val md_theme_light_background = Color(0xFFF8FAFC)
val md_theme_light_onBackground = Color(0xFF0F172A)
val md_theme_light_surface = Color(0xFFFFFFFF)
val md_theme_light_onSurface = Color(0xFF0F172A)
val md_theme_light_surfaceVariant = Color(0xFFF1F5F9)
val md_theme_light_onSurfaceVariant = Color(0xFF64748B)
val md_theme_light_outline = Color(0xFFE2E8F0)

// Material 3 Dark Theme Colors
val md_theme_dark_primary = Color(0xFFFBBF24)
val md_theme_dark_onPrimary = Color(0xFF451A03)
val md_theme_dark_primaryContainer = Color(0xFF78350F)
val md_theme_dark_onPrimaryContainer = Color(0xFFFDE68A)
val md_theme_dark_secondary = Color(0xFF94A3B8)
val md_theme_dark_onSecondary = Color(0xFF0F172A)
val md_theme_dark_secondaryContainer = Color(0xFF1E293B)
val md_theme_dark_onSecondaryContainer = Color(0xFFE2E8F0)
val md_theme_dark_tertiary = Color(0xFFA78BFA)
val md_theme_dark_onTertiary = Color(0xFF2E1065)
val md_theme_dark_tertiaryContainer = Color(0xFF5B21B6)
val md_theme_dark_onTertiaryContainer = Color(0xFFEDE9FE)
val md_theme_dark_error = Color(0xFFF87171)
val md_theme_dark_errorContainer = Color(0xFF7F1D1D)
val md_theme_dark_onError = Color(0xFF450A0A)
val md_theme_dark_onErrorContainer = Color(0xFFFEE2E2)
val md_theme_dark_background = Color(0xFF0B0F19)
val md_theme_dark_onBackground = Color(0xFFF8FAFC)
val md_theme_dark_surface = Color(0xFF111827)
val md_theme_dark_onSurface = Color(0xFFF8FAFC)
val md_theme_dark_surfaceVariant = Color(0xFF1F2937)
val md_theme_dark_onSurfaceVariant = Color(0xFF9CA3AF)
val md_theme_dark_outline = Color(0xFF374151)

