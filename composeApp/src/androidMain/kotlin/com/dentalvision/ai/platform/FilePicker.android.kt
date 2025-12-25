package com.dentalvision.ai.platform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import java.io.InputStream

/**
 * Android implementation of FilePicker
 * Uses Android Activity Result API for image selection
 */
class AndroidFilePicker(
    private val activity: ComponentActivity
) : FilePicker {

    private val filePickerDeferred = CompletableDeferred<FilePickerResult>()

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedImage(uri)
            } ?: run {
                filePickerDeferred.complete(FilePickerResult.Cancelled)
            }
        } else {
            filePickerDeferred.complete(FilePickerResult.Cancelled)
        }
    }

    override suspend fun pickImage(): FilePickerResult {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/jpg"))
            }

            launcher.launch(intent)
            return filePickerDeferred.await()

        } catch (e: Exception) {
            Napier.e("Error picking image on Android", e)
            return FilePickerResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            val contentResolver = activity.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)

            if (inputStream == null) {
                filePickerDeferred.complete(
                    FilePickerResult.Error("Could not open file")
                )
                return
            }

            val fileBytes = inputStream.readBytes()
            inputStream.close()

            // Get file name
            val fileName = getFileName(uri) ?: "image.jpg"

            // Get MIME type
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

            Napier.d("Image selected: $fileName (${fileBytes.size} bytes, $mimeType)")

            filePickerDeferred.complete(
                FilePickerResult.Success(
                    data = fileBytes,
                    name = fileName,
                    mimeType = mimeType
                )
            )

        } catch (e: Exception) {
            Napier.e("Error reading selected image", e)
            filePickerDeferred.complete(
                FilePickerResult.Error(e.message ?: "Error reading file")
            )
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = activity.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && it.moveToFirst()) {
                it.getString(nameIndex)
            } else null
        }
    }
}

/**
 * Create Android FilePicker
 * Note: Requires Activity context to be provided
 */
actual fun createFilePicker(): FilePicker {
    // TODO: Inject activity context properly (via DI or context holder)
    // For now, this is a placeholder
    throw IllegalStateException("AndroidFilePicker requires Activity context. Use AndroidFilePicker(activity) directly.")
}
