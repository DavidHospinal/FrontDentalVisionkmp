package com.dentalvision.ai.data.remote.api

import kotlinx.serialization.Serializable

/**
 * Request DTO for creating a new patient
 * Used when submitting patient data to the backend API
 */
@Serializable
data class CreatePatientRequest(
    val name: String,
    val age: Int,
    val gender: String,
    val email: String? = null,
    val phone: String? = null,
    val medicalHistory: String? = null,
    val allergies: String? = null,
    val notes: String? = null
)

/**
 * Request DTO for updating an existing patient
 * Same structure as CreatePatientRequest
 */
typealias UpdatePatientRequest = CreatePatientRequest
