package com.dentalvision.ai.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class SystemStatisticsDTO(
    val patients: PatientStatsDTO = PatientStatsDTO(),
    val analyses: AnalysisStatsDTO = AnalysisStatsDTO(),
    val appointments: AppointmentStatsDTO = AppointmentStatsDTO(),
    val reports: ReportStatsDTO = ReportStatsDTO()
)

@Serializable
data class PatientStatsDTO(
    val total: Int = 0,
    val new_this_month: Int = 0
)

@Serializable
data class AnalysisStatsDTO(
    val total: Int = 0,
    val this_month: Int = 0
)

@Serializable
data class AppointmentStatsDTO(
    val scheduled: Int = 0,
    val completed: Int = 0
)

@Serializable
data class ReportStatsDTO(
    val generated: Int = 0,
    val this_month: Int = 0
)
