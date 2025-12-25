package com.dentalvision.ai.di

import com.dentalvision.ai.platform.DesktopFilePicker
import com.dentalvision.ai.platform.FilePicker
import org.koin.dsl.module

/**
 * Desktop (JVM) implementation of platformModule
 * Provides platform-specific dependencies for Desktop
 */
actual val platformModule = module {
    // FilePicker for Desktop
    single<FilePicker> {
        DesktopFilePicker()
    }
}
