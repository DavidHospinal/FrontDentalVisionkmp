package com.dentalvision.ai.data.repository

import com.dentalvision.ai.data.remote.api.dto.CreatePatientDTO
import com.dentalvision.ai.data.remote.api.dto.PatientDTO
import com.dentalvision.ai.data.remote.service.PatientService
import com.dentalvision.ai.domain.model.Patient
import com.dentalvision.ai.domain.repository.PatientRepository
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Implementation of PatientRepository
 * Handles patient CRUD operations using Backend API
 */
class PatientRepositoryImpl(
    private val patientService: PatientService
) : PatientRepository {

    /**
     * Get all patients with pagination
     */
    override suspend fun getPatients(page: Int, limit: Int): Result<Pair<List<Patient>, Int>> {
        return try {
            Napier.d("Fetching patients: page=$page, limit=$limit")

            val response = patientService.getPatients(page, limit)

            when (response) {
                is com.dentalvision.ai.domain.model.ApiResponse.Success -> {
                    val patients = response.data.patients.map { it.toDomainModel() }
                    val total = response.data.total
                    Napier.i("Successfully fetched ${patients.size} patients (total: $total)")
                    Result.success(Pair(patients, total))
                }
                is com.dentalvision.ai.domain.model.ApiResponse.Error -> {
                    Napier.e("Failed to fetch patients: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Napier.e("Error fetching patients", e)
            Result.failure(e)
        }
    }

    /**
     * Get patient by ID
     */
    override suspend fun getPatientById(id: String): Result<Patient> {
        return try {
            Napier.d("Fetching patient by ID: $id")

            val response = patientService.getPatient(id)

            when (response) {
                is com.dentalvision.ai.domain.model.ApiResponse.Success -> {
                    val patient = response.data.toDomainModel()
                    Napier.i("Successfully fetched patient: ${patient.name}")
                    Result.success(patient)
                }
                is com.dentalvision.ai.domain.model.ApiResponse.Error -> {
                    Napier.e("Failed to fetch patient: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Napier.e("Error fetching patient by ID: $id", e)
            Result.failure(e)
        }
    }

    /**
     * Create new patient
     */
    override suspend fun createPatient(patient: Patient): Result<Patient> {
        return try {
            Napier.d("Creating patient: ${patient.name}")

            val createDTO = CreatePatientDTO(
                name = patient.name,
                age = patient.age,
                gender = patient.gender.name,
                email = patient.email ?: "",
                phone = patient.phone ?: ""
            )

            val response = patientService.createPatient(createDTO)

            when (response) {
                is com.dentalvision.ai.domain.model.ApiResponse.Success -> {
                    val createdPatient = response.data.toDomainModel()
                    Napier.i("Successfully created patient: ${createdPatient.id}")
                    Result.success(createdPatient)
                }
                is com.dentalvision.ai.domain.model.ApiResponse.Error -> {
                    Napier.e("Failed to create patient: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Napier.e("Error creating patient", e)
            Result.failure(e)
        }
    }

    /**
     * Update existing patient
     */
    override suspend fun updatePatient(id: String, patient: Patient): Result<Patient> {
        return try {
            Napier.d("Updating patient: $id")

            val updateDTO = CreatePatientDTO(
                name = patient.name,
                age = patient.age,
                gender = patient.gender.name,
                email = patient.email ?: "",
                phone = patient.phone ?: ""
            )

            val response = patientService.updatePatient(id, updateDTO)

            when (response) {
                is com.dentalvision.ai.domain.model.ApiResponse.Success -> {
                    val updatedPatient = response.data.toDomainModel()
                    Napier.i("Successfully updated patient: ${updatedPatient.id}")
                    Result.success(updatedPatient)
                }
                is com.dentalvision.ai.domain.model.ApiResponse.Error -> {
                    Napier.e("Failed to update patient: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Napier.e("Error updating patient: $id", e)
            Result.failure(e)
        }
    }

    /**
     * Delete patient
     */
    override suspend fun deletePatient(id: String): Result<Unit> {
        return try {
            Napier.d("Deleting patient: $id")

            val response = patientService.deletePatient(id)

            when (response) {
                is com.dentalvision.ai.domain.model.ApiResponse.Success -> {
                    Napier.i("Successfully deleted patient: $id")
                    Result.success(Unit)
                }
                is com.dentalvision.ai.domain.model.ApiResponse.Error -> {
                    Napier.e("Failed to delete patient: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Napier.e("Error deleting patient: $id", e)
            Result.failure(e)
        }
    }

    /**
     * Convert PatientDTO to domain Patient model
     */
    private fun PatientDTO.toDomainModel(): Patient {
        return Patient(
            id = this.id,
            name = this.name,
            age = this.age,
            gender = parseGender(this.gender),
            email = this.email,
            phone = this.phone,
            createdAt = parseInstant(this.created_at),
            updatedAt = parseInstant(this.updated_at),
            medicalHistory = null,
            allergies = null,
            notes = null,
            synced = true
        )
    }

    /**
     * Parse gender string to Patient.Gender enum
     */
    private fun parseGender(gender: String): Patient.Gender {
        return when (gender.uppercase()) {
            "M", "MALE" -> Patient.Gender.MALE
            "F", "FEMALE" -> Patient.Gender.FEMALE
            else -> Patient.Gender.OTHER
        }
    }

    /**
     * Parse ISO 8601 timestamp to Instant
     */
    private fun parseInstant(timestamp: String): Instant {
        return try {
            Instant.parse(timestamp)
        } catch (e: Exception) {
            Clock.System.now()
        }
    }
}
