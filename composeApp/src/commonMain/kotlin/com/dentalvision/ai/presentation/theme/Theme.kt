package com.dentalvision.ai.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Dental Vision AI Theme
 * Professional medical/clinical theme with Material Design 3
 */
@Composable
fun DentalVisionAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DentalTypography,
        shapes = DentalShapes,
        content = content
    )
}
