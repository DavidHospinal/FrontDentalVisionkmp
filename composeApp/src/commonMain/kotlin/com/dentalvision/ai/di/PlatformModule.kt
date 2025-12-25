package com.dentalvision.ai.di

import org.koin.dsl.module

/**
 * Koin module for Platform-specific implementations
 * Uses expect/actual pattern for multiplatform support
 */
expect val platformModule: org.koin.core.module.Module
