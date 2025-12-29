package com.dentalvision.ai.data.remote.service

import com.dentalvision.ai.data.remote.api.ApiClient
import com.dentalvision.ai.data.remote.api.ApiClientFactory
import com.dentalvision.ai.data.remote.api.ApiConfig
import com.dentalvision.ai.data.remote.api.dto.ApiResponse
import com.dentalvision.ai.data.remote.api.dto.GenerateReportRequest
import com.dentalvision.ai.data.remote.api.dto.ReportDTO
import com.dentalvision.ai.data.remote.api.dto.AnalysisReportDTO

class ReportService(
    private val apiClient: ApiClient = ApiClientFactory.backendClient
) {
    suspend fun generateReport(request: GenerateReportRequest): ApiResponse<ReportDTO> {
        val endpoint = "${ApiConfig.Endpoints.REPORTS}/generate"
        return apiClient.post(endpoint, request)
    }

    suspend fun getReports(page: Int = 1, limit: Int = 10): ApiResponse<List<ReportDTO>> {
        val endpoint = "${ApiConfig.Endpoints.REPORTS}?page=$page&limit=$limit"
        return apiClient.get(endpoint)
    }

    suspend fun getReport(id: String): ApiResponse<ReportDTO> {
        val endpoint = "${ApiConfig.Endpoints.REPORTS}/$id"
        return apiClient.get(endpoint)
    }

    suspend fun downloadReport(id: String): ByteArray {
        val endpoint = "${ApiConfig.Endpoints.REPORTS}/$id/download"
        return apiClient.get(endpoint)
    }

    suspend fun deleteReport(id: String): ApiResponse<Unit> {
        val endpoint = "${ApiConfig.Endpoints.REPORTS}/$id"
        return apiClient.delete(endpoint)
    }

    suspend fun getPatientReports(patientId: String): ApiResponse<List<ReportDTO>> {
        val endpoint = "${ApiConfig.Endpoints.REPORTS}/patient/$patientId"
        return apiClient.get(endpoint)
    }

    suspend fun cleanupReports(days: Int = 30): ApiResponse<Unit> {
        val endpoint = "${ApiConfig.Endpoints.REPORTS}/cleanup"
        return apiClient.post(endpoint, mapOf("days" to days))
    }

    /**
     * Get complete analysis JSON data for report viewer
     */
    suspend fun getAnalysisData(analysisId: String): AnalysisReportDTO {
        val endpoint = "${ApiConfig.Endpoints.REPORTS}/analysis/$analysisId/data"
        return apiClient.get(endpoint)
    }
}
