package com.agape.sovereign.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentMagenta,
    secondary = AccentBlue,
    tertiary = AccentOrange,
    background = ObsidianBg,
    surface = PanelBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightText,
    onSurface = LightText
)

private val LightColorScheme = lightColorScheme(
    primary = AccentMagenta,
    secondary = AccentBlue,
    tertiary = AccentOrange,
    background = Color(0xFFF9FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF13151F),
    onSurface = Color(0xFF13151F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Theme for Privacy & Security Apps as standard best practice
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve Magenta, Blue, Orange requested brand scheme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
