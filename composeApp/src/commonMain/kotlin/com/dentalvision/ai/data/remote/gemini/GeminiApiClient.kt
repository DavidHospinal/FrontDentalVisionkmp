package com.dentalvision.ai.data.remote.gemini

import com.dentalvision.ai.data.remote.gemini.dto.*
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class GeminiApiClient(
    private val httpClient: HttpClient,
    private val apiKey: String
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent"

    suspend fun getClinicalInsight(request: ClinicalInsightRequest): Result<ClinicalInsight> {
        return try {
            Napier.d("GEMINI: Requesting clinical insight for patient ${request.patientName}")

            val prompt = buildPrompt(request)
            Napier.d("GEMINI: Prompt length: ${prompt.length} characters")

            val geminiRequest = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt)
                        )
                    )
                )
            )

            val response: HttpResponse = httpClient.post("$baseUrl?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(geminiRequest)
            }

            Napier.d("GEMINI: Response status: ${response.status}")

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                Napier.e("GEMINI: Error response: $errorBody")
                return Result.failure(Exception("Gemini API error: ${response.status}"))
            }

            val geminiResponse: GeminiResponse = response.body()
            Napier.d("GEMINI: Received ${geminiResponse.candidates.size} candidates")

            val responseText = geminiResponse.candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?: return Result.failure(Exception("Empty response from Gemini"))

            Napier.d("GEMINI: Raw response text length: ${responseText.length}")

            val cleanedJson = cleanJsonResponse(responseText)
            Napier.d("GEMINI: Cleaned JSON: ${cleanedJson.take(200)}")

            val insightResponse = json.decodeFromString<ClinicalInsightResponse>(cleanedJson)

            val clinicalInsight = ClinicalInsight(
                greeting = insightResponse.greeting,
                diagnosisSummary = insightResponse.diagnosisSummary,
                preventionTips = insightResponse.preventionTips,
                correctiveActions = insightResponse.correctiveActions,
                riskLevel = RiskLevel.fromString(insightResponse.riskLevel)
            )

            Napier.i("GEMINI: Successfully generated clinical insight with risk level: ${clinicalInsight.riskLevel}")
            Result.success(clinicalInsight)

        } catch (e: Exception) {
            Napier.e("GEMINI: Failed to get clinical insight", e)
            Result.failure(e)
        }
    }

    private fun buildPrompt(request: ClinicalInsightRequest): String {
        val confidenceRounded = (request.confidence * 10).toInt() / 10.0

        return """
You are Dental Vision IA, an expert assistant in preventive and corrective dentistry.

Context: Dr. ${request.doctorName} is treating patient ${request.patientName}. The analysis detected ${request.cavityCount} cavities and ${request.healthyCount} healthy teeth. The model confidence is $confidenceRounded%.

CRITICAL INSTRUCTIONS:
1. Return ONLY valid JSON (no markdown code blocks, no backticks, no extra text)
2. Use the exact structure provided below
3. Determine riskLevel: "LOW" if cavityCount = 0, "HIGH" if cavityCount > 0, "MODERATE" if uncertain
4. Write in professional English
5. Keep greeting concise and professional
6. Provide 3 prevention tips and 2-3 corrective actions

Required JSON structure:
{
  "greeting": "Hello Dr. ${request.doctorName}, this is Dental Vision IA. Based on the analysis of ${request.patientName}...",
  "diagnosisSummary": "Brief professional clinical summary...",
  "preventionTips": ["Tip 1", "Tip 2", "Tip 3"],
  "correctiveActions": ["Suggested action 1", "Suggested action 2"],
  "riskLevel": "LOW"
}

Generate the JSON response now:
        """.trimIndent()
    }

    private fun cleanJsonResponse(text: String): String {
        var cleaned = text.trim()

        // Remove markdown code blocks
        cleaned = cleaned.replace("```json", "")
        cleaned = cleaned.replace("```", "")

        // Find JSON object boundaries
        val startIndex = cleaned.indexOf('{')
        val endIndex = cleaned.lastIndexOf('}')

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            cleaned = cleaned.substring(startIndex, endIndex + 1)
        }

        return cleaned.trim()
    }
}

data class ClinicalInsight(
    val greeting: String,
    val diagnosisSummary: String,
    val preventionTips: List<String>,
    val correctiveActions: List<String>,
    val riskLevel: RiskLevel
)

enum class RiskLevel(val displayName: String) {
    LOW("Low Risk"),
    MODERATE("Moderate Risk"),
    HIGH("High Risk");

    companion object {
        fun fromString(value: String): RiskLevel {
            return when (value.uppercase()) {
                "LOW" -> LOW
                "MODERATE" -> MODERATE
                "HIGH" -> HIGH
                else -> MODERATE
            }
        }
    }
}
