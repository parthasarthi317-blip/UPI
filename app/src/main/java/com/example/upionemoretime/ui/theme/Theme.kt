package com.example.upionemoretime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VoicePayColorScheme = darkColorScheme(

    primary = PrimaryGreen,
    secondary = SecondaryGreen,

    background = DarkBackground,
    surface = DarkSurface,

    onPrimary = TextWhite,
    onSecondary = TextWhite,

    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun UPIOneMoreTimeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VoicePayColorScheme,
        typography = Typography,
        content = content
    )
}