package com.dentalvision.ai.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class DentalAnalysisDTO(
    val id: String,
    val patient_id: String? = null,
    val image_path: String,
    val confidence_threshold: Double,
    val detections: List<DentalDetectionDTO>,
    val summary: DentalAnalysisSummaryDTO,
    val created_at: String
)

@Serializable
data class DentalDetectionDTO(
    val `class`: String,
    val confidence: Double,
    val bbox: List<Double>,
    val fdi_number: String? = null
)

@Serializable
data class DentalAnalysisSummaryDTO(
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

/**
 * DTOs for listing all analyses
 */
@Serializable
data class AnalysisListResponseDTO(
    val success: Boolean,
    val data: AnalysisListDataDTO,
    val message: String
)

@Serializable
data class AnalysisListDataDTO(
    val analyses: List<AnalysisItemDTO>,
    val pagination: PaginationDTO
)

@Serializable
data class AnalysisItemDTO(
    val analysis_id: String,
    val patient: AnalysisPatientDTO,
    val date: String,
    val summary: AnalysisSummaryItemDTO,
    val status: String,
    val type: String
)

@Serializable
data class AnalysisPatientDTO(
    val id: String,
    val name: String,
    val age: Int
)

@Serializable
data class AnalysisSummaryItemDTO(
    val total_teeth: Int,
    val cavities: Int,
    val healthy: Int,
    val health_percentage: Double
)

@Serializable
data class PaginationDTO(
    val page: Int,
    val per_page: Int,
    val total: Int,
    val pages: Int,
    val has_next: Boolean,
    val has_prev: Boolean
)
