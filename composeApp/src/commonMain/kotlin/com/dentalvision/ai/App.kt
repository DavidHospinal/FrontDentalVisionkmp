package com.dentalvision.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.dentalvision.ai.presentation.navigation.DentalVisionNavGraph
import com.dentalvision.ai.presentation.theme.DentalVisionAITheme
import org.koin.compose.KoinContext
/**
 * Main Application Composable
 * Entry point for Dental Vision AI Kotlin Multiplatform App
 *
 * Integrates:
 * - Koin Dependency Injection (initialized by platform-specific code)
 * - Material Design 3 Theming
 * - AndroidX Navigation Compose
 *
 * NOTE: Koin is initialized by:
 * - Android: DentalVisionApp.onCreate()
 * - Desktop/Web: Platform-specific entry points
 */
@Composable
fun App() {
    // KoinContext accesses the already-initialized Koin instance
    // (no attempt to re-initialize, preventing crashes on Android)
    KoinContext {
        DentalVisionAITheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // Main Navigation Graph
                DentalVisionNavGraph()
            }
        }
    }
}
