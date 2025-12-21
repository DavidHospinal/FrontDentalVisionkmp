package com.dentalvision.ai.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SystemStatisticsDTO(
    val patients: PatientStatsDTO = PatientStatsDTO(),
    val analyses: AnalysisStatsDTO = AnalysisStatsDTO(),
    val appointments: AppointmentStatsDTO = AppointmentStatsDTO(),
    val reports: ReportStatsDTO = ReportStatsDTO(),
    @SerialName("monthly_trend")
    val monthlyTrend: List<MonthlyDataDTO> = emptyList()
)

@Serializable
data class PatientStatsDTO(
    @SerialName("total_patients")
    val total: Int = 0,
    @SerialName("recent_registrations")
    val new_this_month: Int = 0
)

@Serializable
data class AnalysisStatsDTO(
    @SerialName("total_analyses")
    val total: Int = 0,
    val this_month: Int = 0
)

@Serializable
data class AppointmentStatsDTO(
    @SerialName("total_appointments")
    val scheduled: Int = 0,
    @SerialName("completed_appointments")
    val completed: Int = 0
)

@Serializable
data class ReportStatsDTO(
    @SerialName("total_generated")
    val generated: Int = 0,
    @SerialName("total_downloads")
    val this_month: Int = 0
)

@Serializable
data class MonthlyDataDTO(
    val month: String,
    val analyses: Int = 0,
    val appointments: Int = 0
)
