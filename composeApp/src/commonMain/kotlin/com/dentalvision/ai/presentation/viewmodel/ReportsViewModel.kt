package com.dentalvision.ai.presentation.viewmodel

import com.dentalvision.ai.domain.model.Report
import com.dentalvision.ai.domain.repository.ReportRepository
import com.dentalvision.ai.data.remote.api.dto.AnalysisReport
import com.dentalvision.ai.data.remote.api.dto.toDomainModel
import com.dentalvision.ai.data.remote.api.dto.AnalysisItemDTO
import com.dentalvision.ai.data.remote.service.ReportService
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collect
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for Reports screen
 * Handles report listing, generation, and PDF downloads
 */
class ReportsViewModel(
    private val reportRepository: ReportRepository,
    private val reportService: ReportService
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<ReportsUiState>(ReportsUiState.Loading)
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _downloadingReportId = MutableStateFlow<String?>(null)
    val downloadingReportId: StateFlow<String?> = _downloadingReportId.asStateFlow()

    private val _selectedAnalysisReport = MutableStateFlow<AnalysisReport?>(null)
    val selectedAnalysisReport: StateFlow<AnalysisReport?> = _selectedAnalysisReport.asStateFlow()

    private val _allAnalyses = MutableStateFlow<List<AnalysisItemDTO>>(emptyList())
    val allAnalyses: StateFlow<List<AnalysisItemDTO>> = _allAnalyses.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadAllAnalyses()

        // Setup reactive search with debounce
        launchWithErrorHandler {
            _searchQuery
                .debounce(300.milliseconds)
                .distinctUntilChanged()
                .collect { query ->
                    Napier.d("📊 REPORTS: Search query changed to '$query', reloading analyses")
                    loadAllAnalyses()
                }
        }
    }

    /**
     * Load all analyses across all patients
     */
    fun loadAllAnalyses(page: Int = 1, perPage: Int = 50) {
        launchWithErrorHandler {
            _uiState.value = ReportsUiState.Loading
            val query = _searchQuery.value
            Napier.d("📊 REPORTS: Loading analyses (page=$page, perPage=$perPage, query='$query')")

            try {
                val response = reportService.getAllAnalyses(
                    page = page,
                    perPage = perPage,
                    searchQuery = query.ifBlank { null }
                )

                Napier.d("📊 REPORTS: Backend response - success=${response.success}, message=${response.message}")

                if (response.success) {
                    val analyses = response.data.analyses
                    _allAnalyses.value = analyses

                    Napier.i("📊 REPORTS: Loaded ${analyses.size} analyses from backend")

                    // Log first 5 analyses for debugging
                    analyses.take(5).forEachIndexed { index, analysis ->
                        Napier.d("📊 REPORTS: Analysis #$index - ID=${analysis.analysis_id}, Patient=${analysis.patient.name} (${analysis.patient.id}), Date=${analysis.date}")
                    }

                    if (query.isNotBlank() && analyses.isEmpty()) {
                        Napier.w("📊 REPORTS: Search query '$query' returned 0 results")
                    }

                    _uiState.value = if (analyses.isEmpty()) {
                        ReportsUiState.Empty
                    } else {
                        ReportsUiState.Success
                    }
                } else {
                    Napier.e("📊 REPORTS: Backend error: ${response.message}")
                    _uiState.value = ReportsUiState.Error(response.message)
                }
            } catch (e: Exception) {
                Napier.e("📊 REPORTS: Exception loading analyses: ${e.message}", e)
                _uiState.value = ReportsUiState.Error(
                    "Failed to load analyses: ${e.message}"
                )
            }
        }
    }

    /**
     * Update search query - debounced reload is handled by Flow in init
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        // Show loading state immediately for visual feedback
        _uiState.value = ReportsUiState.Loading
    }

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
     * View full analysis report data
     */
    fun viewAnalysisReport(analysisId: String) {
        launchWithErrorHandler {
            Napier.d("🔍 VIEWMODEL: Loading analysis report data: $analysisId")

            try {
                val analysisData = reportService.getAnalysisData(analysisId)
                val domainModel = analysisData.toDomainModel()

                // 🔍 LOG 3: Final domain model before UI
                Napier.d("🔍 VIEWMODEL: Final domain model - ${domainModel.detections.size} detections")
                domainModel.detections.take(5).forEachIndexed { index, detection ->
                    Napier.d("🔍 VIEWMODEL: Final Detection #$index - classId=${detection.classId}, className='${detection.className}', isCavity=${detection.isCavity()}, displayName='${detection.getDisplayName()}'")
                }

                _selectedAnalysisReport.value = domainModel
                Napier.i("✅ VIEWMODEL: Analysis report data loaded successfully")
            } catch (e: Exception) {
                Napier.e("❌ VIEWMODEL: Failed to load analysis report data", e)
                _uiState.value = ReportsUiState.Error(
                    "Failed to load report details: ${e.message}"
                )
            }
        }
    }

    /**
     * Close the analysis report viewer
     */
    fun closeAnalysisReportViewer() {
        _selectedAnalysisReport.value = null
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
