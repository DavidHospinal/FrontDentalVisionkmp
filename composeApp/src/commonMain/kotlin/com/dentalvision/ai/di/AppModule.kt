package com.dentalvision.ai.di

/**
 * Main application Koin module
 * Combines all feature modules for dependency injection
 */
val appModules = listOf(
    networkModule,
    repositoryModule,
    viewModelModule,
    platformModule
)
