package com.dentalvision.ai.platform

import androidx.activity.ComponentActivity
import java.lang.ref.WeakReference

/**
 * Singleton holder for Android Activity context
 * Provides global access to current Activity for FilePicker and other platform features
 * Uses WeakReference to prevent memory leaks
 */
object ActivityContextHolder {
    private var activityRef: WeakReference<ComponentActivity>? = null

    /**
     * Set current Activity
     * Should be called from Activity.onCreate()
     */
    fun setActivity(activity: ComponentActivity) {
        activityRef = WeakReference(activity)
    }

    /**
     * Get current Activity
     * @throws IllegalStateException if Activity is not available
     */
    fun getActivity(): ComponentActivity {
        return activityRef?.get()
            ?: throw IllegalStateException(
                "Activity not available. Make sure ActivityContextHolder.setActivity() " +
                "is called from MainActivity.onCreate()"
            )
    }

    /**
     * Clear Activity reference
     * Should be called from Activity.onDestroy()
     */
    fun clearActivity() {
        activityRef?.clear()
        activityRef = null
    }
}
