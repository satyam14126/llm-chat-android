package com.llmchat.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
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

private val LightColorScheme = lightColorScheme(
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

@Composable
fun LLMChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
