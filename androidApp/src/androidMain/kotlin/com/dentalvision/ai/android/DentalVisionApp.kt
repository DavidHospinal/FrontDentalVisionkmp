package com.dentalvision.ai.android

import android.app.Application
import com.dentalvision.ai.di.appModules
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for Dental Vision AI
 * CRITICAL: Initializes Koin BEFORE any Activity is created
 *
 * This ensures that dependency injection is ready when MainActivity tries to inject dependencies
 */
class DentalVisionApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Napier logging
        Napier.base(DebugAntilog())
        Napier.d("DentalVisionApp: onCreate started")

        // Check if Koin is already started (prevents double initialization)
        if (GlobalContext.getOrNull() == null) {
            Napier.d("DentalVisionApp: Initializing Koin...")

            startKoin {
                // Enable Koin Android logger (optional, useful for debugging)
                androidLogger(Level.ERROR) // Change to Level.DEBUG for more verbose logging

                // Provide Android context to Koin
                androidContext(this@DentalVisionApp)

                // Load all application modules
                modules(appModules)
            }

            Napier.i("DentalVisionApp: Koin initialized successfully with ${appModules.size} modules")
        } else {
            Napier.w("DentalVisionApp: Koin already initialized, skipping")
        }

        Napier.d("DentalVisionApp: onCreate completed")
    }
}
