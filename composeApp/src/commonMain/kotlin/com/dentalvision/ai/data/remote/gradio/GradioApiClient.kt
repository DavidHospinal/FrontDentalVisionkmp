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
import kotlinx.coroutines.delay
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
    private data class GradioEventResponse(
        val event_id: String? = null
    )

    @Serializable
    private data class GradioPollResponse(
        val msg: String? = null,
        val event_id: String? = null,
        val output: GradioRawData? = null,
        val success: Boolean? = true
    )

    @Serializable
    private data class GradioRawData(
        val data: JsonArray? = null,
        val error: String? = null
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
            Napier.d("Starting analysis via Gradio 4.x Protocol")

            // 1. Convert Image
            val base64Image = "data:image/jpeg;base64," + Base64.encode(imageData)

            val requestBody = buildString {
                append("{\"data\":[")
                append("\"$base64Image\",")
                append(confidenceThreshold)
                append("]}")
            }

            // 2. Submit Job (Async)
            // Usamos el endpoint /call/ que es el estándar en Gradio 4+ cuando /api/ falla
            val callUrl = "$baseUrl/gradio_api/call/predict_dental_image"
            Napier.d("POST $callUrl")

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
            val eventId = eventResponse.event_id

            if (eventId == null) {
                return GradioResponse(error = "No event_id returned from server")
            }

            Napier.d("Job submitted. Event ID: $eventId. Polling for results...")

            // 3. Poll for Results
            return pollForResults(eventId)

        } catch (e: Exception) {
            Napier.e("Gradio Error", e)
            GradioResponse(error = e.message ?: "Unknown error")
        }
    }

    private suspend fun pollForResults(eventId: String): GradioResponse {
        val pollUrl = "$baseUrl/gradio_api/call/predict_dental_image/$eventId"
        var attempts = 0
        val maxAttempts = 40 // 40 * 1s = 40s max wait

        while (attempts < maxAttempts) {
            attempts++
            delay(1000) // Wait 1s between checks

            try {
                // Gradio 4 streaming response is line-delimited JSON.
                // We simplify by getting the raw text and parsing the last relevant line manually if needed,
                // or just reading the body as normal JSON if the server returns standard polling responses.
                // Ktor Client might throw error on streaming response if not handled as Stream.
                // For simplicity in KMP, we assume standard GET request behavior.

                val response: HttpResponse = httpClient.get(pollUrl)
                val responseText = response.bodyAsText()

                // Check for "process_completed" message in the response text
                if (responseText.contains("process_completed")) {
                    Napier.d("Process completed signal found")

                    // The response might be SSE format "event: ... data: ...".
                    // We need to extract the JSON data line.
                    // Simple hack: Look for the JSON object containing "output"

                    // Parse text line by line to find the JSON with data
                    val lines = responseText.lines()
                    for (line in lines) {
                        if (line.startsWith("data: ")) {
                            val jsonContent = line.removePrefix("data: ")
                            try {
                                // Try to parse as array (Gradio 4 output format often: [event_id, data_object])
                                // Or check if it matches our structure
                                if (jsonContent.contains("\"data\":")) {
                                    // Found our data!
                                    return parseFinalResult(jsonContent)
                                }
                            } catch (e: Exception) {
                                // Continue searching
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Napier.w("Polling attempt $attempts failed: ${e.message}")
            }
        }

        return GradioResponse(error = "Analysis timed out after ${maxAttempts}s")
    }

    private fun parseFinalResult(jsonString: String): GradioResponse {
        try {
            // Gradio 4 SSE data line is typically just the array of outputs or an object wrapper.
            // Let's try to parse as our PollResponse structure first logic manually
            // We look for the "data" array in the string

            // Extract the 'data' array manually if needed, or decode
            val jsonElement = json.parseToJsonElement(jsonString)

            // Navigate to output -> data
            val dataArray = if (jsonElement is JsonArray) {
                // Sometimes it returns [event_id, {output...}]
                jsonElement.lastOrNull()?.jsonObject?.get("output")?.jsonObject?.get("data")?.jsonArray
            } else {
                jsonElement.jsonObject["output"]?.jsonObject?.get("data")?.jsonArray
            }

            if (dataArray == null || dataArray.size < 2) {
                return GradioResponse(error = "Result data missing in server response")
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

            Napier.i("Analysis successfully parsed via polling!")
            return finalResponse

        } catch (e: Exception) {
            Napier.e("Parsing final result failed", e)
            return GradioResponse(error = "Failed to parse analysis results: ${e.message}")
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