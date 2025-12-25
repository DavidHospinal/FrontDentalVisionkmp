package com.dentalvision.ai.di

import com.dentalvision.ai.platform.FilePicker
import com.dentalvision.ai.platform.createFilePicker
import org.koin.dsl.module

/**
 * Android implementation of platformModule
 * Provides platform-specific dependencies for Android
 */
actual val platformModule = module {
    // FilePicker for Android using ActivityContextHolder
    single<FilePicker> {
        createFilePicker()
    }
}
