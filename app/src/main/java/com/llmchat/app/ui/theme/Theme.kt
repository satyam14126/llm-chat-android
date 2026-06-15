package com.llmchat.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Default Dark Theme (Original)
private val DefaultDarkColorScheme = darkColorScheme(
    primary = Color(0xFF82AAFF),
    onPrimary = Color(0xFF003380),
    primaryContainer = Color(0xFF004CB3),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFBBC7E0),
    onSecondary = Color(0xFF25314A),
    secondaryContainer = Color(0xFF3B4862),
    onSecondaryContainer = Color(0xFFD7E3F8),
    tertiary = Color(0xFFD8BBFF),
    onTertiary = Color(0xFF3D0080),
    tertiaryContainer = Color(0xFF5700B3),
    onTertiaryContainer = Color(0xFFEEDDFF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44464F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44464F),
    surfaceContainer = Color(0xFF1F2026),
    surfaceContainerHigh = Color(0xFF292A31),
    surfaceContainerLow = Color(0xFF1A1B21)
)

private val DefaultLightColorScheme = lightColorScheme(
    primary = Color(0xFF1558D6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B4A),
    secondary = Color(0xFF535F78),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7E3F8),
    onSecondaryContainer = Color(0xFF0F1C31),
    tertiary = Color(0xFF7000CC),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEEDDFF),
    onTertiaryContainer = Color(0xFF1E004B),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF8F9FF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44464F),
    outline = Color(0xFF747780),
    outlineVariant = Color(0xFFC4C6D0),
    surfaceContainer = Color(0xFFECEDF4),
    surfaceContainerHigh = Color(0xFFE6E7EE),
    surfaceContainerLow = Color(0xFFF3F4FB)
)

// Ocean Theme (Cool blues and teals)
private val OceanDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00BCD4),
    onPrimary = Color(0xFF003D4D),
    primaryContainer = Color(0xFF005A6B),
    onPrimaryContainer = Color(0xFFB3F0FF),
    secondary = Color(0xFF80DEEA),
    onSecondary = Color(0xFF003D42),
    secondaryContainer = Color(0xFF005055),
    onSecondaryContainer = Color(0xFFB2EBEF),
    tertiary = Color(0xFF4DD0E1),
    onTertiary = Color(0xFF003D42),
    tertiaryContainer = Color(0xFF005055),
    onTertiaryContainer = Color(0xFFB2EBEF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0D1B20),
    onBackground = Color(0xFFDFE3E5),
    surface = Color(0xFF0D1B20),
    onSurface = Color(0xFFDFE3E5),
    surfaceVariant = Color(0xFF3F4A4E),
    onSurfaceVariant = Color(0xFFC3CCCE),
    outline = Color(0xFF8D9698),
    outlineVariant = Color(0xFF3F4A4E),
    surfaceContainer = Color(0xFF1B2428),
    surfaceContainerHigh = Color(0xFF252E32),
    surfaceContainerLow = Color(0xFF151D21)
)

private val OceanLightColorScheme = lightColorScheme(
    primary = Color(0xFF0097A7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB3E5FC),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = Color(0xFF00838F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB2EBEF),
    onSecondaryContainer = Color(0xFF001F25),
    tertiary = Color(0xFF0097A7),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB2EBEF),
    onTertiaryContainer = Color(0xFF001F25),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAFDFE),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFFAFDFE),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDCE4E6),
    onSurfaceVariant = Color(0xFF3F4A4E),
    outline = Color(0xFF6F7A7E),
    outlineVariant = Color(0xFFC3CCCE),
    surfaceContainer = Color(0xFFEFF1F3),
    surfaceContainerHigh = Color(0xFFE9ECEE),
    surfaceContainerLow = Color(0xFFF5F8F9)
)

// Forest Theme (Greens)
private val ForestDarkColorScheme = darkColorScheme(
    primary = Color(0xFF66BB6A),
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFF2E7D32),
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = Color(0xFF4CAF50),
    onTertiary = Color(0xFF1B5E20),
    tertiaryContainer = Color(0xFF2E7D32),
    onTertiaryContainer = Color(0xFFC8E6C9),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F1B0F),
    onBackground = Color(0xFFE2E8E2),
    surface = Color(0xFF0F1B0F),
    onSurface = Color(0xFFE2E8E2),
    surfaceVariant = Color(0xFF3F4A3F),
    onSurfaceVariant = Color(0xFFC3CCBE),
    outline = Color(0xFF8D9689),
    outlineVariant = Color(0xFF3F4A3F),
    surfaceContainer = Color(0xFF1D2620),
    surfaceContainerHigh = Color(0xFF27302A),
    surfaceContainerLow = Color(0xFF17201A)
)

private val ForestLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF558B2F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC5E1A5),
    onSecondaryContainer = Color(0xFF33691E),
    tertiary = Color(0xFF689F38),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC5E1A5),
    onTertiaryContainer = Color(0xFF33691E),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8FBF7),
    onBackground = Color(0xFF191C19),
    surface = Color(0xFFF8FBF7),
    onSurface = Color(0xFF191C19),
    surfaceVariant = Color(0xFFDFE4D8),
    onSurfaceVariant = Color(0xFF3F4A3F),
    outline = Color(0xFF6F7A6F),
    outlineVariant = Color(0xFFC3CCBE),
    surfaceContainer = Color(0xFFECF0E8),
    surfaceContainerHigh = Color(0xFFE6EAE1),
    surfaceContainerLow = Color(0xFFF3F7F0)
)

// Sunset Theme (Warm oranges and reds)
private val SunsetDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF7043),
    onPrimary = Color(0xFF5D1F0F),
    primaryContainer = Color(0xFF8B3A1A),
    onPrimaryContainer = Color(0xFFFFDBC8),
    secondary = Color(0xFFFFB74D),
    onSecondary = Color(0xFF663C00),
    secondaryContainer = Color(0xFF8B5A00),
    onSecondaryContainer = Color(0xFFFFDBC8),
    tertiary = Color(0xFFFF9800),
    onTertiary = Color(0xFF4D2600),
    tertiaryContainer = Color(0xFF7A4000),
    onTertiaryContainer = Color(0xFFFFDBC8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A0F0A),
    onBackground = Color(0xFFE8DFD9),
    surface = Color(0xFF1A0F0A),
    onSurface = Color(0xFFE8DFD9),
    surfaceVariant = Color(0xFF4A3F38),
    onSurfaceVariant = Color(0xFFC9BDB4),
    outline = Color(0xFF8D8178),
    outlineVariant = Color(0xFF4A3F38),
    surfaceContainer = Color(0xFF261814),
    surfaceContainerHigh = Color(0xFF30221D),
    surfaceContainerLow = Color(0xFF1F1410)
)

private val SunsetLightColorScheme = lightColorScheme(
    primary = Color(0xFFD84315),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBC8),
    onPrimaryContainer = Color(0xFF5D1F0F),
    secondary = Color(0xFFF57C00),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBC8),
    onSecondaryContainer = Color(0xFF663C00),
    tertiary = Color(0xFFE65100),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDBC8),
    onTertiaryContainer = Color(0xFF4D2600),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFEF8F5),
    onBackground = Color(0xFF1F1612),
    surface = Color(0xFFFEF8F5),
    onSurface = Color(0xFF1F1612),
    surfaceVariant = Color(0xFFECDDD3),
    onSurfaceVariant = Color(0xFF4A3F38),
    outline = Color(0xFF7A6F67),
    outlineVariant = Color(0xFFC9BDB4),
    surfaceContainer = Color(0xFFF3EBE6),
    surfaceContainerHigh = Color(0xFFEDE5E0),
    surfaceContainerLow = Color(0xFFFBF3F0)
)

enum class ThemeMode {
    DEFAULT, OCEAN, FOREST, SUNSET
}

@Composable
fun LLMChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: ThemeMode = ThemeMode.DEFAULT,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> when (themeMode) {
            ThemeMode.DEFAULT -> DefaultDarkColorScheme
            ThemeMode.OCEAN -> OceanDarkColorScheme
            ThemeMode.FOREST -> ForestDarkColorScheme
            ThemeMode.SUNSET -> SunsetDarkColorScheme
        }
        else -> when (themeMode) {
            ThemeMode.DEFAULT -> DefaultLightColorScheme
            ThemeMode.OCEAN -> OceanLightColorScheme
            ThemeMode.FOREST -> ForestLightColorScheme
            ThemeMode.SUNSET -> SunsetLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
