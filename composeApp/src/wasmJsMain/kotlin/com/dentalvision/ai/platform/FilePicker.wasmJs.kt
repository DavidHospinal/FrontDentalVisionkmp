package com.dentalvision.ai.platform

import io.github.aakira.napier.Napier
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
 * Uses Int8Array for signed byte conversion matching Kotlin's Byte type
 * Includes error handling to prevent crashes
 */
private fun arrayBufferToByteArray(buffer: ArrayBuffer): ByteArray {
    return try {
        val bufferLength = buffer.byteLength
        Napier.d("WebFilePicker: Converting ArrayBuffer of $bufferLength bytes")

        if (bufferLength == 0) {
            Napier.w("WebFilePicker: ArrayBuffer is empty")
            return ByteArray(0)
        }

        val int8Array = Int8Array(buffer)
        val arrayLength = int8Array.length

        if (arrayLength == 0) {
            Napier.w("WebFilePicker: Int8Array has length 0")
            return ByteArray(0)
        }

        ByteArray(arrayLength) { index ->
            try {
                // Int8Array values are -128 to 127 (signed), matching Kotlin Byte
                getInt8ArrayValue(int8Array, index)
            } catch (e: Exception) {
                Napier.e("WebFilePicker: Failed to get byte at index $index", e)
                0 // Return 0 as fallback
            }
        }.also {
            Napier.d("WebFilePicker: Successfully converted ArrayBuffer ($bufferLength bytes) to ByteArray (${it.size} bytes)")
        }
    } catch (e: Exception) {
        Napier.e("WebFilePicker: Failed to convert ArrayBuffer to ByteArray", e)
        throw e
    }
}

/**
 * Helper function to extract byte value from Int8Array at given index
 * Uses JS eval to avoid WASM operator overloading issues
 */
@JsFun("(array, index) => array[index]")
private external fun getInt8ArrayValue(array: Int8Array, index: Int): Byte

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
            input = try {
                (document.createElement("input") as HTMLInputElement).apply {
                    type = "file"
                    accept = "image/jpeg,image/png,image/jpg"
                    style.display = "none"
                }
            } catch (e: Exception) {
                Napier.e("WebFilePicker: Failed to create input element", e)
                return FilePickerResult.Error("Failed to create file picker: ${e.message}")
            }

            // Add to DOM temporarily
            try {
                document.body?.appendChild(input)
                    ?: run {
                        Napier.e("WebFilePicker: document.body is null")
                        return FilePickerResult.Error("Document body not available")
                    }
            } catch (e: Exception) {
                Napier.e("WebFilePicker: Failed to append input to DOM", e)
                return FilePickerResult.Error("Failed to show file picker: ${e.message}")
            }

            // Wait for file selection
            val file = try {
                suspendCoroutine<File?> { continuation ->
                    var resumed = false

                    input.onchange = {
                        if (!resumed) {
                            resumed = true
                            try {
                                val selectedFile = input.files?.item(0)
                                Napier.d("WebFilePicker: File selected - ${selectedFile?.name ?: "none"}")
                                continuation.resume(selectedFile)
                            } catch (e: Exception) {
                                Napier.e("WebFilePicker: Error getting selected file", e)
                                continuation.resume(null)
                            }
                        }
                    }

                    // Handle cancellation
                    input.oncancel = {
                        if (!resumed) {
                            resumed = true
                            Napier.d("WebFilePicker: File selection cancelled")
                            continuation.resume(null)
                        }
                    }

                    // Trigger file picker dialog
                    try {
                        Napier.d("WebFilePicker: Opening file picker dialog")
                        input.click()
                    } catch (e: Exception) {
                        Napier.e("WebFilePicker: Failed to trigger file picker", e)
                        if (!resumed) {
                            resumed = true
                            continuation.resume(null)
                        }
                    }
                }
            } catch (e: Exception) {
                Napier.e("WebFilePicker: Exception in file selection coroutine", e)
                null
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

            // Read file as ByteArray with error handling
            Napier.d("WebFilePicker: Reading file as bytes...")
            val fileBytes = try {
                readFileAsBytes(file)
            } catch (e: Exception) {
                Napier.e("WebFilePicker: Failed to read file bytes", e)
                return FilePickerResult.Error("Failed to read file: ${e.message}")
            }

            if (fileBytes.isEmpty()) {
                Napier.w("WebFilePicker: File bytes are empty")
                return FilePickerResult.Error("File is empty or could not be read")
            }

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
     * Includes comprehensive error handling to prevent UI crashes
     */
    private suspend fun readFileAsBytes(file: File): ByteArray = suspendCoroutine { continuation ->
        try {
            val reader = FileReader()
            var resumed = false

            reader.onload = onload@{
                if (!resumed) {
                    resumed = true
                    try {
                        Napier.d("WebFilePicker: FileReader onload triggered")
                        val result = reader.result

                        if (result == null) {
                            Napier.e("WebFilePicker: FileReader result is null")
                            continuation.resumeWithException(Exception("FileReader result is null"))
                            return@onload
                        }

                        val arrayBuffer = result as? ArrayBuffer
                        if (arrayBuffer == null) {
                            Napier.e("WebFilePicker: FileReader result is not an ArrayBuffer")
                            continuation.resumeWithException(Exception("FileReader result is not an ArrayBuffer"))
                            return@onload
                        }

                        val byteArray = try {
                            arrayBufferToByteArray(arrayBuffer)
                        } catch (e: Exception) {
                            Napier.e("WebFilePicker: Failed to convert ArrayBuffer to ByteArray", e)
                            continuation.resumeWithException(e)
                            return@onload
                        }

                        continuation.resume(byteArray)
                    } catch (e: Exception) {
                        Napier.e("WebFilePicker: Error in FileReader onload", e)
                        continuation.resumeWithException(e)
                    }
                }
            }

            reader.onerror = {
                if (!resumed) {
                    resumed = true
                    val error = Exception("Error reading file: ${reader.error ?: "unknown error"}")
                    Napier.e("WebFilePicker: FileReader onerror triggered", error)
                    continuation.resumeWithException(error)
                }
            }

            reader.onabort = {
                if (!resumed) {
                    resumed = true
                    val error = Exception("File reading was aborted")
                    Napier.e("WebFilePicker: FileReader onabort triggered", error)
                    continuation.resumeWithException(error)
                }
            }

            Napier.d("WebFilePicker: Starting FileReader.readAsArrayBuffer for ${file.name} (${file.size} bytes)")
            reader.readAsArrayBuffer(file)
        } catch (e: Exception) {
            Napier.e("WebFilePicker: Failed to initialize FileReader", e)
            continuation.resumeWithException(e)
        }
    }
}

/**
 * Create Web FilePicker
 */
actual fun createFilePicker(): FilePicker {
    return WebFilePicker()
}
