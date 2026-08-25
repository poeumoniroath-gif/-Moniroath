package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF4081),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF880E4F),
    onPrimaryContainer = Color(0xFFFFD8E4),
    secondary = Color(0xFF26C6DA),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFFFFAB40),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceAlt,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = SlushiePinkPrimary,
    onPrimary = Color.White,
    primaryContainer = SlushiePinkLight,
    onPrimaryContainer = SlushiePinkDark,
    secondary = SlushieCyanSecondary,
    onSecondary = Color.White,
    secondaryContainer = SlushieCyanLight,
    onSecondaryContainer = SlushieCyanDark,
    tertiary = CandyOrangeTertiary,
    background = SoftCanvasBackground,
    surface = SurfaceCard,
    surfaceVariant = SurfaceCardAlt,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondary,
    outline = AccentBorder
)

@Composable
fun JollySlushieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
