package com.example.dailybreafrewrite.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentWhite,
    secondary = TextSecondary,
    tertiary = TextTertiary,
    background = BgPrimary,
    surface = BgSurface,
    surfaceVariant = BgElevated,
    error = AccentRed,
    onPrimary = BgPrimary,
    onSecondary = AccentWhite,
    onTertiary = AccentWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    onError = AccentWhite,
    outline = Divider,
)

@Composable
fun DailyBriefTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = DailyBriefTypography,
        content = content,
    )
}
