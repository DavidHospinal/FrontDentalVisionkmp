package com.dentalvision.ai.android

import MainView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dentalvision.ai.platform.ActivityContextHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Activity context for FilePicker and other platform features
        // CRITICAL: Must be called BEFORE setContent to ensure FilePicker has access to Activity
        ActivityContextHolder.setActivity(this)

        setContent {
            MainView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear Activity reference to prevent memory leaks
        ActivityContextHolder.clearActivity()
    }
}