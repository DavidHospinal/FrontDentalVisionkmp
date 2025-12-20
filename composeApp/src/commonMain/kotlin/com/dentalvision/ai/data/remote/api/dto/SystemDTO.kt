package com.dentalvision.ai.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class SystemStatisticsDTO(
    val patients: PatientStatsDTO,
    val analyses: AnalysisStatsDTO,
    val appointments: AppointmentStatsDTO,
    val reports: ReportStatsDTO
)

@Serializable
data class PatientStatsDTO(
    val total: Int,
    val new_this_month: Int
)

@Serializable
data class AnalysisStatsDTO(
    val total: Int,
    val this_month: Int
)

@Serializable
data class AppointmentStatsDTO(
    val scheduled: Int,
    val completed: Int
)

@Serializable
data class ReportStatsDTO(
    val generated: Int,
    val this_month: Int
)
