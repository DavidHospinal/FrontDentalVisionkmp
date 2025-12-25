package com.dentalvision.ai.platform

/**
 * Multiplatform FilePicker interface
 * Provides file selection capabilities across Android, Desktop, and Web platforms
 */
interface FilePicker {
    /**
     * Open file picker to select an image file
     * @return FilePickerResult containing file data or error
     */
    suspend fun pickImage(): FilePickerResult
}

/**
 * Result of file picking operation
 */
sealed class FilePickerResult {
    /**
     * File successfully selected
     * @param data File bytes
     * @param name File name
     * @param mimeType MIME type (e.g., "image/png")
     */
    data class Success(
        val data: ByteArray,
        val name: String,
        val mimeType: String
    ) : FilePickerResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Success

            if (!data.contentEquals(other.data)) return false
            if (name != other.name) return false
            if (mimeType != other.mimeType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + mimeType.hashCode()
            return result
        }
    }

    /**
     * User cancelled file selection
     */
    object Cancelled : FilePickerResult()

    /**
     * Error occurred during file selection
     * @param message Error message
     */
    data class Error(val message: String) : FilePickerResult()
}

/**
 * Create platform-specific FilePicker instance
 */
expect fun createFilePicker(): FilePicker
