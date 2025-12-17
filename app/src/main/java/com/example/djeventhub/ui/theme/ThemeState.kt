package com.example.djeventhub.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

// Simple theme controller state with observable state
class ThemeController(initialDark: Boolean = true) {
    var isDark by mutableStateOf(initialDark)
        private set

    fun toggle() {
        isDark = !isDark
    }
}

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    error("No ThemeController provided")
}

@Composable
fun rememberThemeController(defaultDark: Boolean = true): ThemeController {
    return remember {
        ThemeController(defaultDark)
    }
}
