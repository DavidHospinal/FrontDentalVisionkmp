package com.dentalvision.ai.domain.repository

import com.dentalvision.ai.domain.model.Patient

/**
 * Repository interface for Patient operations
 * Defines contract for patient data access
 */
interface PatientRepository {

    /**
     * Get all patients with pagination
     * @param page Page number (default: 1)
     * @param limit Items per page (default: 50)
     * @return Result containing list of patients and total count
     */
    suspend fun getPatients(
        page: Int = 1,
        limit: Int = 50
    ): Result<Pair<List<Patient>, Int>>

    /**
     * Get patient by ID
     * @param id Patient ID
     * @return Result containing patient or error
     */
    suspend fun getPatientById(id: String): Result<Patient>

    /**
     * Create new patient
     * @param patient Patient to create
     * @return Result containing created patient or error
     */
    suspend fun createPatient(patient: Patient): Result<Patient>

    /**
     * Update existing patient
     * @param id Patient ID
     * @param patient Updated patient data
     * @return Result containing updated patient or error
     */
    suspend fun updatePatient(id: String, patient: Patient): Result<Patient>

    /**
     * Delete patient
     * @param id Patient ID
     * @return Result success or error
     */
    suspend fun deletePatient(id: String): Result<Unit>
}
