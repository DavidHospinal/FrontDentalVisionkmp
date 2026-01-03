package com.dentalvision.ai.data.remote.gemini.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContentResponse
)

@Serializable
data class GeminiContentResponse(
    val parts: List<GeminiPartResponse>
)

@Serializable
data class GeminiPartResponse(
    val text: String
)

@Serializable
data class ClinicalInsightResponse(
    val greeting: String,
    @SerialName("diagnosisSummary")
    val diagnosisSummary: String,
    @SerialName("preventionTips")
    val preventionTips: List<String>,
    @SerialName("correctiveActions")
    val correctiveActions: List<String>,
    @SerialName("riskLevel")
    val riskLevel: String
)
