package com.dentalvision.ai.platform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import java.io.InputStream

/**
 * Android implementation of FilePicker
 * FIXED: Early launcher initialization to avoid Lifecycle errors
 * The launcher MUST be registered before Activity reaches STARTED state
 */
class AndroidFilePicker : FilePicker {

    private var currentDeferred: CompletableDeferred<FilePickerResult>? = null

    // Launcher is initialized EAGERLY when initializeEarly() is called from MainActivity.onCreate()
    // This ensures registration happens BEFORE Activity reaches STARTED/RESUMED
    @Volatile
    private var _launcher: ActivityResultLauncher<Intent>? = null

    /**
     * Initialize launcher early in Activity lifecycle
     * MUST be called from MainActivity.onCreate() BEFORE setContent()
     */
    fun initializeEarly() {
        if (_launcher != null) {
            Napier.w("AndroidFilePicker: Launcher already initialized, skipping")
            return
        }

        synchronized(this) {
            if (_launcher != null) return

            val activity = ActivityContextHolder.getActivity()
            Napier.d("AndroidFilePicker: Initializing launcher in ${activity.javaClass.simpleName}")

            _launcher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                Napier.d("AndroidFilePicker: Result received - code=${result.resultCode}")
                when {
                    result.resultCode == Activity.RESULT_OK && result.data?.data != null -> {
                        handleSelectedImage(result.data!!.data!!)
                    }
                    result.resultCode == Activity.RESULT_CANCELED -> {
                        currentDeferred?.complete(FilePickerResult.Cancelled)
                    }
                    else -> {
                        currentDeferred?.complete(
                            FilePickerResult.Error("Unexpected result code: ${result.resultCode}")
                        )
                    }
                }
            }

            Napier.i("AndroidFilePicker: Launcher initialized successfully")
        }
    }

    private fun ensureLauncherInitialized(): ActivityResultLauncher<Intent> {
        return _launcher ?: throw IllegalStateException(
            "AndroidFilePicker not initialized. Call initializeEarly() from MainActivity.onCreate()"
        )
    }

    override suspend fun pickImage(): FilePickerResult {
        try {
            Napier.d("AndroidFilePicker: Starting image selection")

            // Ensure launcher is initialized
            val launcher = ensureLauncherInitialized()

            // Create new deferred for this operation
            currentDeferred = CompletableDeferred()

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/jpg"))
            }

            launcher.launch(intent)

            val result = currentDeferred!!.await()
            Napier.d("AndroidFilePicker: Selection completed - ${result::class.simpleName}")
            return result

        } catch (e: Exception) {
            Napier.e("AndroidFilePicker: Error during image selection", e)
            return FilePickerResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            val activity = ActivityContextHolder.getActivity()
            val contentResolver = activity.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)

            if (inputStream == null) {
                currentDeferred?.complete(
                    FilePickerResult.Error("Could not open file")
                )
                return
            }

            val fileBytes = inputStream.readBytes()
            inputStream.close()

            // Get file name
            val fileName = getFileName(uri) ?: "dental_image_${System.currentTimeMillis()}.jpg"

            // Get MIME type
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

            Napier.i("AndroidFilePicker: Image selected successfully - $fileName (${fileBytes.size} bytes, $mimeType)")

            currentDeferred?.complete(
                FilePickerResult.Success(
                    data = fileBytes,
                    name = fileName,
                    mimeType = mimeType
                )
            )

        } catch (e: Exception) {
            Napier.e("AndroidFilePicker: Error reading selected image", e)
            currentDeferred?.complete(
                FilePickerResult.Error(e.message ?: "Error reading file")
            )
        }
    }

    private fun getFileName(uri: Uri): String? {
        val activity = ActivityContextHolder.getActivity()
        return activity.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else null
        }
    }
}

/**
 * Create Android FilePicker
 * Uses ActivityContextHolder to get current Activity
 */
actual fun createFilePicker(): FilePicker {
    return AndroidFilePicker()
}
