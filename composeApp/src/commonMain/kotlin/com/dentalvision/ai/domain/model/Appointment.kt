package com.dentalvision.ai.domain.model

import kotlinx.datetime.Instant

data class Appointment(
    val id: String,
    val patientId: String,
    val patientName: String? = null, // Patient name for UI display
    val appointmentDate: Instant,
    val appointmentType: AppointmentType,
    val status: AppointmentStatus,
    val durationMinutes: Int = 60,
    val treatmentDescription: String? = null,
    val doctorName: String? = null,
    val clinicLocation: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class AppointmentStatus(val displayName: String) {
    PENDING("Pending"),
    SCHEDULED("Scheduled"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    NO_SHOW("No Show")
}

enum class AppointmentType(val displayName: String) {
    GENERAL_CONSULTATION("General Consultation"),
    DENTAL_CLEANING("Dental Cleaning"),
    CHECKUP("Checkup"),
    AI_ANALYSIS("AI Analysis"),
    EMERGENCY("Emergency"),
    FOLLOW_UP("Follow-up/Control"),
    TREATMENT("Treatment")
}
