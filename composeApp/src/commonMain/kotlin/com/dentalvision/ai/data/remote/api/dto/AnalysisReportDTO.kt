package com.dentalvision.ai.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for parsing AI Analysis Report JSON data
 * Matches the structure from HuggingFace YOLOv12 model output
 */

@Serializable
data class AnalysisReportDTO(
    val success: Boolean,
    @SerialName("analysis_timestamp")
    val analysisTimestamp: String,
    @SerialName("model_info")
    val modelInfo: String,
    val detections: List<DetectionDTO>,
    val summary: AnalysisSummaryDTO,
    @SerialName("confidence_threshold")
    val confidenceThreshold: Double,
    val patient: PatientInfoDTO? = null,
    @SerialName("analysis_id")
    val analysisId: String? = null,
    @SerialName("image_filename")
    val imageFilename: String? = null
)

@Serializable
data class DetectionDTO(
    @SerialName("object_id")
    val objectId: Int,
    @SerialName("class_id")
    val classId: Int,
    @SerialName("class_name")
    val className: String,
    val confidence: Double,
    val bbox: List<Int>, // [x1, y1, x2, y2]
    val probabilities: List<Double>
)

@Serializable
data class AnalysisSummaryDTO(
    @SerialName("total_teeth_detected")
    val totalTeethDetected: Int,
    @SerialName("cavity_count")
    val cavityCount: Int,
    @SerialName("healthy_count")
    val healthyCount: Int,
    @SerialName("health_percentage")
    val healthPercentage: Double
)

@Serializable
data class PatientInfoDTO(
    val id: String,
    val name: String,
    val age: Int? = null,
    val phone: String? = null
)

// Domain model for UI
data class AnalysisReport(
    val success: Boolean,
    val analysisTimestamp: String,
    val modelInfo: String,
    val detections: List<Detection>,
    val summary: AnalysisSummary,
    val confidenceThreshold: Double,
    val patient: PatientInfo? = null,
    val analysisId: String? = null,
    val imageFilename: String? = null
) {
    fun getAverageConfidence(): Double {
        if (detections.isEmpty()) return 0.0
        return detections.map { it.confidence }.average()
    }

    fun getHealthStatus(): HealthStatus {
        return when {
            summary.cavityCount == 0 -> HealthStatus.EXCELLENT
            summary.healthPercentage >= 80.0 -> HealthStatus.GOOD
            summary.healthPercentage >= 50.0 -> HealthStatus.MODERATE
            else -> HealthStatus.REQUIRES_ATTENTION
        }
    }
}

data class Detection(
    val objectId: Int,
    val classId: Int,
    val className: String,
    val confidence: Double,
    val bbox: List<Int>,
    val probabilities: List<Double>
) {
    fun getConfidencePercentage(): String = "${(confidence * 100).toInt()}%"

    fun isCavity(): Boolean = className.equals("cavity", ignoreCase = true)

    fun getSeverity(): DetectionSeverity {
        return when {
            confidence >= 0.8 -> DetectionSeverity.HIGH
            confidence >= 0.5 -> DetectionSeverity.MEDIUM
            else -> DetectionSeverity.LOW
        }
    }
}

data class AnalysisSummary(
    val totalTeethDetected: Int,
    val cavityCount: Int,
    val healthyCount: Int,
    val healthPercentage: Double
)

data class PatientInfo(
    val id: String,
    val name: String,
    val age: Int? = null,
    val phone: String? = null
)

enum class HealthStatus(val displayName: String, val colorCode: String) {
    EXCELLENT("Excellent", "#4CAF50"),
    GOOD("Good", "#8BC34A"),
    MODERATE("Moderate", "#FFC107"),
    REQUIRES_ATTENTION("Requires Attention", "#F44336")
}

enum class DetectionSeverity(val displayName: String) {
    HIGH("High Confidence"),
    MEDIUM("Medium Confidence"),
    LOW("Low Confidence")
}

// Extension functions to convert DTOs to domain models
fun AnalysisReportDTO.toDomainModel(): AnalysisReport {
    return AnalysisReport(
        success = success,
        analysisTimestamp = analysisTimestamp,
        modelInfo = modelInfo,
        detections = detections.map { it.toDomainModel() },
        summary = summary.toDomainModel(),
        confidenceThreshold = confidenceThreshold,
        patient = patient?.toDomainModel(),
        analysisId = analysisId,
        imageFilename = imageFilename
    )
}

fun DetectionDTO.toDomainModel(): Detection {
    return Detection(
        objectId = objectId,
        classId = classId,
        className = className,
        confidence = confidence,
        bbox = bbox,
        probabilities = probabilities
    )
}

fun AnalysisSummaryDTO.toDomainModel(): AnalysisSummary {
    return AnalysisSummary(
        totalTeethDetected = totalTeethDetected,
        cavityCount = cavityCount,
        healthyCount = healthyCount,
        healthPercentage = healthPercentage
    )
}

fun PatientInfoDTO.toDomainModel(): PatientInfo {
    return PatientInfo(
        id = id,
        name = name,
        age = age,
        phone = phone
    )
}
