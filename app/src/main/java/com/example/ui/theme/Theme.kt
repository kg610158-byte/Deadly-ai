package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DeadlyDarkColorScheme = darkColorScheme(
    primary = NeonRed,
    onPrimary = TextWhite,
    primaryContainer = DarkCrimson,
    onPrimaryContainer = TextWhite,
    secondary = ElectricBlue,
    onSecondary = VoidBlack,
    secondaryContainer = DeepCyan,
    onSecondaryContainer = TextWhite,
    tertiary = ElectricBlueGlow,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = TextWhite,
    surface = DarkGraphite,
    onSurface = TextWhite,
    surfaceVariant = ElevatedGraphite,
    onSurfaceVariant = TextMuted,
    outline = BorderCyan,
    outlineVariant = BorderRed
)

@Composable
fun DeadlyAiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DeadlyDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for existing usages
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    DeadlyAiTheme(content = content)
}
