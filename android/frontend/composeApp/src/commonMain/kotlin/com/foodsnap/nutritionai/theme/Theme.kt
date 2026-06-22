package com.foodsnap.nutritionai.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium Design System Colors
val NeonGreen = Color(0xFF10B981)
val Cyan = Color(0xFF06B6D4)

// Dark Theme Colors
val DarkBackground = Color(0xFF050B18)
val CardBackgroundDark = Color(0xFF111827)
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFF94A3B8)
val GlassSurfaceDark = Color(0x1AFFFFFF)
val GlassBorderDark = Color(0x15FFFFFF)

// Light Theme Colors
val LightBackground = Color(0xFFF8FAFC)
val CardBackgroundLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val GlassSurfaceLight = Color(0x0A000000)
val GlassBorderLight = Color(0x11000000)

// Backward compatibility / mapping aliases
val PrimaryGreen = NeonGreen
val SecondaryGreen = Cyan
val DarkSurface = CardBackgroundDark
val GlassSurface = GlassSurfaceDark
val GlassBorder = GlassBorderDark
val LightText = TextPrimaryDark
val GrayText = TextSecondaryDark
val AccentOrange = Color(0xFFF59E0B)

val PrimaryPurple = NeonGreen
val SecondaryPurple = Cyan

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = Cyan,
    background = DarkBackground,
    surface = CardBackgroundDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = NeonGreen,
    secondary = Cyan,
    background = LightBackground,
    surface = CardBackgroundLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight
)

@Composable
fun FoodSnapTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
