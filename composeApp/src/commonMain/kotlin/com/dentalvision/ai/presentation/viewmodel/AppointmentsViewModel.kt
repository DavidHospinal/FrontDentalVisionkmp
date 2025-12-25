package com.dentalvision.ai.presentation.viewmodel

import com.dentalvision.ai.domain.model.Appointment
import com.dentalvision.ai.domain.repository.AppointmentRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for Appointments screen
 * Handles appointment scheduling, listing, and management
 */
class AppointmentsViewModel(
    private val appointmentRepository: AppointmentRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<AppointmentsUiState>(AppointmentsUiState.Loading)
    val uiState: StateFlow<AppointmentsUiState> = _uiState.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    /**
     * Load all appointments
     */
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

    /**
     * Load appointments for a specific patient
     */
    fun loadPatientAppointments(patientId: String) {
        launchWithErrorHandler {
            _uiState.value = AppointmentsUiState.Loading
            Napier.d("Loading appointments for patient: $patientId")

            appointmentRepository.getPatientAppointments(patientId)
                .onSuccess { appointmentsList ->
                    _appointments.value = appointmentsList
                    _uiState.value = if (appointmentsList.isEmpty()) {
                        AppointmentsUiState.Empty
                    } else {
                        AppointmentsUiState.Success
                    }
                    Napier.i("Loaded ${appointmentsList.size} appointments for patient: $patientId")
                }
                .onFailure { error ->
                    Napier.e("Failed to load patient appointments", error)
                    _uiState.value = AppointmentsUiState.Error(
                        error.message ?: "Failed to load appointments"
                    )
                }
        }
    }

    /**
     * Create new appointment
     */
    fun createAppointment(
        patientId: String,
        appointment: Appointment,
        onSuccess: () -> Unit
    ) {
        launchWithErrorHandler {
            _uiState.value = AppointmentsUiState.Loading
            Napier.d("Creating appointment for patient: $patientId")

            appointmentRepository.createAppointment(patientId, appointment)
                .onSuccess { createdAppointment ->
                    Napier.i("Appointment created successfully: ${createdAppointment.id}")
                    loadPatientAppointments(patientId) // Reload
                    onSuccess()
                }
                .onFailure { error ->
                    Napier.e("Failed to create appointment", error)
                    _uiState.value = AppointmentsUiState.Error(
                        error.message ?: "Failed to create appointment"
                    )
                }
        }
    }

    /**
     * Cancel appointment
     */
    fun cancelAppointment(id: String, patientId: String, onSuccess: () -> Unit) {
        launchWithErrorHandler {
            _uiState.value = AppointmentsUiState.Loading
            Napier.d("Cancelling appointment: $id")

            appointmentRepository.cancelAppointment(id)
                .onSuccess { cancelledAppointment ->
                    Napier.i("Appointment cancelled successfully: ${cancelledAppointment.id}")
                    loadPatientAppointments(patientId) // Reload
                    onSuccess()
                }
                .onFailure { error ->
                    Napier.e("Failed to cancel appointment", error)
                    _uiState.value = AppointmentsUiState.Error(
                        error.message ?: "Failed to cancel appointment"
                    )
                }
        }
    }

    /**
     * Refresh appointments list
     */
    fun refresh(patientId: String? = null) {
        if (patientId != null) {
            loadPatientAppointments(patientId)
        } else {
            loadAppointments()
        }
    }
}

/**
 * UI state for Appointments screen
 */
sealed class AppointmentsUiState {
    object Loading : AppointmentsUiState()
    object Success : AppointmentsUiState()
    object Empty : AppointmentsUiState()
    data class Error(val message: String) : AppointmentsUiState()
}
