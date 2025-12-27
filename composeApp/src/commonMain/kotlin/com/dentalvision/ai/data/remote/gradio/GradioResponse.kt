package com.dentalvision.ai.data.remote.gradio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from Gradio API
 * Can contain either:
 * - event_id (for polling-based protocol)
 * - data (direct results)
 * - error (if processing failed)
 */
@Serializable
data class GradioResponse(
    @SerialName("event_id")
    val eventId: String? = null,

    @SerialName("data")
    val data: List<GradioDataItem>? = null,

    @SerialName("error")
    val error: String? = null,

    @SerialName("duration")
    val duration: Double? = null
)

/**
 * Data item from Gradio response
 * The actual structure depends on the Gradio Space implementation
 */
@Serializable
data class GradioDataItem(
    // Annotated image URL/path
    @SerialName("image")
    val image: String? = null,

    // Detection results as JSON string or object
    @SerialName("detections")
    val detections: String? = null,

    // Summary statistics
    @SerialName("summary")
    val summary: GradioSummary? = null
)

/**
 * Summary statistics from AI analysis
 */
@Serializable
data class GradioSummary(
    @SerialName("total_detections")
    val totalDetections: Int = 0,

    @SerialName("healthy_teeth")
    val healthyTeeth: Int = 0,

    @SerialName("caries_detected")
    val cariesDetected: Int = 0,

    @SerialName("average_confidence")
    val averageConfidence: Double = 0.0,

    @SerialName("severity_distribution")
    val severityDistribution: Map<String, Int>? = null,

    @SerialName("recommendations")
    val recommendations: List<String>? = null
)

/**
 * Detection result for a single tooth
 * Matches HuggingFace YOLOv12 backend format
 */
@Serializable
data class GradioDetection(
    @SerialName("object_id")
    val objectId: Int? = null,

    @SerialName("class_id")
    val classId: Int? = null,

    @SerialName("class_name")
    val className: String? = null,

    @SerialName("confidence")
    val confidence: Double = 0.0,

    @SerialName("bbox")
    val bbox: List<Double>? = null,

    @SerialName("fdi_number")
    val fdiNumber: String? = null,

    @SerialName("has_caries")
    val hasCaries: Boolean = false
)
