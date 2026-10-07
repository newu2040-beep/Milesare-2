package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun MilesAreTheme(
    preset: AppThemePreset? = null,
    content: @Composable () -> Unit
) {
    val activePreset by ThemeManager.currentTheme.collectAsState()
    val chosenTheme = preset ?: activePreset
    val colorScheme = chosenTheme.toColorScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
