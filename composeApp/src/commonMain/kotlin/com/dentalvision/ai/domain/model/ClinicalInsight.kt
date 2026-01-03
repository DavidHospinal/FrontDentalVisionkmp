package com.dentalvision.ai.domain.model

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
