package com.dentalvision.ai.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class PatientDTO(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val email: String? = null,
    val phone: String? = null,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class CreatePatientDTO(
    val name: String,
    val age: Int,
    val gender: String,
    val email: String,
    val phone: String
)

@Serializable
data class PatientListResponse(
    val patients: List<PatientDTO>,
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null
)
