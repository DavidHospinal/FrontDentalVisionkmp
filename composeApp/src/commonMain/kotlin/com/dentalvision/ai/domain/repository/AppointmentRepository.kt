package com.dentalvision.ai.domain.repository

import com.dentalvision.ai.domain.model.Appointment

/**
 * Repository interface for Appointment operations
 * Defines contract for appointment data access
 */
interface AppointmentRepository {

    /**
     * Get all appointments with optional pagination
     * @param page Page number (default: 1)
     * @param limit Items per page (default: 50)
     * @return Result containing list of appointments and total count
     */
    suspend fun getAppointments(
        page: Int = 1,
        limit: Int = 50
    ): Result<Pair<List<Appointment>, Int>>

    /**
     * Get appointments for a specific patient
     * @param patientId Patient ID
     * @return Result containing list of patient appointments
     */
    suspend fun getPatientAppointments(patientId: String): Result<List<Appointment>>

    /**
     * Get appointment by ID
     * @param id Appointment ID
     * @return Result containing appointment or error
     */
    suspend fun getAppointmentById(id: String): Result<Appointment>

    /**
     * Create new appointment
     * @param patientId Patient ID
     * @param appointment Appointment to create
     * @return Result containing created appointment or error
     */
    suspend fun createAppointment(
        patientId: String,
        appointment: Appointment
    ): Result<Appointment>

    /**
     * Update existing appointment
     * @param id Appointment ID
     * @param appointment Updated appointment data
     * @return Result containing updated appointment or error
     */
    suspend fun updateAppointment(
        id: String,
        appointment: Appointment
    ): Result<Appointment>

    /**
     * Delete appointment
     * @param id Appointment ID
     * @return Result success or error
     */
    suspend fun deleteAppointment(id: String): Result<Unit>

    /**
     * Cancel appointment (soft delete)
     * @param id Appointment ID
     * @return Result containing cancelled appointment or error
     */
    suspend fun cancelAppointment(id: String): Result<Appointment>
}
