package com.dentalvision.ai.di

import com.dentalvision.ai.platform.FilePicker
import org.koin.dsl.module

/**
 * Android implementation of platformModule
 * Provides platform-specific dependencies for Android
 */
actual val platformModule = module {
    // FilePicker is context-dependent on Android
    // Will be provided at Activity level or using AndroidFilePicker(activity) directly
    // For now, we don't register it globally as it requires Activity context
}
