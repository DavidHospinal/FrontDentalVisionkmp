package com.dentalvision.ai

import androidx.compose.ui.window.ComposeUIViewController

/**
 * iOS entry point
 * Creates UIViewController for SwiftUI integration
 */
fun MainViewController() = ComposeUIViewController { App() }
