package com.dentalvision.ai.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppointmentDTO(
    val id: String,
    val patient_id: String,
    val date: String,
    val time: String,
    val status: String,
    val notes: String? = null,
    val created_at: String
)

@Serializable
data class CreateAppointmentDTO(
    val date: String,
    val time: String,
    val status: String = "scheduled",
    val notes: String? = null
)
