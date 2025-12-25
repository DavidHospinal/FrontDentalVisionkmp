package com.dentalvision.ai.data.remote.gradio

import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json

/**
 * Specialized API client for HuggingFace Gradio Spaces
 * Handles the unique Gradio event-based API protocol
 *
 * Gradio API Flow:
 * 1. POST to /gradio_api/call/predict_dental_image with image data
 * 2. Receive event_id in response
 * 3. Long-poll GET to /gradio_api/call/predict_dental_image/{event_id} for results
 *    OR receive direct JSON response (depends on Gradio version)
 *
 * @property baseUrl HuggingFace Space URL (e.g., https://davidhosp-dental-vision-yolo12.hf.space)
 * @property timeout Request timeout in milliseconds (default: 60 seconds for AI processing)
 */
class GradioApiClient(
    val baseUrl: String,
    val timeout: Long = 60_000L
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
                encodeDefaults = true
                explicitNulls = false
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.d(message, tag = "GradioApiClient")
                }
            }
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = timeout
            connectTimeoutMillis = 15_000  // 15 seconds to establish connection
            socketTimeoutMillis = timeout
        }

        defaultRequest {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }
    }

    /**
     * Submit dental image for AI analysis
     * Uses multipart form data to upload image
     *
     * @param imageData Image file bytes
     * @param imageName Image filename (e.g., "dental_scan.png")
     * @param confidenceThreshold Detection confidence threshold (0.0 to 1.0)
     * @return GradioResponse containing analysis results or event_id for polling
     */
    suspend fun analyzeDentalImage(
        imageData: ByteArray,
        imageName: String,
        confidenceThreshold: Double = 0.5
    ): GradioResponse {
        return try {
            Napier.d("Submitting dental image to Gradio API: $imageName (${imageData.size} bytes)")

            val response: GradioResponse = httpClient.submitForm(
                url = "$baseUrl/gradio_api/call/predict_dental_image",
                formParameters = Parameters.build {
                    append("confidence_threshold", confidenceThreshold.toString())
                }
            ) {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("file", imageData, Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=\"$imageName\"")
                        })
                    }
                ))
            }.body()

            Napier.i("Gradio API response received: ${response.event_id ?: "direct result"}")
            response
        } catch (e: Exception) {
            Napier.e("Gradio API error", e)
            GradioResponse(
                error = e.message ?: "Unknown error during AI analysis"
            )
        }
    }

    /**
     * Poll for analysis results using event_id (if Gradio uses event-based protocol)
     *
     * @param eventId Event ID from initial submission
     * @return GradioResponse with analysis results
     */
    suspend fun pollResults(eventId: String): GradioResponse {
        return try {
            Napier.d("Polling Gradio results for event: $eventId")

            val response: GradioResponse = httpClient.get(
                "$baseUrl/gradio_api/call/predict_dental_image/$eventId"
            ).body()

            Napier.i("Gradio polling response received")
            response
        } catch (e: Exception) {
            Napier.e("Gradio polling error", e)
            GradioResponse(
                error = e.message ?: "Unknown error during result polling"
            )
        }
    }

    /**
     * Close the HTTP client and release resources
     */
    fun close() {
        httpClient.close()
    }
}
