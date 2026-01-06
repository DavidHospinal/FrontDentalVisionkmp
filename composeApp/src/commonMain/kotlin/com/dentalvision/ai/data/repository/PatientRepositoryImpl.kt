@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.dentalvision.ai.data.repository

import com.dentalvision.ai.data.remote.api.dto.CreatePatientDTO
import com.dentalvision.ai.data.remote.api.dto.PatientDTO
import com.dentalvision.ai.data.remote.service.PatientService
import com.dentalvision.ai.domain.model.Patient
import com.dentalvision.ai.domain.repository.PatientRepository
import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlinx.datetime.Instant

class PatientRepositoryImpl(
    private val patientService: PatientService
) : PatientRepository {

    override suspend fun getPatients(page: Int, limit: Int, searchQuery: String?): Result<Pair<List<Patient>, Int>> {
        return try {
            val searchLog = if (!searchQuery.isNullOrBlank()) " search='$searchQuery'" else ""
            Napier.d("Fetching patients: page=$page, limit=$limit$searchLog")

            val response = patientService.getPatients(page, limit, searchQuery)

            if (response.success && response.data != null) {
                val patients = response.data.patients.map { it.toDomainModel() }
                // Support both old and new backend response structures
                // Try: new structure (data.total) -> old structure (data.pagination.total) -> fallback (patients.size)
                val total = response.data.total ?: response.data.pagination?.total ?: patients.size
                Napier.i("Successfully fetched ${patients.size} patients (total: $total)")
                Result.success(Pair(patients, total))
            } else {
                Napier.e("Failed to fetch patients: ${response.message ?: response.error}")
                Result.failure(Exception(response.message ?: response.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Napier.e("Error fetching patients", e)
            Result.failure(e)
        }
    }

    override suspend fun getPatientById(id: String): Result<Patient> {
        return try {
            Napier.d("Fetching patient by ID: $id")

            val response = patientService.getPatient(id)

            if (response.success && response.data != null) {
                val patient = response.data.toDomainModel()
                Napier.i("Successfully fetched patient: ${patient.name}")
                Result.success(patient)
            } else {
                Napier.e("Failed to fetch patient: ${response.message ?: response.error}")
                Result.failure(Exception(response.message ?: response.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Napier.e("Error fetching patient by ID: $id", e)
            Result.failure(e)
        }
    }

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

            if (response.success && response.data != null) {
                val createdPatient = response.data.toDomainModel()
                Napier.i("Successfully created patient: ${createdPatient.id}")
                Result.success(createdPatient)
            } else {
                Napier.e("Failed to create patient: ${response.message ?: response.error}")
                Result.failure(Exception(response.message ?: response.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Napier.e("Error creating patient", e)
            Result.failure(e)
        }
    }

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

            if (response.success && response.data != null) {
                val updatedPatient = response.data.toDomainModel()
                Napier.i("Successfully updated patient: ${updatedPatient.id}")
                Result.success(updatedPatient)
            } else {
                Napier.e("Failed to update patient: ${response.message ?: response.error}")
                Result.failure(Exception(response.message ?: response.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Napier.e("Error updating patient: $id", e)
            Result.failure(e)
        }
    }

    override suspend fun deletePatient(id: String): Result<Unit> {
        return try {
            Napier.d("Deleting patient: $id")

            val response = patientService.deletePatient(id)

            if (response.success) {
                Napier.i("Successfully deleted patient: $id")
                Result.success(Unit)
            } else {
                Napier.e("Failed to delete patient: ${response.message ?: response.error}")
                Result.failure(Exception(response.message ?: response.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Napier.e("Error deleting patient: $id", e)
            Result.failure(e)
        }
    }

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

    private fun parseGender(gender: String): Patient.Gender {
        return when (gender.uppercase()) {
            "M", "MALE" -> Patient.Gender.MALE
            "F", "FEMALE" -> Patient.Gender.FEMALE
            else -> Patient.Gender.OTHER
        }
    }

    private fun parseInstant(timestamp: String): Instant {
        return try {
            Instant.parse(timestamp)
        } catch (e: Exception) {
            Clock.System.now()
        }
    }
}
