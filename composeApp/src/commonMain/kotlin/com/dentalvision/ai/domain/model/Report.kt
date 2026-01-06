@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.dentalvision.ai.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Domain model representing a generated dental report
 */
@Serializable
data class Report(
    val id: String,
    val analysisId: String,
    val patientId: String,
    @Contextual val reportDate: Instant,
    val pdfUrl: String? = null,
    val summary: String,
    val recommendations: String? = null,
    val findings: List<Finding> = emptyList(),
    val generatedBy: String? = null,
    val clinicName: String? = null,
    val clinicLogo: String? = null,
    val synced: Boolean = false
) {
    /**
     * Individual finding in the dental report
     */
    @Serializable
    data class Finding(
        val toothNumberFDI: Int,
        val description: String,
        val severity: Severity,
        val recommendedTreatment: String? = null
    ) {
        enum class Severity {
            NORMAL,
            MILD,
            MODERATE,
            SEVERE
        }

        val severityText: String
            get() = severity.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    /**
     * Total number of findings in the report
     */
    val totalFindings: Int
        get() = findings.size

    /**
     * Number of severe findings
     */
    val severeFindings: Int
        get() = findings.count { it.severity == Finding.Severity.SEVERE }

    /**
     * Number of moderate findings
     */
    val moderateFindings: Int
        get() = findings.count { it.severity == Finding.Severity.MODERATE }

    /**
     * Number of mild findings
     */
    val mildFindings: Int
        get() = findings.count { it.severity == Finding.Severity.MILD }

    /**
     * Checks if PDF is available for download
     */
    val hasPDF: Boolean
        get() = !pdfUrl.isNullOrBlank()

    /**
     * Report summary statistics
     */
    val statistics: String
        get() = buildString {
            append("Total findings: $totalFindings")
            if (severeFindings > 0) append(" | Severe: $severeFindings")
            if (moderateFindings > 0) append(" | Moderate: $moderateFindings")
            if (mildFindings > 0) append(" | Mild: $mildFindings")
        }

    /**
     * Grouped findings by severity
     */
    val findingsBySeverity: Map<Finding.Severity, List<Finding>>
        get() = findings.groupBy { it.severity }

    /**
     * Priority findings (severe and moderate)
     */
    val priorityFindings: List<Finding>
        get() = findings.filter {
            it.severity == Finding.Severity.SEVERE ||
            it.severity == Finding.Severity.MODERATE
        }.sortedByDescending { it.severity }
}
