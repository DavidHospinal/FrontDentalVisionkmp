package com.dentalvision.ai.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReportDTO(
    val id: String,
    val patient_id: String,
    val report_type: String,
    val file_path: String,
    val metadata: ReportMetadataDTO,
    val created_at: String
)

@Serializable
data class ReportMetadataDTO(
    val include_images: Boolean,
    val include_recommendations: Boolean,
    val analysis_count: Int,
    val date_range: DateRangeDTO
)

@Serializable
data class DateRangeDTO(
    val start: String,
    val end: String
)

@Serializable
data class GenerateReportRequest(
    val patient_id: String,
    val report_type: String = "individual",
    val include_images: Boolean = true,
    val include_recommendations: Boolean = true,
    val date_from: String? = null,
    val date_to: String? = null
)
