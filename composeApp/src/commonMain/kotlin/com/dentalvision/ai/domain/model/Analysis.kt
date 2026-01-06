@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.dentalvision.ai.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Domain model representing a dental analysis performed on a patient's dental image
 */
@Serializable
data class Analysis(
    val id: String,
    val patientId: String,
    val imageUrl: String,
    @Contextual val analysisDate: Instant,
    val totalTeethDetected: Int,
    val totalCariesDetected: Int,
    val confidenceScore: Double,        // Average confidence across all detections
    val status: AnalysisStatus,
    val detections: List<ToothDetection> = emptyList(),
    val notes: String? = null,
    val performedBy: String? = null,    // Dentist/user who performed the analysis
    val synced: Boolean = false
) {
    enum class AnalysisStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    /**
     * Number of healthy teeth detected
     */
    val healthyTeethCount: Int
        get() = totalTeethDetected - totalCariesDetected

    /**
     * Percentage of teeth with caries
     */
    val cariesPercentage: Double
        get() = if (totalTeethDetected > 0) {
            (totalCariesDetected.toDouble() / totalTeethDetected) * 100
        } else {
            0.0
        }

    /**
     * Severity level based on caries percentage
     */
    val severityLevel: SeverityLevel
        get() = when {
            cariesPercentage == 0.0 -> SeverityLevel.EXCELLENT
            cariesPercentage < 15.0 -> SeverityLevel.GOOD
            cariesPercentage < 30.0 -> SeverityLevel.MODERATE
            cariesPercentage < 50.0 -> SeverityLevel.CONCERNING
            else -> SeverityLevel.SEVERE
        }

    enum class SeverityLevel(val displayName: String, val color: String) {
        EXCELLENT("Excellent", "#4CAF50"),      // Green
        GOOD("Good", "#8BC34A"),               // Light Green
        MODERATE("Moderate", "#FFC107"),        // Amber
        CONCERNING("Concerning", "#FF9800"),    // Orange
        SEVERE("Severe", "#F44336")            // Red
    }

    /**
     * Summary text for the analysis
     */
    val summary: String
        get() = buildString {
            append("Detected $totalTeethDetected teeth")
            if (totalCariesDetected > 0) {
                val percentage = (cariesPercentage * 10).toInt() / 10.0
                append(" with $totalCariesDetected caries ($percentage%)")
            } else {
                append(" - All healthy!")
            }
        }

    /**
     * Detections grouped by quadrant
     */
    val detectionsByQuadrant: Map<Int, List<ToothDetection>>
        get() = detections.groupBy { it.quadrant }

    /**
     * List of teeth with caries only
     */
    val cariesDetections: List<ToothDetection>
        get() = detections.filter { it.hasCaries }

    /**
     * Average confidence of all detections
     */
    val averageConfidence: Double
        get() = if (detections.isNotEmpty()) {
            detections.map { it.confidence }.average()
        } else {
            0.0
        }

    /**
     * Checks if analysis is reliable (average confidence > 70%)
     */
    val isReliable: Boolean
        get() = averageConfidence >= 0.70
}
