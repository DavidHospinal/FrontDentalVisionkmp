package com.dentalvision.ai.data.remote.gradio

import io.github.aakira.napier.Napier
import io.ktor.client.*
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
    val timeout: Long = 180_000L // 3 minutos para mayor seguridad
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
            header(HttpHeaders.Accept, "text/event-stream")
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
            Napier.d("Starting analysis via Gradio SSE Protocol for: $imageName")

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

            val responseBodyText = callResponse.bodyAsText()
            val eventResponse = json.decodeFromString<GradioEventResponse>(responseBodyText)
            val eventId = eventResponse.event_id

            if (eventId == null) {
                return GradioResponse(error = "No event_id returned")
            }

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
            val result: GradioResponse = httpClient.prepareGet(streamUrl).execute { response ->
                val channel: ByteReadChannel = response.bodyAsChannel()
                var finalResponse: GradioResponse? = null

                // Bucle de lectura del stream
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break

                    if (line.isBlank()) continue // Ignorar líneas vacías (heartbeats)

                    if (line.startsWith("event: complete")) {
                        // El proceso terminó, esperamos que ya hayamos recibido los datos o vengan en la siguiente línea
                        Napier.d("SSE Event: Complete signal received")
                        continue
                    }

                    if (line.startsWith("data: ")) {
                        val jsonContent = line.removePrefix("data: ").trim()

                        // Gradio 4 envía un array [...] con los resultados finales.
                        // A veces envía actualizaciones de estado que son objetos {...}.
                        // Solo nos interesa el array final.
                        if (jsonContent.startsWith("[")) {
                            Napier.d("Received data array from SSE")
                            try {
                                finalResponse = parseFinalResult(jsonContent)
                                if (finalResponse?.data != null) {
                                    // ÉXITO: Tenemos los datos, cerramos y retornamos
                                    return@execute finalResponse!!
                                }
                            } catch (e: Exception) {
                                Napier.w("Failed to parse intermediate data array: ${e.message}")
                            }
                        } else {
                            // Es un mensaje de estado o heartbeat, lo logueamos pero seguimos esperando
                            // Napier.v("SSE Status update: $jsonContent")
                        }
                    }
                }

                // Si salimos del bucle sin respuesta válida
                finalResponse ?: GradioResponse(error = "Stream closed without valid data")
            }
            result
        } catch (e: Exception) {
            Napier.e("SSE Stream Error", e)
            GradioResponse(error = "Streaming failed: ${e.message}")
        }
    }

    private fun parseFinalResult(jsonString: String): GradioResponse {
        try {
            val jsonElement = json.parseToJsonElement(jsonString)
            val dataArray = jsonElement.jsonArray

            if (dataArray.isEmpty()) {
                return GradioResponse(error = "Empty result data")
            }

            // Validar que el array tenga la estructura esperada [image, json_str, html]
            if (dataArray.size < 2) {
                // Puede ser un mensaje parcial, retornamos null para seguir esperando
                throw Exception("Incomplete data array")
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

            // Extract Technical Data (Index 1) - ESTO ES CRÍTICO
            // A veces Gradio envía null si falló, validar.
            val technicalDataElement = dataArray[1]
            if (technicalDataElement is JsonNull) {
                throw Exception("Technical data is null")
            }

            val technicalDataString = technicalDataElement.jsonPrimitive.content

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
            // Re-lanzar para que el bucle siga intentando si es un error de formato temporal
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

    fun close() {
        httpClient.close()
    }
}