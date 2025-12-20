package com.dentalvision.ai.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class DentalAnalysisDTO(
    val id: String,
    val patient_id: String? = null,
    val image_path: String,
    val confidence_threshold: Double,
    val detections: List<DetectionDTO>,
    val summary: AnalysisSummaryDTO,
    val created_at: String
)

@Serializable
data class DetectionDTO(
    val `class`: String,
    val confidence: Double,
    val bbox: List<Double>,
    val fdi_number: String? = null
)

@Serializable
data class AnalysisSummaryDTO(
    val total_detections: Int,
    val healthy_teeth: Int,
    val caries_detected: Int,
    val severity_distribution: SeverityDistributionDTO,
    val recommendations: List<String>
)

@Serializable
data class SeverityDistributionDTO(
    val mild: Int,
    val moderate: Int,
    val severe: Int
)

@Serializable
data class AnalysisRequest(
    val patient_id: String? = null,
    val confidence_threshold: Double = 0.5,
    val analysis_type: String = "complete"
)
