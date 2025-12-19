package com.dentalvision.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DentalAnalysis(
    val id: String,
    val patient_id: String? = null,
    val image_path: String,
    val confidence_threshold: Double,
    val detections: List<Detection>,
    val summary: AnalysisSummary,
    val created_at: String
)

@Serializable
data class Detection(
    val `class`: String,
    val confidence: Double,
    val bbox: List<Double>,
    val fdi_number: String? = null
)

@Serializable
data class AnalysisSummary(
    val total_detections: Int,
    val healthy_teeth: Int,
    val caries_detected: Int,
    val severity_distribution: SeverityDistribution,
    val recommendations: List<String>
)

@Serializable
data class SeverityDistribution(
    val mild: Int,
    val moderate: Int,
    val severe: Int
)
