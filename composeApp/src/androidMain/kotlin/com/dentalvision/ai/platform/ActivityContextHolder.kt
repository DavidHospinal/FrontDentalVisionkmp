package com.dentalvision.ai.platform

import androidx.activity.ComponentActivity
import io.github.aakira.napier.Napier
import java.lang.ref.WeakReference

/**
 * Singleton holder for Android Activity context
 * THREAD-SAFE: Uses synchronized blocks for concurrent access
 * MEMORY-SAFE: Uses WeakReference to prevent memory leaks
 *
 * Lifecycle Integration:
 * - Call setActivity() in Activity.onCreate() BEFORE any other setup
 * - Call clearActivity() in Activity.onDestroy() for proper cleanup
 */
object ActivityContextHolder {
    @Volatile
    private var activityRef: WeakReference<ComponentActivity>? = null

    /**
     * Set current Activity
     * MUST be called from Activity.onCreate() before any FilePicker operations
     */
    @Synchronized
    fun setActivity(activity: ComponentActivity) {
        // Clear previous reference if exists
        activityRef?.clear()

        // Set new reference
        activityRef = WeakReference(activity)
        Napier.d("ActivityContextHolder: Activity set - ${activity.javaClass.simpleName}")
    }

    /**
     * Get current Activity
     * @throws IllegalStateException if Activity is not available or has been garbage collected
     */
    @Synchronized
    fun getActivity(): ComponentActivity {
        val activity = activityRef?.get()

        return when {
            activity == null && activityRef == null -> {
                throw IllegalStateException(
                    "Activity not initialized. Call ActivityContextHolder.setActivity() " +
                    "from MainActivity.onCreate() before using FilePicker."
                )
            }
            activity == null -> {
                throw IllegalStateException(
                    "Activity has been garbage collected. This usually means the Activity " +
                    "was destroyed while FilePicker was still in use."
                )
            }
            activity.isDestroyed -> {
                throw IllegalStateException(
                    "Activity is destroyed. Cannot perform FilePicker operations " +
                    "on a destroyed Activity."
                )
            }
            activity.isFinishing -> {
                Napier.w("ActivityContextHolder: Activity is finishing, operations may fail")
                activity
            }
            else -> {
                activity
            }
        }
    }

    /**
     * Check if Activity is available and not destroyed
     */
    @Synchronized
    fun isActivityAvailable(): Boolean {
        return try {
            val activity = activityRef?.get()
            activity != null && !activity.isDestroyed && !activity.isFinishing
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clear Activity reference
     * MUST be called from Activity.onDestroy() to prevent memory leaks
     */
    @Synchronized
    fun clearActivity() {
        val activity = activityRef?.get()
        if (activity != null) {
            Napier.d("ActivityContextHolder: Clearing activity - ${activity.javaClass.simpleName}")
        }

        activityRef?.clear()
        activityRef = null

        Napier.d("ActivityContextHolder: Activity reference cleared")
    }
}
