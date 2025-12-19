package com.dentalvision.ai.data.remote.service

import com.dentalvision.ai.data.remote.api.ApiClient
import com.dentalvision.ai.data.remote.api.ApiClientFactory
import com.dentalvision.ai.data.remote.api.ApiConfig
import com.dentalvision.ai.data.remote.api.dto.ApiResponse
import com.dentalvision.ai.data.remote.api.dto.SystemStatisticsDTO

class SystemService(
    private val apiClient: ApiClient = ApiClientFactory.backendClient
) {
    suspend fun getSystemStatistics(): ApiResponse<SystemStatisticsDTO> {
        return apiClient.get(ApiConfig.Endpoints.SYSTEM_STATS)
    }

    suspend fun healthCheck(): ApiResponse<Map<String, String>> {
        return apiClient.get("/health")
    }
}
