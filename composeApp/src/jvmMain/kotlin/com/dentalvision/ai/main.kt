package com.dentalvision.ai

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * Main entry point for Desktop (JVM) application
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Dental Vision AI - Professional Dental Analysis System"
    ) {
        App()
    }
}
