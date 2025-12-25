package com.dentalvision.ai.platform

import io.github.aakira.napier.Napier
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.browser.document

/**
 * Convert ArrayBuffer to ByteArray for WASM
 * FIXED: Proper implementation using Uint8Array
 */
private fun arrayBufferToByteArray(buffer: ArrayBuffer): ByteArray {
    val uint8Array = Uint8Array(buffer)
    val byteArray = ByteArray(uint8Array.length)

    for (i in 0 until uint8Array.length) {
        byteArray[i] = uint8Array[i].toByte()
    }

    Napier.d("WebFilePicker: Converted ArrayBuffer (${buffer.byteLength} bytes) to ByteArray (${byteArray.size} bytes)")
    return byteArray
}

/**
 * Web/WASM implementation of FilePicker
 * FIXED: Proper DOM cleanup and efficient ArrayBuffer conversion
 */
class WebFilePicker : FilePicker {

    override suspend fun pickImage(): FilePickerResult {
        var input: HTMLInputElement? = null

        return try {
            Napier.d("WebFilePicker: Starting file selection")

            // Create hidden file input element
            input = (document.createElement("input") as HTMLInputElement).apply {
                type = "file"
                accept = "image/jpeg,image/png,image/jpg"
                style.display = "none"
            }

            // Add to DOM temporarily
            document.body?.appendChild(input)

            // Wait for file selection
            val file = suspendCoroutine<File?> { continuation ->
                var resumed = false

                input!!.onchange = {
                    if (!resumed) {
                        resumed = true
                        val selectedFile = input.files?.item(0)
                        Napier.d("WebFilePicker: File selected - ${selectedFile?.name ?: "none"}")
                        continuation.resume(selectedFile)
                    }
                }

                // Handle cancellation
                input!!.oncancel = {
                    if (!resumed) {
                        resumed = true
                        Napier.d("WebFilePicker: File selection cancelled")
                        continuation.resume(null)
                    }
                }

                // Trigger file picker dialog
                Napier.d("WebFilePicker: Opening file picker dialog")
                input.click()
            }

            // Clean up DOM IMMEDIATELY after file selection (or cancellation)
            try {
                document.body?.removeChild(input)
                Napier.d("WebFilePicker: Input element removed from DOM")
            } catch (e: Exception) {
                Napier.w("WebFilePicker: Could not remove input from DOM (already removed?)", e)
            }

            if (file == null) {
                Napier.d("WebFilePicker: No file selected - user cancelled")
                return FilePickerResult.Cancelled
            }

            // Read file as ByteArray
            Napier.d("WebFilePicker: Reading file as bytes...")
            val fileBytes = readFileAsBytes(file)

            Napier.i("WebFilePicker: Successfully read image - ${file.name} (${fileBytes.size} bytes, ${file.type})")

            FilePickerResult.Success(
                data = fileBytes,
                name = file.name,
                mimeType = file.type.takeIf { it.isNotEmpty() } ?: "image/jpeg"
            )

        } catch (e: Exception) {
            Napier.e("WebFilePicker: Error during file selection", e)

            // Ensure cleanup even on error
            input?.let {
                try {
                    document.body?.removeChild(it)
                    Napier.d("WebFilePicker: Input element cleaned up after error")
                } catch (cleanupError: Exception) {
                    Napier.w("WebFilePicker: Could not cleanup input after error", cleanupError)
                }
            }

            FilePickerResult.Error(e.message ?: "Unknown error during file selection")
        }
    }

    /**
     * Read File as ByteArray using FileReader API
     */
    private suspend fun readFileAsBytes(file: File): ByteArray = suspendCoroutine { continuation ->
        val reader = FileReader()

        reader.onload = {
            try {
                Napier.d("WebFilePicker: FileReader onload triggered")
                val arrayBuffer = reader.result as ArrayBuffer
                val byteArray = arrayBufferToByteArray(arrayBuffer)
                continuation.resume(byteArray)
            } catch (e: Exception) {
                Napier.e("WebFilePicker: Error in FileReader onload", e)
                continuation.resumeWithException(e)
            }
        }

        reader.onerror = {
            val error = Exception("Error reading file: ${reader.error}")
            Napier.e("WebFilePicker: FileReader onerror triggered", error)
            continuation.resumeWithException(error)
        }

        Napier.d("WebFilePicker: Starting FileReader.readAsArrayBuffer")
        reader.readAsArrayBuffer(file)
    }
}

/**
 * Create Web FilePicker
 */
actual fun createFilePicker(): FilePicker {
    return WebFilePicker()
}
