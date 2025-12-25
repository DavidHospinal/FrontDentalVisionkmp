package com.dentalvision.ai.di

import com.dentalvision.ai.platform.FilePicker
import com.dentalvision.ai.platform.WebFilePicker
import org.koin.dsl.module

/**
 * Web/WASM implementation of platformModule
 * Provides platform-specific dependencies for Web
 */
actual val platformModule = module {
    // FilePicker for Web
    single<FilePicker> {
        WebFilePicker()
    }
}
