package com.example.upionemoretime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EclipseColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    secondary = SecondaryEmerald,
    tertiary = WarningAmber,
    background = Obsidian,
    surface = DeepSlate,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = Obsidian,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRose,
    outline = TextDisabled,
    surfaceVariant = CardSurface
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    secondary = SecondaryEmerald,
    tertiary = WarningAmber,
    background = Color(0xFFF5F7F9),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = ErrorRose,
    outline = Color.LightGray,
    surfaceVariant = Color(0xFFE2E8F0)
)

@Composable
fun UPIOneMoreTimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EclipseColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
