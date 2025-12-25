package com.dentalvision.ai.platform

import io.github.aakira.napier.Napier
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
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
 * TODO: This is a stub implementation. WASM file picking requires proper JS interop.
 * For now, returns empty array to allow compilation.
 */
private fun arrayBufferToByteArray(buffer: ArrayBuffer): ByteArray {
    Napier.w("WASM FilePicker: arrayBufferToByteArray is not fully implemented")
    // Stub implementation - returns empty array
    // Proper implementation requires external JS functions or different approach
    return ByteArray(0)
}

/**
 * Web/WASM implementation of FilePicker
 * Uses HTML5 File Input API for file selection
 */
class WebFilePicker : FilePicker {

    override suspend fun pickImage(): FilePickerResult {
        return try {
            // Create hidden file input element
            val input = (document.createElement("input") as HTMLInputElement).apply {
                type = "file"
                accept = "image/*"
                style.display = "none"
            }

            // Add to DOM temporarily
            document.body?.appendChild(input)

            // Wait for file selection
            val file = suspendCoroutine<File?> { continuation ->
                input.onchange = {
                    val selectedFile = input.files?.item(0)
                    document.body?.removeChild(input)
                    continuation.resume(selectedFile)
                }

                // Handle cancellation
                input.oncancel = {
                    document.body?.removeChild(input)
                    continuation.resume(null)
                }

                // Trigger file picker dialog
                input.click()
            }

            if (file == null) {
                Napier.d("File selection cancelled by user")
                return FilePickerResult.Cancelled
            }

            // Read file as ByteArray
            val fileBytes = readFileAsBytes(file)

            Napier.d("Image selected: ${file.name} (${fileBytes.size} bytes, ${file.type})")

            FilePickerResult.Success(
                data = fileBytes,
                name = file.name,
                mimeType = file.type.takeIf { it.isNotEmpty() } ?: "image/jpeg"
            )

        } catch (e: Exception) {
            Napier.e("Error picking image on Web", e)
            FilePickerResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Read File as ByteArray using FileReader API
     */
    private suspend fun readFileAsBytes(file: File): ByteArray = suspendCoroutine { continuation ->
        val reader = FileReader()

        reader.onload = {
            try {
                val arrayBuffer = reader.result as ArrayBuffer
                val byteArray = arrayBufferToByteArray(arrayBuffer)
                continuation.resume(byteArray)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

        reader.onerror = {
            continuation.resumeWithException(
                Exception("Error reading file")
            )
        }

        reader.readAsArrayBuffer(file)
    }
}

/**
 * Create Web FilePicker
 */
actual fun createFilePicker(): FilePicker {
    return WebFilePicker()
}
