package com.dentalvision.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.dentalvision.ai.presentation.navigation.DentalVisionNavGraph
import com.dentalvision.ai.presentation.theme.DentalVisionAITheme
import org.koin.compose.KoinApplication

/**
 * Main Application Composable
 * Entry point for Dental Vision AI Kotlin Multiplatform App
 *
 * Integrates:
 * - Koin Dependency Injection
 * - Material Design 3 Theming
 * - AndroidX Navigation Compose
 */
@Composable
fun App() {
    KoinApplication(application = {
        // TODO: Add Koin modules when implementing DI
        // modules(appModule, dataModule, domainModule, presentationModule)
    }) {
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
