package com.dentalvision.ai.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentDTO(
    val id: String,
    @SerialName("patient_id")
    val patientId: String,
    @SerialName("appointment_date")
    val appointmentDate: String,
    @SerialName("appointment_type")
    val appointmentType: String,
    val status: String,
    @SerialName("duration_minutes")
    val durationMinutes: Int = 60,
    @SerialName("treatment_description")
    val treatmentDescription: String? = null,
    @SerialName("doctor_name")
    val doctorName: String? = null,
    @SerialName("clinic_location")
    val clinicLocation: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class CreateAppointmentRequest(
    @SerialName("patient_id")
    val patientId: String,
    @SerialName("appointment_date")
    val appointmentDate: String,
    @SerialName("appointment_type")
    val appointmentType: String,
    @SerialName("duration_minutes")
    val durationMinutes: Int = 60,
    @SerialName("treatment_description")
    val treatmentDescription: String? = null,
    @SerialName("doctor_name")
    val doctorName: String? = "Dr. General",
    @SerialName("clinic_location")
    val clinicLocation: String? = "Main Clinic"
)

@Serializable
data class UpdateAppointmentStatusRequest(
    val status: String,
    @SerialName("treatment_description")
    val treatmentDescription: String? = null
)

@Serializable
data class AppointmentListResponse(
    val appointments: List<AppointmentDTO>,
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null
)

enum class AppointmentStatus(val value: String, val displayName: String) {
    PENDING("pending", "Pending"),
    SCHEDULED("scheduled", "Scheduled"),
    CONFIRMED("confirmed", "Confirmed"),
    COMPLETED("completed", "Completed"),
    CANCELLED("cancelled", "Cancelled"),
    NO_SHOW("no_show", "No Show");

    companion object {
        fun fromValue(value: String): AppointmentStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

enum class ConsultationType(val value: String, val displayName: String) {
    GENERAL_CONSULTATION("general_consultation", "General Consultation"),
    DENTAL_CLEANING("dental_cleaning", "Dental Cleaning"),
    CHECKUP("checkup", "Checkup"),
    AI_ANALYSIS("ai_analysis", "AI Analysis"),
    EMERGENCY("emergency", "Emergency"),
    FOLLOW_UP("follow_up", "Follow-up/Control"),
    TREATMENT("treatment", "Treatment");

    companion object {
        fun fromValue(value: String): ConsultationType {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: GENERAL_CONSULTATION
        }
    }
}
