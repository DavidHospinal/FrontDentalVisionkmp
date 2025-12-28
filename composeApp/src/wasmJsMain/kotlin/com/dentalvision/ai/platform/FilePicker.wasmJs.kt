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
 * ULTRA-SAFE: Comprehensive error handling to prevent UI crashes
 */
class WebFilePicker : FilePicker {

    override suspend fun pickImage(): FilePickerResult {
        Napier.d("WebFilePicker: Starting file selection (WASM)")

        // MASTER TRY-CATCH: No exceptions should escape this function
        return try {
            // Step 1: Verify document.body exists
            val body = document.body
            if (body == null) {
                Napier.e("WebFilePicker: document.body is null - cannot create file picker")
                return FilePickerResult.Error("Browser environment not ready")
            }

            // Step 2: Create input element with full error handling
            val input: HTMLInputElement = try {
                val element = document.createElement("input") as? HTMLInputElement
                    ?: throw Exception("createElement did not return HTMLInputElement")

                element.apply {
                    type = "file"
                    accept = "image/jpeg,image/png,image/jpg"
                    style.display = "none"
                }
            } catch (e: Exception) {
                Napier.e("WebFilePicker: Failed to create input element", e)
                return FilePickerResult.Error("Could not create file picker (${e.message})")
            }

            // Step 3: Add to DOM with error handling
            try {
                body.appendChild(input)
                Napier.d("WebFilePicker: Input element added to DOM")
            } catch (e: Exception) {
                Napier.e("WebFilePicker: Failed to append input to DOM", e)
                return FilePickerResult.Error("Could not show file picker (${e.message})")
            }

            // Step 4: Wait for user selection with full error recovery
            val file: File? = try {
                suspendCoroutine { continuation ->
                    var resumed = false

                    // Success handler
                    input.onchange = {
                        if (!resumed) {
                            resumed = true
                            try {
                                val selectedFile = input.files?.item(0)
                                Napier.d("WebFilePicker: File selected - ${selectedFile?.name ?: "none"}")
                                continuation.resume(selectedFile)
                            } catch (e: Exception) {
                                Napier.e("WebFilePicker: Error in onchange handler", e)
                                continuation.resume(null)
                            }
                        }
                    }

                    // Cancel handler
                    input.oncancel = {
                        if (!resumed) {
                            resumed = true
                            Napier.d("WebFilePicker: File selection cancelled by user")
                            continuation.resume(null)
                        }
                    }

                    // Trigger picker dialog
                    try {
                        input.click()
                        Napier.d("WebFilePicker: File picker dialog opened")
                    } catch (e: Exception) {
                        Napier.e("WebFilePicker: Failed to trigger file picker", e)
                        if (!resumed) {
                            resumed = true
                            continuation.resume(null)
                        }
                    }
                }
            } catch (e: Exception) {
                Napier.e("WebFilePicker: Coroutine exception during file selection", e)
                null
            } finally {
                // Step 5: ALWAYS cleanup DOM (even on error)
                try {
                    body.removeChild(input)
                    Napier.d("WebFilePicker: Input element cleaned up from DOM")
                } catch (e: Exception) {
                    Napier.w("WebFilePicker: Could not cleanup input element", e)
                }
            }

            // Step 6: Process selection result
            if (file == null) {
                Napier.d("WebFilePicker: No file selected (user cancelled or error)")
                return FilePickerResult.Cancelled
            }

            // Step 7: Read file with comprehensive error handling
            Napier.d("WebFilePicker: Reading file '${file.name}' (${file.size} bytes)")
            val fileBytes = try {
                readFileAsBytes(file)
            } catch (e: Exception) {
                Napier.e("WebFilePicker: Failed to read file data", e)
                return FilePickerResult.Error("Could not read file: ${e.message ?: "unknown error"}")
            }

            // Step 8: Validate result
            if (fileBytes.isEmpty()) {
                Napier.w("WebFilePicker: File read resulted in empty ByteArray")
                return FilePickerResult.Error("File is empty or could not be read")
            }

            // Step 9: Success!
            val mimeType = file.type.takeIf { it.isNotEmpty() } ?: "image/jpeg"
            Napier.i("WebFilePicker: SUCCESS - Read ${file.name} (${fileBytes.size} bytes, $mimeType)")

            FilePickerResult.Success(
                data = fileBytes,
                name = file.name,
                mimeType = mimeType
            )

        } catch (e: Exception) {
            // ULTIMATE FALLBACK: Log and return error instead of crashing
            Napier.e("WebFilePicker: CRITICAL - Unhandled exception in pickImage()", e)
            FilePickerResult.Error("Critical error: ${e.message ?: "Unknown failure"}")
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
