package com.dentalvision.ai.android

import MainView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dentalvision.ai.platform.ActivityContextHolder
import com.dentalvision.ai.platform.AndroidFilePicker
import com.dentalvision.ai.platform.FilePicker
import io.github.aakira.napier.Napier
import org.koin.android.ext.android.inject

/**
 * Main Activity for Dental Vision AI
 * CRITICAL: Initializes FilePicker early to avoid Lifecycle errors
 */
class MainActivity : ComponentActivity() {

    // Inject FilePicker to initialize it early
    private val filePicker: FilePicker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Napier.d("MainActivity: onCreate started")

        // STEP 1: Register Activity context FIRST (required by FilePicker)
        ActivityContextHolder.setActivity(this)

        // STEP 2: Initialize FilePicker launcher EARLY (before STARTED state)
        // This prevents "attempting to register while current state is RESUMED" error
        if (filePicker is AndroidFilePicker) {
            (filePicker as AndroidFilePicker).initializeEarly()
            Napier.i("MainActivity: FilePicker initialized early")
        } else {
            Napier.w("MainActivity: FilePicker is not AndroidFilePicker (${filePicker::class.simpleName})")
        }

        // STEP 3: Set content (Activity will now transition to STARTED/RESUMED)
        setContent {
            MainView()
        }

        Napier.d("MainActivity: onCreate completed")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear Activity reference to prevent memory leaks
        ActivityContextHolder.clearActivity()
        Napier.d("MainActivity: onDestroy - Activity context cleared")
    }
}