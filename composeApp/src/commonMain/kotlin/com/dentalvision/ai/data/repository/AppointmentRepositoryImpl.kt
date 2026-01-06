@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.dentalvision.ai.data.repository

import com.dentalvision.ai.data.remote.api.dto.AppointmentDTO
import com.dentalvision.ai.data.remote.api.dto.CreateAppointmentRequest
import com.dentalvision.ai.data.remote.api.dto.UpdateAppointmentStatusRequest
import com.dentalvision.ai.data.remote.service.AppointmentService
import com.dentalvision.ai.domain.model.Appointment
import com.dentalvision.ai.domain.model.AppointmentStatus
import com.dentalvision.ai.domain.model.AppointmentType
import com.dentalvision.ai.domain.repository.AppointmentRepository
import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AppointmentRepositoryImpl(
    private val appointmentService: AppointmentService
) : AppointmentRepository {

    override suspend fun getAppointments(page: Int, limit: Int): Result<Pair<List<Appointment>, Int>> {
        return try {
            val response = appointmentService.getAllAppointments(page, limit)

            if (response.success && response.data != null) {
                val appointments = response.data.appointments.map { it.toDomainModel() }
                val total = response.data.total ?: appointments.size
                Result.success(Pair(appointments, total))
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Napier.e("Error fetching appointments", e)
            Result.failure(e)
        }
    }

    override suspend fun getPatientAppointments(patientId: String): Result<List<Appointment>> {
        return try {
            val response = appointmentService.getPatientAppointments(patientId)

            if (response.success && response.data != null) {
                val appointments = response.data.appointments.map { it.toDomainModel() }
                Result.success(appointments)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Napier.e("Error fetching patient appointments", e)
            Result.failure(e)
        }
    }

    override suspend fun getAppointmentById(id: String): Result<Appointment> {
        return try {
            val response = appointmentService.getAppointmentById(id)

            if (response.success && response.data != null) {
                Result.success(response.data.toDomainModel())
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Napier.e("Error fetching appointment", e)
            Result.failure(e)
        }
    }

    override suspend fun createAppointment(patientId: String, appointment: Appointment): Result<Appointment> {
        return try {
            // Format date manually to avoid 'Z' timezone that Python can't parse
            val dateTime = appointment.appointmentDate.toLocalDateTime(TimeZone.currentSystemDefault())
            val formattedDate = buildString {
                append(dateTime.year)
                append("-")
                append(dateTime.monthNumber.toString().padStart(2, '0'))
                append("-")
                append(dateTime.dayOfMonth.toString().padStart(2, '0'))
                append("T")
                append(dateTime.hour.toString().padStart(2, '0'))
                append(":")
                append(dateTime.minute.toString().padStart(2, '0'))
                append(":")
                append(dateTime.second.toString().padStart(2, '0'))
            }

            println("DEBUG: Creating appointment with formatted date: $formattedDate")

            val request = CreateAppointmentRequest(
                patientId = patientId,
                appointmentDate = formattedDate,
                appointmentType = appointment.appointmentType.name.lowercase(),
                durationMinutes = appointment.durationMinutes,
                treatmentDescription = appointment.treatmentDescription
            )

            val response = appointmentService.createAppointment(patientId, request)

            if (response.success && response.data != null) {
                println("SUCCESS: Appointment created successfully")
                Result.success(response.data.toDomainModel())
            } else {
                println("ERROR CREATING: ${response.message}")
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            println("ERROR CREATING: ${e.message}")
            Napier.e("Error creating appointment", e)
            Result.failure(e)
        }
    }

    override suspend fun updateAppointment(id: String, appointment: Appointment): Result<Appointment> {
        return try {
            val request = UpdateAppointmentStatusRequest(
                status = appointment.status.name.lowercase(),
                treatmentDescription = appointment.treatmentDescription
            )

            val response = appointmentService.updateAppointmentStatus(
                appointment.patientId,
                id,
                request
            )

            if (response.success && response.data != null) {
                Result.success(response.data.toDomainModel())
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Napier.e("Error updating appointment", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteAppointment(id: String): Result<Unit> {
        return try {
            Result.failure(Exception("Not implemented - use cancelAppointment instead"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelAppointment(id: String): Result<Appointment> {
        return try {
            Result.failure(Exception("Use updateAppointment with CANCELLED status"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun AppointmentDTO.toDomainModel(): Appointment {
        return Appointment(
            id = this.id,
            patientId = this.patientId,
            appointmentDate = parseInstant(this.appointmentDate),
            appointmentType = parseAppointmentType(this.appointmentType),
            status = parseStatus(this.status),
            durationMinutes = this.durationMinutes,
            treatmentDescription = this.treatmentDescription,
            doctorName = this.doctorName,
            clinicLocation = this.clinicLocation,
            createdAt = parseInstant(this.createdAt),
            updatedAt = parseInstant(this.updatedAt)
        )
    }

    private fun parseInstant(timestamp: String): Instant {
        return try {
            Instant.parse(timestamp)
        } catch (e: Exception) {
            Clock.System.now()
        }
    }

    private fun parseStatus(status: String): AppointmentStatus {
        return when (status.lowercase()) {
            "pending" -> AppointmentStatus.PENDING
            "scheduled" -> AppointmentStatus.SCHEDULED
            "confirmed" -> AppointmentStatus.CONFIRMED
            "completed" -> AppointmentStatus.COMPLETED
            "cancelled" -> AppointmentStatus.CANCELLED
            "no_show" -> AppointmentStatus.NO_SHOW
            else -> AppointmentStatus.PENDING
        }
    }

    private fun parseAppointmentType(type: String): AppointmentType {
        return when (type.lowercase()) {
            "general_consultation" -> AppointmentType.GENERAL_CONSULTATION
            "dental_cleaning" -> AppointmentType.DENTAL_CLEANING
            "checkup" -> AppointmentType.CHECKUP
            "ai_analysis" -> AppointmentType.AI_ANALYSIS
            "emergency" -> AppointmentType.EMERGENCY
            "follow_up" -> AppointmentType.FOLLOW_UP
            "treatment" -> AppointmentType.TREATMENT
            else -> AppointmentType.GENERAL_CONSULTATION
        }
    }
}
