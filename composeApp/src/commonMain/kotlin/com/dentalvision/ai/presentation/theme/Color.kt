package com.dentalvision.ai.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Dental Vision AI Color Palette
 * Medical/Clinical theme with professional blue and teal accents
 */
object DentalColors {
    // Primary Colors
    val Primary = Color(0xFF0066CC)           // Medical Blue
    val PrimaryVariant = Color(0xFF0052A3)    // Darker Blue
    val Secondary = Color(0xFF00A896)          // Teal Accent
    val SecondaryVariant = Color(0xFF008C7A)  // Darker Teal

    // Background & Surface
    val Background = Color(0xFFFFFFFF)        // Pure White
    val Surface = Color(0xFFFFFFFF)           // Pure White
    val SurfaceVariant = Color(0xFFF8F9FA)    // Very Light Gray

    // Text Colors
    val OnPrimary = Color(0xFFFFFFFF)         // White text on primary
    val OnSecondary = Color(0xFFFFFFFF)       // White text on secondary
    val OnBackground = Color(0xFF000000)      // Pure Black on background
    val OnSurface = Color(0xFF212121)         // Dark gray on surface

    // Semantic Colors
    val Error = Color(0xFFD32F2F)             // Red for errors
    val Success = Color(0xFF388E3C)           // Green for success
    val Warning = Color(0xFFFF9800)           // Orange for warnings
    val Info = Color(0xFF2196F3)              // Blue for info

    // Dental-Specific Colors
    val ToothHealthy = Color(0xFF81C784)      // Light Green (healthy tooth)
    val ToothCaries = Color(0xFFE57373)       // Light Red (caries detected)
    val ToothNormal = Color(0xFFE0E0E0)       // Gray (normal/not analyzed)

    // Chart Colors (for analysis visualization)
    val ChartBlue = Color(0xFF42A5F5)
    val ChartGreen = Color(0xFF66BB6A)
    val ChartOrange = Color(0xFFFF9800)
    val ChartRed = Color(0xFFEF5350)
    val ChartPurple = Color(0xFFAB47BC)

    // Severity Levels
    val SeverityExcellent = Color(0xFF4CAF50)   // Green
    val SeverityGood = Color(0xFF8BC34A)        // Light Green
    val SeverityModerate = Color(0xFFFFC107)    // Amber
    val SeverityConcerning = Color(0xFFFF9800)  // Orange
    val SeveritySevere = Color(0xFFF44336)      // Red

    // Overlay & Divider
    val Overlay = Color(0x80000000)           // Black 50% opacity
    val Divider = Color(0xFFE0E0E0)           // Light gray
}

/**
 * Material 3 Light Color Scheme
 */
val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = DentalColors.Primary,
    onPrimary = DentalColors.OnPrimary,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001D36),

    secondary = DentalColors.Secondary,
    onSecondary = DentalColors.OnSecondary,
    secondaryContainer = Color(0xFFB8F3E8),
    onSecondaryContainer = Color(0xFF002019),

    tertiary = Color(0xFF6A5D00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF5E287),
    onTertiaryContainer = Color(0xFF1F1B00),

    error = DentalColors.Error,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = DentalColors.Background,
    onBackground = DentalColors.OnBackground,

    surface = DentalColors.Surface,
    onSurface = DentalColors.OnSurface,
    surfaceVariant = DentalColors.SurfaceVariant,
    onSurfaceVariant = Color(0xFF212121),

    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C6CF),

    scrim = Color(0xFF000000),

    inverseSurface = Color(0xFF2E3036),
    inverseOnSurface = Color(0xFFF0F0F7),
    inversePrimary = Color(0xFFA8C8FF)
)

/**
 * Material 3 Dark Color Scheme
 */
val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF00315A),
    primaryContainer = Color(0xFF004880),
    onPrimaryContainer = Color(0xFFD6E3FF),

    secondary = Color(0xFF9CD6CC),
    onSecondary = Color(0xFF00352D),
    secondaryContainer = Color(0xFF005143),
    onSecondaryContainer = Color(0xFFB8F3E8),

    tertiary = Color(0xFFD8C66E),
    onTertiary = Color(0xFF363100),
    tertiaryContainer = Color(0xFF4E4700),
    onTertiaryContainer = Color(0xFFF5E287),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E9),

    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C6CF),

    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF43474E),

    scrim = Color(0xFF000000),

    inverseSurface = Color(0xFFE2E2E9),
    inverseOnSurface = Color(0xFF2E3036),
    inversePrimary = Color(0xFF005FA8)
)
