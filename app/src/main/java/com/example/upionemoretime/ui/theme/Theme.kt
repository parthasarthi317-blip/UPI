package com.example.upionemoretime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun UPIOneMoreTimeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EclipseColorScheme,
        typography = Typography,
        content = content
    )
}
