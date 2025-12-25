package com.dentalvision.ai.presentation.viewmodel

import com.dentalvision.ai.domain.model.Patient
import com.dentalvision.ai.domain.repository.PatientRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for Patients screen
 * Handles patient CRUD operations and UI state
 */
class PatientsViewModel(
    private val patientRepository: PatientRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<PatientsUiState>(PatientsUiState.Loading)
    val uiState: StateFlow<PatientsUiState> = _uiState.asStateFlow()

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var currentPage = 1
    private var totalPatients = 0

    init {
        loadPatients()
    }

    /**
     * Load patients with pagination
     */
    fun loadPatients(page: Int = 1) {
        launchWithErrorHandler {
            _uiState.value = PatientsUiState.Loading
            Napier.d("Loading patients: page=$page")

            patientRepository.getPatients(page = page, limit = 50)
                .onSuccess { (patientsList, total) ->
                    _patients.value = patientsList
                    currentPage = page
                    totalPatients = total
                    _uiState.value = if (patientsList.isEmpty()) {
                        PatientsUiState.Empty
                    } else {
                        PatientsUiState.Success
                    }
                    Napier.i("Loaded ${patientsList.size} patients (total: $total)")
                }
                .onFailure { error ->
                    Napier.e("Failed to load patients", error)
                    _uiState.value = PatientsUiState.Error(
                        error.message ?: "Failed to load patients"
                    )
                }
        }
    }

    /**
     * Search patients by query
     */
    fun searchPatients(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            loadPatients()
            return
        }

        launchWithErrorHandler {
            _uiState.value = PatientsUiState.Loading

            // Filter locally for now
            // TODO: Implement backend search when available
            val filtered = _patients.value.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.id.contains(query, ignoreCase = true) ||
                it.email?.contains(query, ignoreCase = true) == true ||
                it.phone?.contains(query, ignoreCase = true) == true
            }

            _uiState.value = if (filtered.isEmpty()) {
                PatientsUiState.Empty
            } else {
                PatientsUiState.Success
            }

            Napier.d("Search results: ${filtered.size} patients")
        }
    }

    /**
     * Create new patient
     */
    fun createPatient(patient: Patient, onSuccess: () -> Unit) {
        launchWithErrorHandler {
            _uiState.value = PatientsUiState.Loading
            Napier.d("Creating patient: ${patient.name}")

            patientRepository.createPatient(patient)
                .onSuccess { createdPatient ->
                    Napier.i("Patient created successfully: ${createdPatient.id}")
                    loadPatients(currentPage) // Reload current page
                    onSuccess()
                }
                .onFailure { error ->
                    Napier.e("Failed to create patient", error)
                    _uiState.value = PatientsUiState.Error(
                        error.message ?: "Failed to create patient"
                    )
                }
        }
    }

    /**
     * Update existing patient
     */
    fun updatePatient(id: String, patient: Patient, onSuccess: () -> Unit) {
        launchWithErrorHandler {
            _uiState.value = PatientsUiState.Loading
            Napier.d("Updating patient: $id")

            patientRepository.updatePatient(id, patient)
                .onSuccess { updatedPatient ->
                    Napier.i("Patient updated successfully: ${updatedPatient.id}")
                    loadPatients(currentPage) // Reload current page
                    onSuccess()
                }
                .onFailure { error ->
                    Napier.e("Failed to update patient", error)
                    _uiState.value = PatientsUiState.Error(
                        error.message ?: "Failed to update patient"
                    )
                }
        }
    }

    /**
     * Delete patient
     */
    fun deletePatient(id: String, onSuccess: () -> Unit) {
        launchWithErrorHandler {
            _uiState.value = PatientsUiState.Loading
            Napier.d("Deleting patient: $id")

            patientRepository.deletePatient(id)
                .onSuccess {
                    Napier.i("Patient deleted successfully: $id")
                    loadPatients(currentPage) // Reload current page
                    onSuccess()
                }
                .onFailure { error ->
                    Napier.e("Failed to delete patient", error)
                    _uiState.value = PatientsUiState.Error(
                        error.message ?: "Failed to delete patient"
                    )
                }
        }
    }

    /**
     * Reload current page
     */
    fun refresh() {
        loadPatients(currentPage)
    }

    /**
     * Load next page
     */
    fun loadNextPage() {
        if (hasNextPage()) {
            loadPatients(currentPage + 1)
        }
    }

    /**
     * Load previous page
     */
    fun loadPreviousPage() {
        if (currentPage > 1) {
            loadPatients(currentPage - 1)
        }
    }

    /**
     * Check if there are more pages
     */
    private fun hasNextPage(): Boolean {
        return _patients.value.size * currentPage < totalPatients
    }
}

/**
 * UI state for Patients screen
 */
sealed class PatientsUiState {
    object Loading : PatientsUiState()
    object Success : PatientsUiState()
    object Empty : PatientsUiState()
    data class Error(val message: String) : PatientsUiState()
}
