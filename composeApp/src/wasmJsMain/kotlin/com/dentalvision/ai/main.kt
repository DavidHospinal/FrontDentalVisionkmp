package com.dentalvision.ai

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.dentalvision.ai.di.appModules
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Initialize Napier logging
    Napier.base(DebugAntilog())
    Napier.d("Web App: Initializing Koin...")

    // Initialize Koin for Web/WASM
    startKoin {
        modules(appModules)
    }

    Napier.i("Web App: Koin initialized successfully")

    ComposeViewport(document.getElementById("root")!!) {
        App()
    }
}
