package com.dentalvision.ai.platform

import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

/**
 * Desktop (JVM) implementation of FilePicker
 * Uses native AWT FileDialog for OS-native file selection experience
 */
class DesktopFilePicker : FilePicker {

    override suspend fun pickImage(): FilePickerResult = withContext(Dispatchers.IO) {
        try {
            Napier.d("DesktopFilePicker: Opening native file dialog")

            // Create FileDialog with image filter (OS-native dialog)
            val fileDialog = FileDialog(null as Frame?, "Select Dental Image", FileDialog.LOAD).apply {
                // Set file filter for images
                filenameFilter = FilenameFilter { _, name ->
                    val lowercaseName = name.lowercase()
                    lowercaseName.endsWith(".jpg") ||
                    lowercaseName.endsWith(".jpeg") ||
                    lowercaseName.endsWith(".png")
                }

                // Set initial directory to user's Pictures folder if available
                val picturesDir = System.getProperty("user.home")?.let { home ->
                    File(home, "Pictures").takeIf { it.exists() }
                }
                if (picturesDir != null) {
                    directory = picturesDir.absolutePath
                }
            }

            // Show dialog (blocks until user selects or cancels)
            fileDialog.isVisible = true

            // Get selected file
            val selectedFileName = fileDialog.file
            val selectedDirectory = fileDialog.directory

            // Check if user cancelled
            if (selectedFileName == null || selectedDirectory == null) {
                Napier.d("DesktopFilePicker: File selection cancelled by user")
                return@withContext FilePickerResult.Cancelled
            }

            // Build full file path
            val selectedFile = File(selectedDirectory, selectedFileName)

            // Validate file exists
            if (!selectedFile.exists()) {
                Napier.e("DesktopFilePicker: Selected file does not exist: ${selectedFile.absolutePath}")
                return@withContext FilePickerResult.Error("Selected file does not exist")
            }

            // Validate file is readable
            if (!selectedFile.canRead()) {
                Napier.e("DesktopFilePicker: Cannot read selected file: ${selectedFile.absolutePath}")
                return@withContext FilePickerResult.Error("Cannot read selected file")
            }

            // Read file bytes
            val fileBytes = try {
                selectedFile.readBytes()
            } catch (e: Exception) {
                Napier.e("DesktopFilePicker: Failed to read file bytes", e)
                return@withContext FilePickerResult.Error("Failed to read file: ${e.message}")
            }

            // Determine MIME type from extension
            val mimeType = when (selectedFile.extension.lowercase()) {
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                else -> "image/jpeg" // Default fallback
            }

            Napier.i("DesktopFilePicker: SUCCESS - Selected ${selectedFile.name} (${fileBytes.size} bytes, $mimeType)")

            FilePickerResult.Success(
                data = fileBytes,
                name = selectedFile.name,
                mimeType = mimeType
            )

        } catch (e: Exception) {
            Napier.e("DesktopFilePicker: CRITICAL - Error picking image", e)
            FilePickerResult.Error(e.message ?: "Unknown error during file selection")
        }
    }
}

/**
 * Create Desktop FilePicker
 */
actual fun createFilePicker(): FilePicker {
    return DesktopFilePicker()
}
