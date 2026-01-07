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
import kotlinx.datetime.*

class AppointmentsViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository
) : BaseViewModel() {

    companion object {
        private const val DEFAULT_LIMIT = 15
        private const val DIALOG_PATIENTS_LIMIT = 50
    }

    private val _uiState = MutableStateFlow<AppointmentsUiState>(AppointmentsUiState.Loading)
    val uiState: StateFlow<AppointmentsUiState> = _uiState.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _recentPatients = MutableStateFlow<List<Patient>>(emptyList())
    val recentPatients: StateFlow<List<Patient>> = _recentPatients.asStateFlow()

    private val _selectedDate = MutableStateFlow<Instant?>(null)
    val selectedDate: StateFlow<Instant?> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(Clock.System.now())
    val currentMonth: StateFlow<Instant> = _currentMonth.asStateFlow()

    init {
        loadAppointments()
        loadRecentPatients()
    }

    fun goToNextMonth() {
        val timezone = TimeZone.currentSystemDefault()
        val current = _currentMonth.value.toLocalDateTime(timezone)
        val nextMonth = if (current.month.number == 12) {
            LocalDateTime(current.year + 1, 1, 1, 0, 0, 0)
        } else {
            LocalDateTime(current.year, current.month.number + 1, 1, 0, 0, 0)
        }
        _currentMonth.value = nextMonth.toInstant(timezone)
        Napier.d("Navigated to next month: ${nextMonth.month} ${nextMonth.year}")
    }

    fun goToPreviousMonth() {
        val timezone = TimeZone.currentSystemDefault()
        val current = _currentMonth.value.toLocalDateTime(timezone)
        val previousMonth = if (current.month.number == 1) {
            LocalDateTime(current.year - 1, 12, 1, 0, 0, 0)
        } else {
            LocalDateTime(current.year, current.month.number - 1, 1, 0, 0, 0)
        }
        _currentMonth.value = previousMonth.toInstant(timezone)
        Napier.d("Navigated to previous month: ${previousMonth.month} ${previousMonth.year}")
    }

    fun loadAppointments() {
        launchWithErrorHandler {
            _uiState.value = AppointmentsUiState.Loading
            Napier.d("Loading all appointments")

            appointmentRepository.getAppointments()
                .onSuccess { (appointmentsList, _) ->
                    // Load patient names for appointments
                    val appointmentsWithNames = enrichAppointmentsWithPatientNames(appointmentsList)

                    _appointments.value = appointmentsWithNames
                    _uiState.value = if (appointmentsWithNames.isEmpty()) {
                        AppointmentsUiState.Empty
                    } else {
                        AppointmentsUiState.Success
                    }
                    Napier.i("Loaded ${appointmentsWithNames.size} appointments with patient names")
                }
                .onFailure { error ->
                    Napier.e("Failed to load appointments", error)
                    _uiState.value = AppointmentsUiState.Error(
                        error.message ?: "Failed to load appointments"
                    )
                }
        }
    }

    private suspend fun enrichAppointmentsWithPatientNames(appointments: List<Appointment>): List<Appointment> {
        if (appointments.isEmpty()) return appointments

        val patientsResult = patientRepository.getPatients(page = 1, limit = DEFAULT_LIMIT)
        val patientNameMap = patientsResult.getOrNull()?.first?.associate { it.id to it.name } ?: emptyMap()

        return appointments.map { appointment ->
            val patientName = patientNameMap[appointment.patientId] ?: "Patient ${appointment.patientId}"
            appointment.copy(patientName = patientName)
        }
    }

    fun loadRecentPatients(limit: Int = DIALOG_PATIENTS_LIMIT) {
        launchWithErrorHandler {
            Napier.d("Loading recent patients for appointments (limit: $limit)")

            patientRepository.getPatients(page = 1, limit = limit)
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

                    // Enrich with patient name from recentPatients cache
                    val patientName = _recentPatients.value
                        .find { it.id == patientId }
                        ?.name
                        ?: "Patient $patientId"

                    val enrichedAppointment = created.copy(patientName = patientName)

                    val currentList = _appointments.value
                    val updatedList = listOf(enrichedAppointment) + currentList
                    _appointments.value = updatedList

                    _uiState.value = AppointmentsUiState.Success

                    loadRecentPatients()

                    Napier.d("📋 New appointment added to sidebar: ${enrichedAppointment.id} for ${patientName}")
                    Napier.d("📋 Appointment appears in sidebar as PENDING - ready for confirmation")
                    Napier.d("📋 Total appointments in list: ${updatedList.size}")
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
        if (appointment.status == AppointmentStatus.CONFIRMED) {
            Napier.d("Appointment ${appointment.id} already confirmed, ignoring duplicate request")
            return
        }

        launchWithErrorHandler {
            Napier.d("Confirming appointment: ${appointment.id}")

            val updatedAppointment = appointment.copy(
                status = AppointmentStatus.CONFIRMED,
                updatedAt = Clock.System.now()
            )

            val currentList = _appointments.value
            val optimisticList = currentList.map {
                if (it.id == appointment.id) updatedAppointment else it
            }
            _appointments.value = optimisticList

            Napier.d("Optimistic update applied for appointment ${appointment.id}")

            appointmentRepository.updateAppointment(appointment.id, updatedAppointment)
                .onSuccess {
                    Napier.i("Appointment confirmed: ${appointment.id}")
                    onSuccess()
                }
                .onFailure { error ->
                    Napier.e("Failed to confirm appointment", error)

                    _appointments.value = currentList
                    Napier.w("Rollback: Reverted appointment ${appointment.id} to original state")

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

            val currentList = _appointments.value
            val optimisticList = currentList.map {
                if (it.id == appointment.id) updatedAppointment else it
            }
            _appointments.value = optimisticList

            Napier.d("Optimistic update applied for cancellation of appointment ${appointment.id}")

            appointmentRepository.updateAppointment(appointment.id, updatedAppointment)
                .onSuccess {
                    Napier.i("Appointment cancelled: ${appointment.id}")
                    onSuccess()
                }
                .onFailure { error ->
                    Napier.e("Failed to cancel appointment", error)

                    _appointments.value = currentList
                    Napier.w("Rollback: Reverted appointment ${appointment.id} to original state")

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

    /**
     * Reset appointment form state
     * Call this when opening the new appointment dialog to ensure fresh state
     */
    fun resetAppointmentForm() {
        Napier.d("Resetting appointment form state - reloading patients")
        loadRecentPatients() // Force fresh reload of patient list
    }
}

sealed class AppointmentsUiState {
    data object Loading : AppointmentsUiState()
    data object Success : AppointmentsUiState()
    data object Empty : AppointmentsUiState()
    data class Error(val message: String) : AppointmentsUiState()
}
