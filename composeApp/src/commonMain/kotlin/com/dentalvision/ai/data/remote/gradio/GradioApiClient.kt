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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.text.Regex

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
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = timeout
        }

        defaultRequest {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }
    }

    @Serializable
    private data class GradioRawResponse(
        val data: JsonArray? = null,
        val error: String? = null,
        val event_id: String? = null
    )

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

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun analyzeDentalImage(
        imageData: ByteArray,
        imageName: String,
        confidenceThreshold: Double = 0.5
    ): GradioResponse {
        return try {
            Napier.d("Submitting dental image to Gradio API: $imageName (${imageData.size} bytes)")

            val base64Image = "data:image/jpeg;base64," + Base64.encode(imageData)

            val requestBody = buildString {
                append("{\"data\":[")
                append("\"$base64Image\",")
                append(confidenceThreshold)
                append("]}")
            }

            // URL corregida según documentación
            val targetUrl = "$baseUrl/api/predict_dental_image"
            Napier.d("REQUEST: POST $targetUrl")

            val httpResponse: HttpResponse = httpClient.post(targetUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            Napier.d("RESPONSE: ${httpResponse.status}")

            if (httpResponse.status.value !in 200..299) {
                val errorText = httpResponse.bodyAsText()
                Napier.e("Gradio server error (${httpResponse.status.value}): $errorText")
                return GradioResponse(
                    error = "Server error ${httpResponse.status.value}: ${errorText.take(200)}"
                )
            }

            val responseText = httpResponse.bodyAsText()
            val rawResponse = json.decodeFromString<GradioRawResponse>(responseText)

            if (rawResponse.error != null) {
                Napier.e("Gradio returned error: ${rawResponse.error}")
                return GradioResponse(error = rawResponse.error)
            }

            if (rawResponse.data == null || rawResponse.data.size < 2) {
                Napier.e("Invalid Gradio response - missing data")
                return GradioResponse(error = "Invalid response format from Gradio API")
            }

            val processedImageElement = rawResponse.data[0]
            val processedImageUrl = when {
                processedImageElement is JsonPrimitive && processedImageElement.isString -> {
                    val url = processedImageElement.content
                    if (url.startsWith("http") || url.startsWith("data:")) url else "$baseUrl/file=$url"
                }
                processedImageElement is JsonObject -> {
                    val url = processedImageElement["url"]?.jsonPrimitive?.content
                        ?: processedImageElement["path"]?.jsonPrimitive?.content ?: ""
                    if (url.startsWith("http")) url else "$baseUrl/file=$url"
                }
                else -> null
            }

            val technicalDataString = rawResponse.data[1].jsonPrimitive.content

            val htmlReport = if (rawResponse.data.size > 2) {
                rawResponse.data[2].jsonPrimitive.contentOrNull
            } else null

            val technicalData = json.decodeFromString<GradioTechnicalData>(technicalDataString)
            Napier.i("Successfully parsed data: ${technicalData.detections.size} detections")

            val finalResponse = GradioResponse(
                eventId = null,
                data = listOf(
                    GradioDataItem(
                        image = processedImageUrl,
                        detections = technicalDataString,
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

            Napier.i("Analysis completed. Found ${finalResponse.data?.first()?.summary?.cariesDetected} caries.")
            finalResponse

        } catch (e: Exception) {
            Napier.e("Gradio API error", e)
            GradioResponse(error = e.message ?: "Unknown error")
        }
    }

    private fun extractRecommendations(
        htmlReport: String?,
        technicalData: GradioTechnicalData
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (htmlReport != null) {
            // Regex compatible con KMP para extraer contenido de listas
            val liRegex = Regex("<li[^>]*>([\\s\\S]*?)</li>")

            liRegex.findAll(htmlReport).forEach { matchResult ->
                val text = matchResult.groupValues[1]
                    .replace(Regex("<[^>]*>"), "")
                    .trim()

                if (text.length > 10) {
                    recommendations.add(text)
                }
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Análisis completado con éxito")
            if ((technicalData.summary?.cavity_count ?: 0) > 0) {
                recommendations.add("Se detectaron caries que requieren atención")
                recommendations.add("Consultar con odontólogo")
            } else {
                recommendations.add("No se detectaron caries")
            }
        }
        return recommendations
    }

    suspend fun pollResults(eventId: String): GradioResponse {
        return GradioResponse(error = "Polling not supported in sync mode")
    }

    fun close() {
        httpClient.close()
    }
}