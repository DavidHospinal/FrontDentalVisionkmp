package com.dentalvision.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    val id: String,
    val patient_id: String,
    val date: String,
    val time: String,
    val status: AppointmentStatus,
    val notes: String? = null,
    val created_at: String
)

@Serializable
enum class AppointmentStatus {
    scheduled,
    completed,
    cancelled
}
