package com.dentalvision.ai.data.remote.gemini.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

data class ClinicalInsightRequest(
    val doctorName: String,
    val patientName: String,
    val cavityCount: Int,
    val healthyCount: Int,
    val confidence: Float
)
