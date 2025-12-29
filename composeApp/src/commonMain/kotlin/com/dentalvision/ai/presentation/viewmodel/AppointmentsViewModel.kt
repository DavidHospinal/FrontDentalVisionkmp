package com.dentalvision.ai.presentation.viewmodel

import com.dentalvision.ai.domain.model.Appointment
import com.dentalvision.ai.domain.model.AppointmentStatus
import com.dentalvision.ai.domain.model.AppointmentType
import com.dentalvision.ai.domain.model.Patient
import com.dentalvision.ai.domain.repository.AppointmentRepository
import com.dentalvision.ai.domain.repository.PatientRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class AppointmentsViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<AppointmentsUiState>(AppointmentsUiState.Loading)
    val uiState: StateFlow<AppointmentsUiState> = _uiState.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _recentPatients = MutableStateFlow<List<Patient>>(emptyList())
    val recentPatients: StateFlow<List<Patient>> = _recentPatients.asStateFlow()

    private val _selectedDate = MutableStateFlow<Instant?>(null)
    val selectedDate: StateFlow<Instant?> = _selectedDate.asStateFlow()

    init {
        loadAppointments()
        loadRecentPatients()
    }

    fun loadAppointments() {
        launchWithErrorHandler {
            _uiState.value = AppointmentsUiState.Loading
            Napier.d("Loading all appointments")

            appointmentRepository.getAppointments()
                .onSuccess { (appointmentsList, _) ->
                    _appointments.value = appointmentsList
                    _uiState.value = if (appointmentsList.isEmpty()) {
                        AppointmentsUiState.Empty
                    } else {
                        AppointmentsUiState.Success
                    }
                    Napier.i("Loaded ${appointmentsList.size} appointments")
                }
                .onFailure { error ->
                    Napier.e("Failed to load appointments", error)
                    _uiState.value = AppointmentsUiState.Error(
                        error.message ?: "Failed to load appointments"
                    )
                }
        }
    }

    fun loadRecentPatients() {
        launchWithErrorHandler {
            Napier.d("Loading recent patients for appointments")

            patientRepository.getPatients(page = 1, limit = 100)
                .onSuccess { (patients, _) ->
                    val sortedPatients = patients.sortedByDescending { it.createdAt }
                    _recentPatients.value = sortedPatients
                    Napier.i("Loaded ${sortedPatients.size} patients sorted by creation date (DESC)")
                }
                .onFailure { error ->
                    Napier.e("Failed to load patients", error)
                }
        }
    }

    fun createAppointment(
        patientId: String,
        appointmentDate: Instant,
        appointmentType: AppointmentType,
        observations: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        launchWithErrorHandler {
            Napier.d("Creating appointment for patient: $patientId")

            val appointment = Appointment(
                id = "",
                patientId = patientId,
                appointmentDate = appointmentDate,
                appointmentType = appointmentType,
                status = AppointmentStatus.PENDING,
                durationMinutes = 60,
                treatmentDescription = observations.ifBlank { null },
                doctorName = "Dr. General",
                clinicLocation = "Main Clinic",
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )

            appointmentRepository.createAppointment(patientId, appointment)
                .onSuccess { created ->
                    Napier.i("Appointment created successfully: ${created.id}")
                    loadAppointments()
                    onSuccess()
                }
                .onFailure { error ->
                    Napier.e("Failed to create appointment", error)
                    onError(error.message ?: "Failed to create appointment")
                }
        }
    }

    fun confirmAppointment(
        appointment: Appointment,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        launchWithErrorHandler {
            Napier.d("Confirming appointment: ${appointment.id}")

            val updatedAppointment = appointment.copy(
                status = AppointmentStatus.CONFIRMED,
                updatedAt = Clock.System.now()
            )

            appointmentRepository.updateAppointment(appointment.id, updatedAppointment)
                .onSuccess {
                    Napier.i("Appointment confirmed: ${appointment.id}")
                    loadAppointments()
                    onSuccess()
                }
                .onFailure { error ->
                    Napier.e("Failed to confirm appointment", error)
                    onError(error.message ?: "Failed to confirm appointment")
                }
        }
    }

    fun cancelAppointment(
        appointment: Appointment,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        launchWithErrorHandler {
            Napier.d("Cancelling appointment: ${appointment.id}")

            val updatedAppointment = appointment.copy(
                status = AppointmentStatus.CANCELLED,
                treatmentDescription = "Cancellation reason: $reason",
                updatedAt = Clock.System.now()
            )

            appointmentRepository.updateAppointment(appointment.id, updatedAppointment)
                .onSuccess {
                    Napier.i("Appointment cancelled: ${appointment.id}")
                    loadAppointments()
                    onSuccess()
                }
                .onFailure { error ->
                    Napier.e("Failed to cancel appointment", error)
                    onError(error.message ?: "Failed to cancel appointment")
                }
        }
    }

    fun selectDate(date: Instant) {
        _selectedDate.value = date
        Napier.d("Selected date: $date")
    }

    fun getAppointmentsForDate(date: Instant): List<Appointment> {
        val dateString = date.toString().substring(0, 10)
        return _appointments.value.filter { appointment ->
            appointment.appointmentDate.toString().startsWith(dateString)
        }
    }

    fun getEligiblePatientsForAnalysis(): List<Patient> {
        val eligiblePatientIds = _appointments.value
            .filter {
                it.status == AppointmentStatus.CONFIRMED &&
                it.appointmentType == AppointmentType.AI_ANALYSIS
            }
            .map { it.patientId }
            .toSet()

        val eligiblePatients = _recentPatients.value.filter { it.id in eligiblePatientIds }
        Napier.d("Found ${eligiblePatients.size} eligible patients for AI Analysis")
        return eligiblePatients
    }

    fun refresh() {
        loadAppointments()
        loadRecentPatients()
    }
}

sealed class AppointmentsUiState {
    data object Loading : AppointmentsUiState()
    data object Success : AppointmentsUiState()
    data object Empty : AppointmentsUiState()
    data class Error(val message: String) : AppointmentsUiState()
}
