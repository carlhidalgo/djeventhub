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

// NEON-NOIR Color Scheme (Dark Mode)
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

// Light color scheme - a simplified light counterpart
private val NeonLightColorScheme = lightColorScheme(
    primary = NeonPink,
    onPrimary = DeepBlack,
    primaryContainer = NeonPink.copy(alpha = 0.1f),
    onPrimaryContainer = DeepBlack,

    secondary = ElectricBlue,
    onSecondary = DeepBlack,
    secondaryContainer = ElectricBlue.copy(alpha = 0.08f),
    onSecondaryContainer = DeepBlack,

    tertiary = NeonPurple,
    onTertiary = DeepBlack,

    background = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
    onBackground = DeepBlack,

    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = DeepBlack,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF0F0F0),
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = androidx.compose.ui.graphics.Color.White,

    outline = TextTertiary,
    inverseSurface = DeepBlack,
    inverseOnSurface = TextPrimary,
    inversePrimary = NeonPink
)

@Composable
fun DJEventHubTheme(
    darkTheme: Boolean = true,
    // Keep the parameter for compatibility
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Choose color scheme based on the flag
    val colorScheme = if (darkTheme) NeonNoirColorScheme else NeonLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}