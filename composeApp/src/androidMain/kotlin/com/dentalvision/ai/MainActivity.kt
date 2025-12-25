package com.dentalvision.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dentalvision.ai.platform.ActivityContextHolder

/**
 * Main Activity for Android
 * Entry point for Dental Vision AI on Android
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Activity context for FilePicker and other platform features
        ActivityContextHolder.setActivity(this)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear Activity reference to prevent memory leaks
        ActivityContextHolder.clearActivity()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
