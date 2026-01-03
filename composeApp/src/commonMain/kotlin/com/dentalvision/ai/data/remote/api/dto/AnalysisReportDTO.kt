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
    val objectId: Int? = null,
    @SerialName("class_id")
    val classId: Int? = null,
    @SerialName("class_name")
    val className: String? = null,
    val confidence: Double? = null,
    val bbox: List<Double>? = null,
    val probabilities: List<Double>? = null
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
    val bbox: List<Double>,
    val probabilities: List<Double>
) {
    fun getConfidencePercentage(): String = "${(confidence * 100).toInt()}%"

    fun isCavity(): Boolean = classId == 0

    fun getDisplayName(): String {
        return when (classId) {
            0 -> "Cavity"
            1 -> "Healthy Tooth"
            else -> "Unknown"
        }
    }

    fun getDisplayColor(): String {
        return when (classId) {
            0 -> "#FF0000"
            1 -> "#00C853"
            else -> "#FFC107"
        }
    }

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
    // 🔍 LOG 2: Before DTO conversion
    io.github.aakira.napier.Napier.d("🔍 DTO CONVERSION START: Converting ${detections.size} detections")

    val convertedDetections = detections.mapIndexed { index, detection ->
        val converted = detection.toDomainModel()
        if (index < 5) {
            io.github.aakira.napier.Napier.d("🔍 DTO CONVERSION: Detection #$index BEFORE - classId=${detection.classId}, className='${detection.className}'")
            io.github.aakira.napier.Napier.d("🔍 DTO CONVERSION: Detection #$index AFTER  - classId=${converted.classId}, className='${converted.className}', isCavity=${converted.isCavity()}")
        }
        converted
    }

    return AnalysisReport(
        success = success,
        analysisTimestamp = analysisTimestamp,
        modelInfo = modelInfo,
        detections = convertedDetections,
        summary = summary.toDomainModel(),
        confidenceThreshold = confidenceThreshold,
        patient = patient?.toDomainModel(),
        analysisId = analysisId,
        imageFilename = imageFilename
    )
}

fun DetectionDTO.toDomainModel(): Detection {
    // CRITICAL: className is the source of truth, NOT classId
    // Backend sometimes sends wrong classId, but className is always correct
    val resolvedClassId = when {
        // ALWAYS prioritize className parsing first (most reliable)
        // IMPORTANT: Check for null value AND string literal "null"
        className != null && className != "null" -> when {
            className.contains("normal", ignoreCase = true) ||
            className.contains("healthy", ignoreCase = true) -> 1  // Healthy tooth
            className.contains("cavity", ignoreCase = true) ||
            className.contains("caries", ignoreCase = true) -> 0  // Cavity
            else -> classId ?: 0  // Fallback to classId if className is ambiguous
        }
        // Only use classId if className is null (rare case)
        classId != null -> classId
        // Ultimate fallback - className is null or "null" string, classId is null
        // This should NEVER happen with new data, but handle legacy data gracefully
        else -> 0  // Default to cavity for safety
    }

    return Detection(
        objectId = objectId ?: 0,
        classId = resolvedClassId,
        className = className ?: "Unknown",
        confidence = confidence ?: 0.0,
        bbox = bbox ?: emptyList(),
        probabilities = probabilities ?: emptyList()
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
