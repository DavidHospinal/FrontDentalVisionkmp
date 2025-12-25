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
import io.ktor.utils.io.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.text.Regex

class GradioApiClient(
    val baseUrl: String,
    val timeout: Long = 120_000L // Aumentado a 2 minutos para SSE
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
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = timeout
        }

        defaultRequest {
            header(HttpHeaders.Accept, "text/event-stream") // Header clave para SSE
            header(HttpHeaders.CacheControl, "no-cache")
        }
    }

    @Serializable
    private data class GradioEventResponse(
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
            Napier.d("Starting analysis via Gradio SSE Protocol")

            val base64Image = "data:image/jpeg;base64," + Base64.encode(imageData)

            val requestBody = buildString {
                append("{\"data\":[")
                append("\"$base64Image\",")
                append(confidenceThreshold)
                append("]}")
            }

            // 1. POST para obtener event_id
            val callUrl = "$baseUrl/gradio_api/call/predict_dental_image"

            val callResponse: HttpResponse = httpClient.post(callUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (callResponse.status.value !in 200..299) {
                val errorText = callResponse.bodyAsText()
                Napier.e("Gradio Call Failed: $errorText")
                return GradioResponse(error = "Server error: ${callResponse.status}")
            }

            val eventResponse = callResponse.body<GradioEventResponse>()
            val eventId = eventResponse.event_id ?: return GradioResponse(error = "No event_id returned")

            Napier.d("Job submitted. Event ID: $eventId. Listening for SSE stream...")

            // 2. GET Streaming para escuchar eventos
            return listenToSSE(eventId)

        } catch (e: Exception) {
            Napier.e("Gradio Error", e)
            GradioResponse(error = e.message ?: "Unknown error")
        }
    }

    private suspend fun listenToSSE(eventId: String): GradioResponse {
        val streamUrl = "$baseUrl/gradio_api/call/predict_dental_image/$eventId"

        return try {
            httpClient.prepareGet(streamUrl).execute { response ->
                val channel: ByteReadChannel = response.body()
                var finalResponse: GradioResponse? = null

                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break

                    if (line.startsWith("event: complete")) {
                        // El siguiente 'data:' contiene el resultado final
                        continue
                    }

                    if (line.startsWith("data: ")) {
                        val jsonContent = line.removePrefix("data: ").trim()

                        // Gradio envía actualizaciones de estado o el resultado final
                        // El resultado final es un array JSON
                        if (jsonContent.startsWith("[")) {
                            Napier.d("Received data array from SSE")
                            try {
                                finalResponse = parseFinalResult(jsonContent)
                                if (finalResponse.data != null) {
                                    return@execute // Salir del bloque execute y retornar
                                }
                            } catch (e: Exception) {
                                Napier.w("Failed to parse intermediate data: ${e.message}")
                            }
                        }
                    }
                }
                finalResponse ?: GradioResponse(error = "Stream closed without valid data")
            }
        } catch (e: Exception) {
            Napier.e("SSE Stream Error", e)
            GradioResponse(error = "Streaming failed: ${e.message}")
        }
    }

    private fun parseFinalResult(jsonString: String): GradioResponse {
        try {
            // El formato es un array directo: [image_info, json_string, html]
            val jsonElement = json.parseToJsonElement(jsonString)
            val dataArray = jsonElement.jsonArray

            if (dataArray.isEmpty()) {
                return GradioResponse(error = "Empty result data")
            }

            // Extract Processed Image (Index 0)
            val processedImageElement = dataArray[0]
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

            // Extract Technical Data (Index 1)
            val technicalDataString = dataArray[1].jsonPrimitive.content

            // Extract HTML Report (Index 2)
            val htmlReport = if (dataArray.size > 2) {
                dataArray[2].jsonPrimitive.contentOrNull
            } else null

            // Parse Inner JSON
            val technicalData = json.decodeFromString<GradioTechnicalData>(technicalDataString)

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

            Napier.i("Analysis successfully parsed from SSE stream!")
            return finalResponse

        } catch (e: Exception) {
            Napier.e("Parsing logic failed", e)
            throw e
        }
    }

    private fun extractRecommendations(
        htmlReport: String?,
        technicalData: GradioTechnicalData
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (htmlReport != null) {
            val liRegex = Regex("<li[^>]*>([\\s\\S]*?)</li>")
            liRegex.findAll(htmlReport).forEach { matchResult ->
                val text = matchResult.groupValues[1]
                    .replace(Regex("<[^>]*>"), "")
                    .trim()
                if (text.length > 10) recommendations.add(text)
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Análisis completado con éxito")
            if ((technicalData.summary?.cavity_count ?: 0) > 0) {
                recommendations.add("Se detectaron caries.")
                recommendations.add("Consultar con odontólogo.")
            } else {
                recommendations.add("No se detectaron caries.")
            }
        }
        return recommendations
    }

    suspend fun pollResults(eventId: String): GradioResponse {
        return GradioResponse(error = "Deprecated manual polling")
    }

    fun close() {
        httpClient.close()
    }
}