package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemePreset(
    val title: String,
    val description: String,
    val previewColor: Color,
    val isDark: Boolean,
    val gradientColors: List<Color>,
    val cardColor: Color
) {
    MIDNIGHT(
        title = "Midnight Violet",
        description = "Mysterious obsidian & electric lavender",
        previewColor = Color(0xFFB388FF),
        isDark = true,
        gradientColors = listOf(Color(0xFF0D0B18), Color(0xFF1E1435), Color(0xFF130924)),
        cardColor = Color(0xFF1F1A35)
    ),
    PASTEL_LILAC(
        title = "Pastel Lilac",
        description = "Soothing lavender & soft orchid",
        previewColor = Color(0xFFC084FC),
        isDark = false,
        gradientColors = listOf(Color(0xFFFBF7FF), Color(0xFFF3E8FF), Color(0xFFE9D5FF)),
        cardColor = Color(0xFFF8F0FF)
    ),
    PASTEL_ROSE(
        title = "Pastel Rose",
        description = "Blush strawberry & gentle blossom",
        previewColor = Color(0xFFFDA4AF),
        isDark = false,
        gradientColors = listOf(Color(0xFFFFF5F6), Color(0xFFFFE4E6), Color(0xFFFECDD3)),
        cardColor = Color(0xFFFFF0F2)
    ),
    PASTEL_MINT(
        title = "Pastel Mint",
        description = "Calming sage & whisper green",
        previewColor = Color(0xFF6EE7B7),
        isDark = false,
        gradientColors = listOf(Color(0xFFF4FBF7), Color(0xFFDCFCE7), Color(0xFFD1FAE5)),
        cardColor = Color(0xFFEEFBF2)
    ),
    PASTEL_SKY(
        title = "Pastel Sky",
        description = "Cloud blue & serene dawn",
        previewColor = Color(0xFF7DD3FC),
        isDark = false,
        gradientColors = listOf(Color(0xFFF4FAFF), Color(0xFFE0F2FE), Color(0xFFBAE6FD)),
        cardColor = Color(0xFFEDF8FF)
    ),
    PASTEL_PEACH(
        title = "Pastel Peach",
        description = "Warm apricot cream & sunset",
        previewColor = Color(0xFFFDBA74),
        isDark = false,
        gradientColors = listOf(Color(0xFFFFF8F1), Color(0xFFFFEDD5), Color(0xFFFED7AA)),
        cardColor = Color(0xFFFFF3E6)
    ),
    PASTEL_BUTTER(
        title = "Pastel Butter",
        description = "Soft sunlight vanilla & honey",
        previewColor = Color(0xFFFDE047),
        isDark = false,
        gradientColors = listOf(Color(0xFFFFFDEB), Color(0xFFFEF9C3), Color(0xFFFEF08A)),
        cardColor = Color(0xFFFFFCE8)
    );

    fun toColorScheme(): ColorScheme {
        return if (isDark) {
            darkColorScheme(
                primary = previewColor,
                onPrimary = Color(0xFF1D0048),
                primaryContainer = Color(0xFF6B21A8),
                onPrimaryContainer = Color(0xFFF3E8FF),
                secondary = Color(0xFFFF4081),
                onSecondary = Color(0xFF3F0015),
                secondaryContainer = Color(0xFF5A0023),
                onSecondaryContainer = Color(0xFFFFD9E2),
                tertiary = Color(0xFF00E5FF),
                onTertiary = Color(0xFF00363D),
                background = gradientColors.first(),
                onBackground = Color(0xFFF3F0FA),
                surface = cardColor,
                onSurface = Color(0xFFF3F0FA),
                surfaceVariant = Color(0xFF261F3F),
                onSurfaceVariant = Color(0xFFAFA7C9),
                outline = Color(0xFF4C3F70),
                outlineVariant = Color(0xFF2E2648)
            )
        } else {
            lightColorScheme(
                primary = when (this) {
                    PASTEL_LILAC -> Color(0xFF7E22CE)
                    PASTEL_ROSE -> Color(0xFFBE123C)
                    PASTEL_MINT -> Color(0xFF047857)
                    PASTEL_SKY -> Color(0xFF0369A1)
                    PASTEL_PEACH -> Color(0xFFC2410C)
                    PASTEL_BUTTER -> Color(0xFFA16207)
                    else -> Color(0xFF7E22CE)
                },
                onPrimary = Color.White,
                primaryContainer = gradientColors[1],
                onPrimaryContainer = when (this) {
                    PASTEL_LILAC -> Color(0xFF581C87)
                    PASTEL_ROSE -> Color(0xFF881337)
                    PASTEL_MINT -> Color(0xFF064E3B)
                    PASTEL_SKY -> Color(0xFF0C4A6E)
                    PASTEL_PEACH -> Color(0xFF7C2D12)
                    PASTEL_BUTTER -> Color(0xFF713F12)
                    else -> Color(0xFF581C87)
                },
                secondary = previewColor,
                onSecondary = Color(0xFF1A1423),
                secondaryContainer = gradientColors.last(),
                onSecondaryContainer = Color(0xFF1E1B2E),
                tertiary = Color(0xFF4338CA),
                onTertiary = Color.White,
                background = gradientColors.first(),
                onBackground = Color(0xFF1E182B),
                surface = cardColor,
                onSurface = Color(0xFF1E182B),
                surfaceVariant = gradientColors[1],
                onSurfaceVariant = Color(0xFF5E5470),
                outline = previewColor.copy(alpha = 0.6f),
                outlineVariant = gradientColors[1]
            )
        }
    }
}

object ThemeManager {
    private val _currentTheme = MutableStateFlow(AppThemePreset.MIDNIGHT)
    val currentTheme: StateFlow<AppThemePreset> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppThemePreset) {
        _currentTheme.value = theme
    }
}
