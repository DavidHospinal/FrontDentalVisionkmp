package com.dentalvision.ai.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Domain model representing a dental patient
 */
@Serializable
data class Patient(
    val id: String,
    val name: String,
    val age: Int,
    val gender: Gender,
    val email: String? = null,
    val phone: String? = null,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
    val medicalHistory: String? = null,
    val allergies: String? = null,
    val notes: String? = null,
    val synced: Boolean = false
) {
    enum class Gender {
        MALE,
        FEMALE,
        OTHER
    }

    /**
     * Returns formatted patient name with age
     * Example: "John Doe, 32"
     */
    val displayName: String
        get() = "$name, $age"

    /**
     * Returns formatted contact info
     */
    val contactInfo: String
        get() = buildString {
            email?.let { append("📧 $it\n") }
            phone?.let { append("📱 $it") }
        }.trim()

    /**
     * Validates if patient data is complete
     */
    val isValid: Boolean
        get() = name.isNotBlank() && age > 0 && age < 150
}
