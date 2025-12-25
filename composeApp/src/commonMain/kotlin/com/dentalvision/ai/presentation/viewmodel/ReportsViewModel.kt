package com.dentalvision.ai.presentation.viewmodel

import com.dentalvision.ai.domain.model.Report
import com.dentalvision.ai.domain.repository.ReportRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for Reports screen
 * Handles report listing, generation, and PDF downloads
 */
class ReportsViewModel(
    private val reportRepository: ReportRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<ReportsUiState>(ReportsUiState.Loading)
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _downloadingReportId = MutableStateFlow<String?>(null)
    val downloadingReportId: StateFlow<String?> = _downloadingReportId.asStateFlow()

    /**
     * Load reports for a patient
     */
    fun loadPatientReports(patientId: String) {
        launchWithErrorHandler {
            _uiState.value = ReportsUiState.Loading
            Napier.d("Loading reports for patient: $patientId")

            reportRepository.getPatientReports(patientId)
                .onSuccess { (reportsList, _) ->
                    _reports.value = reportsList
                    _uiState.value = if (reportsList.isEmpty()) {
                        ReportsUiState.Empty
                    } else {
                        ReportsUiState.Success
                    }
                    Napier.i("Loaded ${reportsList.size} reports")
                }
                .onFailure { error ->
                    Napier.e("Failed to load reports", error)
                    _uiState.value = ReportsUiState.Error(
                        error.message ?: "Failed to load reports"
                    )
                }
        }
    }

    /**
     * Generate new report for an analysis
     */
    fun generateReport(analysisId: String, onSuccess: () -> Unit) {
        launchWithErrorHandler {
            _uiState.value = ReportsUiState.Loading
            Napier.d("Generating report for analysis: $analysisId")

            reportRepository.generateReport(analysisId)
                .onSuccess { report ->
                    Napier.i("Report generated successfully: ${report.id}")
                    // Add to list
                    _reports.value = _reports.value + report
                    _uiState.value = ReportsUiState.Success
                    onSuccess()
                }
                .onFailure { error ->
                    Napier.e("Failed to generate report", error)
                    _uiState.value = ReportsUiState.Error(
                        error.message ?: "Failed to generate report"
                    )
                }
        }
    }

    /**
     * Download PDF report
     */
    fun downloadReport(reportId: String, onSuccess: (ByteArray) -> Unit) {
        launchWithErrorHandler {
            _downloadingReportId.value = reportId
            Napier.d("Downloading report: $reportId")

            reportRepository.downloadReport(reportId)
                .onSuccess { pdfBytes ->
                    Napier.i("Report downloaded successfully: ${pdfBytes.size} bytes")
                    _downloadingReportId.value = null
                    onSuccess(pdfBytes)
                }
                .onFailure { error ->
                    Napier.e("Failed to download report", error)
                    _downloadingReportId.value = null
                    _uiState.value = ReportsUiState.Error(
                        error.message ?: "Failed to download report"
                    )
                }
        }
    }

    /**
     * Refresh reports list
     */
    fun refresh(patientId: String) {
        loadPatientReports(patientId)
    }
}

/**
 * UI state for Reports screen
 */
sealed class ReportsUiState {
    object Loading : ReportsUiState()
    object Success : ReportsUiState()
    object Empty : ReportsUiState()
    data class Error(val message: String) : ReportsUiState()
}
