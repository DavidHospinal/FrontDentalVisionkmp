package com.dentalvision.ai.data.remote.service

import com.dentalvision.ai.data.remote.api.ApiClient
import com.dentalvision.ai.data.remote.api.ApiClientFactory
import com.dentalvision.ai.data.remote.api.ApiConfig
import com.dentalvision.ai.data.remote.api.dto.ApiResponse
import com.dentalvision.ai.data.remote.api.dto.AppointmentDTO
import com.dentalvision.ai.data.remote.api.dto.CreateAppointmentDTO
import com.dentalvision.ai.data.remote.api.dto.CreatePatientDTO
import com.dentalvision.ai.data.remote.api.dto.PatientDTO
import com.dentalvision.ai.data.remote.api.dto.PatientListResponse

class PatientService(
    private val apiClient: ApiClient = ApiClientFactory.backendClient
) {
    suspend fun getPatients(page: Int = 1, limit: Int = 10): ApiResponse<PatientListResponse> {
        val endpoint = "${ApiConfig.Endpoints.PATIENTS}?page=$page&limit=$limit"
        return apiClient.get(endpoint)
    }

    suspend fun getPatient(id: String): ApiResponse<PatientDTO> {
        val endpoint = "${ApiConfig.Endpoints.PATIENTS}/$id"
        return apiClient.get(endpoint)
    }

    suspend fun createPatient(patient: CreatePatientDTO): ApiResponse<PatientDTO> {
        return apiClient.post(ApiConfig.Endpoints.PATIENTS, patient)
    }

    suspend fun updatePatient(id: String, patient: CreatePatientDTO): ApiResponse<PatientDTO> {
        val endpoint = "${ApiConfig.Endpoints.PATIENTS}/$id"
        return apiClient.put(endpoint, patient)
    }

    suspend fun deletePatient(id: String): ApiResponse<Unit> {
        val endpoint = "${ApiConfig.Endpoints.PATIENTS}/$id"
        return apiClient.delete(endpoint)
    }

    suspend fun searchPatients(query: String): ApiResponse<List<PatientDTO>> {
        val endpoint = "${ApiConfig.Endpoints.PATIENTS}/search"
        return apiClient.post(endpoint, mapOf("query" to query))
    }

    suspend fun getPatientAppointments(patientId: String): ApiResponse<List<AppointmentDTO>> {
        val endpoint = ApiConfig.Endpoints.APPOINTMENTS.replace("{id}", patientId)
        return apiClient.get(endpoint)
    }

    suspend fun createAppointment(
        patientId: String,
        appointment: CreateAppointmentDTO
    ): ApiResponse<AppointmentDTO> {
        val endpoint = ApiConfig.Endpoints.APPOINTMENTS.replace("{id}", patientId)
        return apiClient.post(endpoint, appointment)
    }
}
