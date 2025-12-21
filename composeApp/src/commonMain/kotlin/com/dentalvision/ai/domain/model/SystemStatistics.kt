package com.dentalvision.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SystemStatistics(
    val patients: PatientStats,
    val analyses: AnalysisStats,
    val appointments: AppointmentStats,
    val reports: ReportStats,
    val monthlyTrend: List<MonthlyData> = emptyList()
)

@Serializable
data class PatientStats(
    val total: Int,
    val new_this_month: Int
)

@Serializable
data class AnalysisStats(
    val total: Int,
    val this_month: Int
)

@Serializable
data class AppointmentStats(
    val scheduled: Int,
    val completed: Int
)

@Serializable
data class ReportStats(
    val generated: Int,
    val this_month: Int
)

@Serializable
data class MonthlyData(
    val month: String,
    val analyses: Int,
    val appointments: Int = 0
)
