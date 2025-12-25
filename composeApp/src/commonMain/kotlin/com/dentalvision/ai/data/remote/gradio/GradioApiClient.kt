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
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Cliente API para HuggingFace Gradio Space personalizado
 * Usa endpoints especificos del deployment davidhosp-dental-vision-yolo12
 */
class GradioApiClient(
    val baseUrl: String,
    val timeout: Long = 180_000L
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
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }
    }

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
            Napier.d("[TEST-LOG] Iniciando analisis de imagen: $imageName (${imageData.size} bytes)")

            val base64Image = "data:image/jpeg;base64," + Base64.encode(imageData)
            Napier.d("[TEST-LOG] Imagen convertida a Base64 (longitud: ${base64Image.length} chars)")

            val requestBody = buildString {
                append("{\"data\":[")
                append("\"$base64Image\",")
                append(confidenceThreshold)
                append("]}")
            }

            // Paso 1: POST para obtener event_id (endpoint correcto segun documentacion)
            val callUrl = "$baseUrl/gradio_api/call/predict_dental_image"
            Napier.d("[TEST-LOG] POST URL: $callUrl")

            val callResponse: HttpResponse = httpClient.post(callUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            Napier.d("[TEST-LOG] POST Response status: ${callResponse.status}")

            if (callResponse.status.value !in 200..299) {
                val errorText = callResponse.bodyAsText()
                Napier.e("[TEST-LOG] POST fallo con status ${callResponse.status.value}: $errorText")
                return GradioResponse(error = "Error iniciando analisis: ${callResponse.status}")
            }

            val callResponseText = callResponse.bodyAsText()
            Napier.d("[TEST-LOG] POST response body: $callResponseText")

            // Extraer event_id de la respuesta
            val eventId = try {
                val jsonResponse = json.parseToJsonElement(callResponseText).jsonObject
                jsonResponse["event_id"]?.jsonPrimitive?.content
                    ?: throw Exception("No se encontro event_id en la respuesta")
            } catch (e: Exception) {
                Napier.e("[TEST-LOG] Error extrayendo event_id", e)
                return GradioResponse(error = "Error obteniendo event_id: ${e.message}")
            }

            Napier.i("[TEST-LOG] Event ID obtenido: $eventId")

            // Paso 2: Polling para obtener resultados usando SSE
            val pollUrl = "$baseUrl/gradio_api/call/predict_dental_image/$eventId"
            Napier.d("[TEST-LOG] Iniciando polling en: $pollUrl")

            return pollForResults(pollUrl, eventId)

        } catch (e: Exception) {
            Napier.e("[TEST-LOG] Error en analyzeDentalImage", e)
            GradioResponse(error = e.message ?: "Error desconocido")
        }
    }

    private suspend fun pollForResults(pollUrl: String, eventId: String): GradioResponse {
        val maxAttempts = 60
        var attempts = 0

        while (attempts < maxAttempts) {
            attempts++
            Napier.d("[TEST-LOG] Polling intento $attempts/$maxAttempts")

            try {
                val pollResponse: HttpResponse = httpClient.get(pollUrl)
                val responseText = pollResponse.bodyAsText()

                Napier.d("[TEST-LOG] Polling response (primeros 500 chars): ${responseText.take(500)}")

                // Parsear eventos SSE
                val lines = responseText.lines()
                for (line in lines) {
                    if (line.startsWith("event: complete")) {
                        Napier.i("[TEST-LOG] Evento 'complete' recibido")
                        // Buscar la linea con los datos
                        val dataLine = lines.find { it.startsWith("data: ") }
                        if (dataLine != null) {
                            val jsonData = dataLine.removePrefix("data: ")
                            Napier.d("[TEST-LOG] Datos del evento complete: ${jsonData.take(300)}")
                            return parseCompleteEvent(jsonData)
                        }
                    } else if (line.startsWith("event: error")) {
                        Napier.e("[TEST-LOG] Evento 'error' recibido")
                        val dataLine = lines.find { it.startsWith("data: ") }
                        val errorMsg = dataLine?.removePrefix("data: ") ?: "Error desconocido"
                        return GradioResponse(error = "Error del servidor: $errorMsg")
                    } else if (line.startsWith("event: generating")) {
                        Napier.d("[TEST-LOG] Evento 'generating' - analisis en progreso")
                    }
                }

                // Esperar antes del siguiente intento
                delay(2000)

            } catch (e: Exception) {
                Napier.e("[TEST-LOG] Error en polling intento $attempts", e)
                if (attempts >= maxAttempts) {
                    return GradioResponse(error = "Timeout esperando resultados: ${e.message}")
                }
                delay(2000)
            }
        }

        return GradioResponse(error = "Timeout: Se agoto el tiempo de espera")
    }

    private fun parseCompleteEvent(jsonData: String): GradioResponse {
        return parseDirectResponse(jsonData)
    }

    private fun parseDirectResponse(responseText: String): GradioResponse {
        try {
            Napier.d("[TEST-LOG] Parseando respuesta directa")

            // Parsear como JSON object con campo "data"
            val jsonElement = json.parseToJsonElement(responseText)

            if (jsonElement is JsonObject && jsonElement.containsKey("data")) {
                val dataElement = jsonElement["data"]

                if (dataElement !is JsonArray) {
                    Napier.e("[TEST-LOG] Campo 'data' no es un array")
                    return GradioResponse(error = "Formato de respuesta invalido")
                }

                val dataArray = dataElement.jsonArray

                if (dataArray.size < 2) {
                    Napier.e("[TEST-LOG] Array de datos incompleto (size: ${dataArray.size})")
                    return GradioResponse(error = "Respuesta incompleta")
                }

                Napier.d("[TEST-LOG] Array de datos tiene ${dataArray.size} elementos")

                // Extraer imagen procesada (Index 0)
                val processedImageElement = dataArray[0]
                val processedImageUrl = when {
                    processedImageElement is JsonPrimitive && processedImageElement.isString -> {
                        val url = processedImageElement.content
                        if (url.startsWith("http") || url.startsWith("data:")) {
                            url
                        } else {
                            "$baseUrl/file=$url"
                        }
                    }
                    processedImageElement is JsonObject -> {
                        val url = processedImageElement["url"]?.jsonPrimitive?.content
                            ?: processedImageElement["path"]?.jsonPrimitive?.content ?: ""
                        if (url.startsWith("http")) url else "$baseUrl/file=$url"
                    }
                    else -> null
                }

                Napier.d("[TEST-LOG] URL imagen procesada: $processedImageUrl")

                // Extraer datos tecnicos (Index 1) - String JSON
                val technicalDataElement = dataArray[1]
                if (technicalDataElement is JsonNull) {
                    Napier.e("[TEST-LOG] Datos tecnicos son null")
                    return GradioResponse(error = "Datos tecnicos no disponibles")
                }

                val technicalDataString = technicalDataElement.jsonPrimitive.content
                Napier.d("[TEST-LOG] String de datos tecnicos (primeros 200 chars): ${technicalDataString.take(200)}")

                // Extraer reporte HTML (Index 2 - opcional)
                val htmlReport = if (dataArray.size > 2) {
                    dataArray[2].jsonPrimitive.contentOrNull
                } else null

                // Parsear JSON interno
                val technicalData = json.decodeFromString<GradioTechnicalData>(technicalDataString)
                Napier.d("[TEST-LOG] Datos tecnicos parseados: ${technicalData.detections.size} detecciones")

                // Construir respuesta final
                val finalResponse = GradioResponse(
                    eventId = null,
                    data = listOf(
                        GradioDataItem(
                            image = processedImageUrl,
                            detections = technicalDataString,
                            summary = GradioSummary(
                                totalDetections = technicalData.summary?.total_teeth_detected
                                    ?: technicalData.detections.size,
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

                Napier.i("[TEST-LOG] Analisis completado exitosamente")
                Napier.i("[TEST-LOG] Resultados: ${finalResponse.data?.first()?.summary?.totalDetections} dientes, ${finalResponse.data?.first()?.summary?.cariesDetected} caries")

                return finalResponse
            } else {
                Napier.e("[TEST-LOG] Respuesta no contiene campo 'data'")
                return GradioResponse(error = "Formato de respuesta invalido")
            }

        } catch (e: Exception) {
            Napier.e("[TEST-LOG] Error parseando respuesta", e)
            return GradioResponse(error = "Error parseando resultados: ${e.message}")
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
                if (text.length > 10) {
                    recommendations.add(text)
                }
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Analisis completado con exito")

            val cavityCount = technicalData.summary?.cavity_count ?: 0
            if (cavityCount > 0) {
                recommendations.add("Se detectaron $cavityCount caries que requieren atencion")
                recommendations.add("Consultar con profesional odontologico para tratamiento")
            } else {
                recommendations.add("No se detectaron caries en la imagen analizada")
                recommendations.add("Mantener higiene dental regular")
            }

            recommendations.add("Revisar detecciones marcadas en la imagen procesada")
        }

        return recommendations
    }

    fun close() {
        httpClient.close()
    }
}
