@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.dentalvision.ai.data.repository

import com.dentalvision.ai.data.remote.api.dto.GenerateReportRequest
import com.dentalvision.ai.data.remote.api.dto.ReportDTO
import com.dentalvision.ai.data.remote.service.ReportService
import com.dentalvision.ai.domain.model.Report
import com.dentalvision.ai.domain.repository.ReportRepository
import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlinx.datetime.Instant

/**
 * Implementation of ReportRepository
 * Handles PDF report generation and retrieval
 */
class ReportRepositoryImpl(
    private val reportService: ReportService
) : ReportRepository {

    /**
     * Generate PDF report for an analysis
     */
    override suspend fun generateReport(analysisId: String): Result<Report> {
        return try {
            Napier.d("Generating report for analysis: $analysisId")

            // Note: Backend might require patient_id instead of analysis_id
            // Adjust request as needed based on actual API contract
            val request = GenerateReportRequest(
                patient_id = analysisId, // TODO: Get actual patient_id from analysis
                report_type = "individual",
                include_images = true,
                include_recommendations = true
            )

            val response = reportService.generateReport(request)

            if (response.success && response.data != null) {
                val report = response.data.toDomainModel()
                Napier.i("Successfully generated report: ${report.id}")
                Result.success(report)
            } else {
                Napier.e("Failed to generate report: ${response.message ?: response.error}")
                Result.failure(Exception(response.message ?: response.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Napier.e("Error generating report for analysis: $analysisId", e)
            Result.failure(e)
        }
    }

    /**
     * Download PDF report
     */
    override suspend fun downloadReport(reportId: String): Result<ByteArray> {
        return try {
            Napier.d("Downloading report: $reportId")

            val pdfBytes = reportService.downloadReport(reportId)

            Napier.i("Successfully downloaded report: $reportId (${pdfBytes.size} bytes)")
            Result.success(pdfBytes)

        } catch (e: Exception) {
            Napier.e("Error downloading report: $reportId", e)
            Result.failure(e)
        }
    }

    /**
     * Get all reports for a patient
     */
    override suspend fun getPatientReports(patientId: String): Result<Pair<List<Report>, Int>> {
        return try {
            Napier.d("Fetching reports for patient: $patientId")

            val response = reportService.getPatientReports(patientId)

            if (response.success && response.data != null) {
                val reports = response.data.map { it.toDomainModel() }
                Napier.i("Successfully fetched ${reports.size} reports for patient: $patientId")
                Result.success(Pair(reports, reports.size))
            } else {
                Napier.e("Failed to fetch patient reports: ${response.message ?: response.error}")
                Result.failure(Exception(response.message ?: response.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Napier.e("Error fetching patient reports: $patientId", e)
            Result.failure(e)
        }
    }

    /**
     * Convert ReportDTO to domain Report model
     */
    private fun ReportDTO.toDomainModel(): Report {
        return Report(
            id = this.id,
            analysisId = "", // Not provided in DTO, would need separate lookup
            patientId = this.patient_id,
            reportDate = parseInstant(this.created_at),
            pdfUrl = this.file_path,
            summary = "Report generated successfully", // DTO doesn't include summary
            recommendations = if (this.metadata.include_recommendations) {
                "Follow recommended dental hygiene practices"
            } else null,
            findings = emptyList(), // DTO doesn't include detailed findings
            generatedBy = "Dental Vision AI System",
            clinicName = null,
            clinicLogo = null,
            synced = true
        )
    }

    /**
     * Parse ISO 8601 timestamp to Instant
     */
    private fun parseInstant(timestamp: String): Instant {
        return try {
            Instant.parse(timestamp)
        } catch (e: Exception) {
            Clock.System.now()
        }
    }
}
