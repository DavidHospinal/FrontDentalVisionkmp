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
data class PaginationInfo(
    val total: Int,
    val page: Int,
    val per_page: Int,
    val pages: Int? = null,
    val has_next: Boolean? = null,
    val has_prev: Boolean? = null
)

@Serializable
data class PatientListResponse(
    val patients: List<PatientDTO>,
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null,
    val pagination: PaginationInfo? = null  // Support old backend structure
)
