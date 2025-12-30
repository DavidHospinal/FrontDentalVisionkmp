package com.dentalvision.ai.data.remote.service

import com.dentalvision.ai.data.remote.api.ApiClient
import com.dentalvision.ai.data.remote.api.ApiClientFactory
import com.dentalvision.ai.data.remote.api.ApiConfig
import com.dentalvision.ai.data.remote.api.dto.AnalysisRequest
import com.dentalvision.ai.data.remote.api.dto.ApiResponse
import com.dentalvision.ai.data.remote.api.dto.DentalAnalysisDTO

class AnalysisService(
    private val backendClient: ApiClient = ApiClientFactory.backendClient,
    private val huggingFaceClient: ApiClient = ApiClientFactory.huggingFaceClient
) {
    /**
     * Submit dental image for AI analysis
     * Calls backend endpoint which processes with YOLO and generates sequential ID
     */
    suspend fun submitAnalysis(
        patientId: String,
        imageData: ByteArray,
        imageName: String,
        confidence: Double = 0.25
    ): ApiResponse<DentalAnalysisDTO> {
        val endpoint = ApiConfig.Endpoints.ANALYSIS
        // TODO: Implement multipart/form-data upload
        // For now, use existing Gradio-based flow
        throw NotImplementedError("Use AnalysisRepository.submitAnalysis instead")
    }

    suspend fun getAnalysis(id: String): ApiResponse<DentalAnalysisDTO> {
        val endpoint = "${ApiConfig.Endpoints.ANALYSIS}/$id"
        return backendClient.get(endpoint)
    }

    suspend fun getPatientAnalyses(patientId: String): ApiResponse<List<DentalAnalysisDTO>> {
        val endpoint = "${ApiConfig.Endpoints.ANALYSIS}/patient/$patientId"
        return backendClient.get(endpoint)
    }

    suspend fun getAnalysisDetails(id: String): ApiResponse<DentalAnalysisDTO> {
        val endpoint = "${ApiConfig.Endpoints.ANALYSIS}/$id/details"
        return backendClient.get(endpoint)
    }

    suspend fun updateAnalysis(id: String, notes: String): ApiResponse<DentalAnalysisDTO> {
        val endpoint = "${ApiConfig.Endpoints.ANALYSIS}/$id/update"
        return backendClient.put(endpoint, mapOf("notes" to notes))
    }
}
