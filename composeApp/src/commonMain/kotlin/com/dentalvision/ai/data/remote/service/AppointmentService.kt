package com.dentalvision.ai.data.remote.service

import com.dentalvision.ai.data.remote.api.ApiClient
import com.dentalvision.ai.data.remote.api.ApiClientFactory
import com.dentalvision.ai.data.remote.api.dto.ApiResponse
import com.dentalvision.ai.data.remote.api.dto.AppointmentDTO
import com.dentalvision.ai.data.remote.api.dto.AppointmentListResponse
import com.dentalvision.ai.data.remote.api.dto.CreateAppointmentRequest
import com.dentalvision.ai.data.remote.api.dto.UpdateAppointmentStatusRequest

class AppointmentService(
    private val apiClient: ApiClient = ApiClientFactory.backendClient
) {
    suspend fun getPatientAppointments(patientId: String): ApiResponse<AppointmentListResponse> {
        val endpoint = "/api/v1/patients/$patientId/appointments"
        return apiClient.get(endpoint)
    }

    suspend fun createAppointment(
        patientId: String,
        request: CreateAppointmentRequest
    ): ApiResponse<AppointmentDTO> {
        val endpoint = "/api/v1/patients/$patientId/appointments"
        return apiClient.post(endpoint, request)
    }

    suspend fun updateAppointmentStatus(
        patientId: String,
        appointmentId: String,
        request: UpdateAppointmentStatusRequest
    ): ApiResponse<AppointmentDTO> {
        val endpoint = "/api/v1/patients/$patientId/appointments/$appointmentId"
        return apiClient.put(endpoint, request)
    }

    suspend fun deleteAppointment(
        patientId: String,
        appointmentId: String
    ): ApiResponse<Unit> {
        val endpoint = "/api/v1/patients/$patientId/appointments/$appointmentId"
        return apiClient.delete(endpoint)
    }

    suspend fun getAllAppointments(
        page: Int = 1,
        limit: Int = 50,
        status: String? = null
    ): ApiResponse<AppointmentListResponse> {
        val params = mutableListOf("page=$page", "limit=$limit")
        if (status != null) {
            params.add("status=$status")
        }
        val endpoint = "/api/v1/appointments?${params.joinToString("&")}"
        return apiClient.get(endpoint)
    }

    suspend fun getAppointmentById(appointmentId: String): ApiResponse<AppointmentDTO> {
        val endpoint = "/api/v1/appointments/$appointmentId"
        return apiClient.get(endpoint)
    }

    suspend fun getEligiblePatientsForAnalysis(): ApiResponse<List<AppointmentDTO>> {
        val endpoint = "/api/v1/appointments?status=confirmed&type=ai_analysis"
        return apiClient.get(endpoint)
    }
}
