package com.example.djeventhub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// NEON-NOIR Color Scheme (Dark Mode Only)
private val NeonNoirColorScheme = darkColorScheme(
    // Primary (Main brand color - Neon Pink)
    primary = NeonPink,
    onPrimary = DeepBlack,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = NeonPink,

    // Secondary (Accent color - Electric Blue)
    secondary = ElectricBlue,
    onSecondary = DeepBlack,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = ElectricBlue,

    // Tertiary (Purple accent)
    tertiary = NeonPurple,
    onTertiary = DeepBlack,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = NeonPurple,

    // Backgrounds
    background = DeepBlack,
    onBackground = TextPrimary,

    // Surfaces (Cards, Dialogs)
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    // Error states
    error = ErrorRed,
    onError = TextPrimary,
    errorContainer = DarkSurfaceVariant,
    onErrorContainer = ErrorRed,

    // Outline (borders, dividers)
    outline = TextTertiary,
    outlineVariant = DarkSurfaceVariant,

    // Inverse (for snackbars, etc)
    inverseSurface = TextPrimary,
    inverseOnSurface = DeepBlack,
    inversePrimary = NeonPink
)

@Composable
fun DJEventHubTheme(
    // Force dark theme always (Neon-Noir is dark-only)
    darkTheme: Boolean = true,
    // Disable dynamic color to maintain brand consistency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Always use Neon-Noir color scheme
    val colorScheme = NeonNoirColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}