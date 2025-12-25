package com.dentalvision.ai.data.remote.gradio

import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Specialized API client for HuggingFace Gradio Spaces
 * Handles the unique Gradio event-based API protocol
 *
 * IMPORTANT - Gradio Response Structure (Double Parse):
 * Gradio returns: { "data": [processedImage, jsonString, htmlReport] }
 * Where:
 * - data[0] = Processed image (base64 or {url: "..."})
 * - data[1] = JSON AS STRING containing actual detections/summary
 * - data[2] = HTML report (optional)
 *
 * We need to:
 * 1. Parse outer JSON to get the "data" array
 * 2. Extract data[1] as String
 * 3. Parse that String as JSON to get actual results
 *
 * @property baseUrl HuggingFace Space URL (e.g., https://davidhosp-dental-vision-yolo12.hf.space)
 * @property timeout Request timeout in milliseconds (default: 60 seconds for AI processing)
 */
class GradioApiClient(
    val baseUrl: String,
    val timeout: Long = 60_000L
) {
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
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
     * Raw Gradio response wrapper (outer JSON)
     */
    @Serializable
    private data class GradioRawResponse(
        val data: JsonArray? = null,
        val error: String? = null,
        val event_id: String? = null
    )

    /**
     * Technical data extracted from data[1] string (inner JSON)
     */
    @Serializable
    private data class GradioTechnicalData(
        val detections: List<GradioDetection> = emptyList(),
        val summary: GradioInnerSummary? = null
    )

    @Serializable
    private data class GradioInnerSummary(
        val total_teeth_detected: Int = 0,
        val healthy_count: Int = 0,
        val cavity_count: Int = 0,
        val average_confidence: Double = 0.0
    )

    /**
     * Submit dental image for AI analysis
     * Uses Gradio's JSON protocol with Base64-encoded image
     *
     * @param imageData Image file bytes
     * @param imageName Image filename (for logging purposes)
     * @param confidenceThreshold Detection confidence threshold (0.0 to 1.0)
     * @return GradioResponse containing analysis results
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun analyzeDentalImage(
        imageData: ByteArray,
        imageName: String,
        confidenceThreshold: Double = 0.5
    ): GradioResponse {
        return try {
            Napier.d("🚀 Submitting dental image to Gradio API: $imageName (${imageData.size} bytes)")

            // Step 1: Convert image to Base64 with data URI prefix
            val base64Image = "data:image/jpeg;base64," + Base64.encode(imageData)
            Napier.d("📸 Image converted to Base64 (length: ${base64Image.length} chars)")

            // Step 2: Build Gradio request with proper format
            val requestBody = buildString {
                append("{\"data\":[")
                append("\"$base64Image\",")
                append(confidenceThreshold)
                append("]}")
            }

            Napier.d("📤 REQUEST: POST $baseUrl/api/predict")
            Napier.d("📊 Confidence threshold: $confidenceThreshold")

            // Step 3: Send POST request and capture HttpResponse FIRST
            val httpResponse: HttpResponse = httpClient.post("$baseUrl/api/predict") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            Napier.d("📥 RESPONSE: ${httpResponse.status}")
            Napier.d("📋 Content-Type: ${httpResponse.contentType()}")

            // Step 4: Check status code BEFORE parsing
            if (httpResponse.status.value !in 200..299) {
                // Server error - read as text to see Python stacktrace
                val errorText = httpResponse.bodyAsText()
                Napier.e("❌ Gradio server error (${httpResponse.status.value}):\n$errorText")

                return GradioResponse(
                    error = "Server error ${httpResponse.status.value}: ${errorText.take(200)}"
                )
            }

            // Step 5: DOUBLE PARSE - First get the raw response as String
            val responseText = httpResponse.bodyAsText()
            Napier.d("📄 Raw response text (first 500 chars): ${responseText.take(500)}")

            // Step 6: Parse outer JSON to get the "data" array
            val rawResponse = json.decodeFromString<GradioRawResponse>(responseText)
            Napier.d("🔍 Parsed outer JSON - has data: ${rawResponse.data != null}, error: ${rawResponse.error}")

            if (rawResponse.error != null) {
                Napier.e("❌ Gradio returned error: ${rawResponse.error}")
                return GradioResponse(error = rawResponse.error)
            }

            if (rawResponse.data == null || rawResponse.data.size < 2) {
                Napier.e("❌ Invalid Gradio response - missing data array or insufficient elements")
                return GradioResponse(error = "Invalid response format from Gradio API")
            }

            // Step 7: Extract data[0] = processed image
            val processedImageElement = rawResponse.data[0]
            val processedImageUrl = when {
                processedImageElement is JsonPrimitive && processedImageElement.isString -> {
                    val url = processedImageElement.content
                    if (url.startsWith("http") || url.startsWith("data:")) {
                        url
                    } else {
                        "$baseUrl/file=$url"
                    }
                }
                processedImageElement is JsonObject && processedImageElement.containsKey("url") -> {
                    val url = processedImageElement["url"]?.jsonPrimitive?.content ?: ""
                    if (url.startsWith("http")) url else "$baseUrl$url"
                }
                processedImageElement is JsonObject && processedImageElement.containsKey("path") -> {
                    val path = processedImageElement["path"]?.jsonPrimitive?.content ?: ""
                    "$baseUrl/file=$path"
                }
                else -> null
            }

            Napier.d("🖼️ Processed image URL: $processedImageUrl")

            // Step 8: Extract data[1] = JSON STRING containing actual results
            val technicalDataString = rawResponse.data[1].jsonPrimitive.content
            Napier.d("📊 Technical data string (first 300 chars): ${technicalDataString.take(300)}")

            // Step 9: SECOND PARSE - Parse the inner JSON string
            val technicalData = json.decodeFromString<GradioTechnicalData>(technicalDataString)
            Napier.i("✅ Successfully parsed technical data - ${technicalData.detections.size} detections found")

            // Step 10: Extract HTML report (optional - data[2])
            val htmlReport = if (rawResponse.data.size > 2) {
                rawResponse.data[2].jsonPrimitive.contentOrNull
            } else null

            // Step 11: Transform to our GradioResponse format
            val transformedDetections = technicalData.detections.map { detection ->
                GradioDataItem(
                    image = null, // Not used in this context
                    detections = null, // Will use the summary instead
                    summary = null
                )
            }

            // Step 12: Build final response
            val finalResponse = GradioResponse(
                eventId = null,
                data = listOf(
                    GradioDataItem(
                        image = processedImageUrl,
                        detections = technicalDataString, // Store raw JSON for downstream parsing
                        summary = GradioSummary(
                            totalDetections = technicalData.summary?.total_teeth_detected ?: technicalData.detections.size,
                            healthyTeeth = technicalData.summary?.healthy_count ?: 0,
                            cariesDetected = technicalData.summary?.cavity_count ?: 0,
                            averageConfidence = technicalData.summary?.average_confidence ?: 0.0,
                            severityDistribution = null,
                            recommendations = extractRecommendations(htmlReport, technicalData)
                        )
                    )
                ),
                error = null
            )

            Napier.i("🎉 Analysis completed successfully!")
            Napier.i("📊 Results: ${finalResponse.data?.first()?.summary?.totalDetections} teeth, ${finalResponse.data?.first()?.summary?.cariesDetected} caries")

            finalResponse

        } catch (e: Exception) {
            Napier.e("💥 Gradio API error", e)
            GradioResponse(
                error = e.message ?: "Unknown error during AI analysis"
            )
        }
    }

    /**
     * Extract recommendations from HTML report or generate defaults
     */
    private fun extractRecommendations(
        htmlReport: String?,
        technicalData: GradioTechnicalData
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Try to extract from HTML if available
        if (htmlReport != null) {
            val liMatches = Regex("<li[^>]*>(.*?)</li>", RegexOption.DOT_MATCHES_ALL)
                .findAll(htmlReport)

            liMatches.forEach { match ->
                val text = match.groupValues[1]
                    .replace(Regex("<[^>]*>"), "") // Remove HTML tags
                    .trim()
                if (text.length > 10) {
                    recommendations.add(text)
                }
            }
        }

        // Fallback recommendations if none found
        if (recommendations.isEmpty()) {
            recommendations.add("Análisis completado con éxito")

            if (technicalData.summary?.cavity_count ?: 0 > 0) {
                recommendations.add("Se detectaron ${technicalData.summary?.cavity_count} caries que requieren atención")
                recommendations.add("Consultar con profesional odontológico para tratamiento")
            } else {
                recommendations.add("No se detectaron caries en la imagen analizada")
                recommendations.add("Mantener higiene dental regular")
            }

            recommendations.add("Revisar detecciones marcadas en la imagen procesada")
        }

        return recommendations
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
                "$baseUrl/api/predict/$eventId"
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
