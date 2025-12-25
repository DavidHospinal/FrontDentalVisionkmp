package com.dentalvision.ai.platform

import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop (JVM) implementation of FilePicker
 * Uses Swing JFileChooser for file selection
 */
class DesktopFilePicker : FilePicker {

    override suspend fun pickImage(): FilePickerResult = withContext(Dispatchers.IO) {
        try {
            val fileChooser = JFileChooser().apply {
                dialogTitle = "Select Dental Image"
                fileSelectionMode = JFileChooser.FILES_ONLY
                isMultiSelectionEnabled = false

                // Add image file filter
                val imageFilter = FileNameExtensionFilter(
                    "Image Files (*.jpg, *.jpeg, *.png)",
                    "jpg", "jpeg", "png", "JPG", "JPEG", "PNG"
                )
                fileFilter = imageFilter
            }

            val result = fileChooser.showOpenDialog(null)

            if (result == JFileChooser.APPROVE_OPTION) {
                val selectedFile: File = fileChooser.selectedFile

                if (!selectedFile.exists()) {
                    return@withContext FilePickerResult.Error("Selected file does not exist")
                }

                val fileBytes = selectedFile.readBytes()
                val fileName = selectedFile.name
                val mimeType = when (selectedFile.extension.lowercase()) {
                    "png" -> "image/png"
                    "jpg", "jpeg" -> "image/jpeg"
                    else -> "image/jpeg"
                }

                Napier.d("Image selected: $fileName (${fileBytes.size} bytes, $mimeType)")

                FilePickerResult.Success(
                    data = fileBytes,
                    name = fileName,
                    mimeType = mimeType
                )
            } else {
                Napier.d("File selection cancelled by user")
                FilePickerResult.Cancelled
            }

        } catch (e: Exception) {
            Napier.e("Error picking image on Desktop", e)
            FilePickerResult.Error(e.message ?: "Unknown error")
        }
    }
}

/**
 * Create Desktop FilePicker
 */
actual fun createFilePicker(): FilePicker {
    return DesktopFilePicker()
}
