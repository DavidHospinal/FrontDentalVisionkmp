package com.dentalvision.ai.domain.repository

import com.dentalvision.ai.domain.model.Report

/**
 * Repository interface for Report operations
 * Defines contract for PDF report generation and management
 */
interface ReportRepository {

    /**
     * Generate PDF report for an analysis
     * @param analysisId Analysis ID
     * @return Result containing report metadata or error
     */
    suspend fun generateReport(analysisId: String): Result<Report>

    /**
     * Download PDF report
     * @param reportId Report ID
     * @return Result containing PDF file bytes or error
     */
    suspend fun downloadReport(reportId: String): Result<ByteArray>

    /**
     * Get all reports for a patient
     * @param patientId Patient ID
     * @return Result containing list of reports and total count
     */
    suspend fun getPatientReports(patientId: String): Result<Pair<List<Report>, Int>>
}
