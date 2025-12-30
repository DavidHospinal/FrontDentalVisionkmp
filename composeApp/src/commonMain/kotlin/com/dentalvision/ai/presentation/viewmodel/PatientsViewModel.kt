package com.dentalvision.ai.presentation.viewmodel

import com.dentalvision.ai.domain.model.Patient
import com.dentalvision.ai.domain.repository.PatientRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for Patients screen
 * Handles patient CRUD operations and UI state
 * ROBUST ERROR HANDLING - Shows REAL errors, NO demo data fallback
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
        loadPatients(1)
    }

    /**
     * Load patients with pagination and search
     * ROBUST ERROR HANDLING - Shows REAL errors, NO demo data fallback
     */
    fun loadPatients(page: Int = 1, searchQuery: String? = null) {
        currentPage = page

        launchWithErrorHandler {
            try {
                _uiState.value = PatientsUiState.Loading
                val query = searchQuery ?: _searchQuery.value.ifBlank { null }

                patientRepository.getPatients(page = page, limit = 50, searchQuery = query)
                    .onSuccess { response ->
                        val list = response.first
                        val total = response.second

                        totalPatients = total
                        _patients.value = list

                        _uiState.value = if (list.isEmpty()) {
                            PatientsUiState.Empty
                        } else {
                            PatientsUiState.Success
                        }
                        val searchLog = if (query != null) " (search='$query')" else ""
                        Napier.i("✅ SUCCESS: Loaded ${list.size} patients from REAL backend (Total: $total, Page: $page)$searchLog")
                    }
                    .onFailure { error ->
                        // Backend returned error - show REAL error to user, NO demo data
                        val searchParam = if (query != null) "&search=$query" else ""
                        val errorMessage = buildErrorMessage(
                            operation = "Backend API Call",
                            error = error,
                            context = "GET /patients?page=$page&per_page=50$searchParam"
                        )
                        Napier.e("❌ BACKEND ERROR: ${error.message}", error)
                        _uiState.value = PatientsUiState.Error(errorMessage)
                        _patients.value = emptyList() // NO fallback to demo data
                    }
            } catch (exception: Exception) {
                // Catch unexpected errors - show REAL error to user
                val errorMessage = buildErrorMessage(
                    operation = "Load Patients",
                    error = exception,
                    context = "Fetching page $page from backend"
                )
                Napier.e("❌ EXCEPTION: Load Patients Failed", exception)
                _uiState.value = PatientsUiState.Error(errorMessage)
                _patients.value = emptyList() // Clear patients, NO demo data
            }
        }
    }

    /**
     * Search patients by query (backend search)
     */
    fun searchPatients(query: String) {
        _searchQuery.value = query
        // Call loadPatients with search query - backend will filter
        loadPatients(page = 1, searchQuery = query.ifBlank { null })
    }

    fun createPatient(patient: Patient, onSuccess: () -> Unit) {
        launchWithErrorHandler {
            try {
                _uiState.value = PatientsUiState.Loading

                patientRepository.createPatient(patient)
                    .onSuccess {
                        Napier.i("✅ Patient created successfully: ${patient.name}")
                        loadPatients(currentPage)
                        onSuccess()
                    }
                    .onFailure { error ->
                        val errorMessage = buildErrorMessage(
                            operation = "Create Patient",
                            error = error,
                            context = "Backend rejected patient creation"
                        )
                        Napier.e("❌ BACKEND ERROR: Create failed - ${error.message}", error)
                        _uiState.value = PatientsUiState.Error(errorMessage)
                    }
            } catch (exception: Exception) {
                val errorMessage = buildErrorMessage(
                    operation = "Create Patient",
                    error = exception,
                    context = "POST /patients - ${patient.name}"
                )
                Napier.e("❌ CREATE FAILED: ${exception.message}", exception)
                _uiState.value = PatientsUiState.Error(errorMessage)
            }
        }
    }

    fun updatePatient(id: String, patient: Patient, onSuccess: () -> Unit) {
        launchWithErrorHandler {
            try {
                _uiState.value = PatientsUiState.Loading

                patientRepository.updatePatient(id, patient)
                    .onSuccess {
                        Napier.i("✅ Patient updated successfully: ${patient.name}")
                        loadPatients(currentPage)
                        onSuccess()
                    }
                    .onFailure { error ->
                        val errorMessage = buildErrorMessage(
                            operation = "Update Patient",
                            error = error,
                            context = "Backend rejected update for patient ID: $id"
                        )
                        Napier.e("❌ BACKEND ERROR: Update failed - ${error.message}", error)
                        _uiState.value = PatientsUiState.Error(errorMessage)
                    }
            } catch (exception: Exception) {
                val errorMessage = buildErrorMessage(
                    operation = "Update Patient",
                    error = exception,
                    context = "PUT /patients/$id - ${patient.name}"
                )
                Napier.e("❌ UPDATE FAILED: ${exception.message}", exception)
                _uiState.value = PatientsUiState.Error(errorMessage)
            }
        }
    }

    fun deletePatient(id: String, onSuccess: () -> Unit) {
        launchWithErrorHandler {
            try {
                _uiState.value = PatientsUiState.Loading

                patientRepository.deletePatient(id)
                    .onSuccess {
                        Napier.i("✅ Patient deleted successfully: $id")
                        loadPatients(currentPage)
                        onSuccess()
                    }
                    .onFailure { error ->
                        val errorMessage = buildErrorMessage(
                            operation = "Delete Patient",
                            error = error,
                            context = "Backend rejected deletion for patient ID: $id"
                        )
                        Napier.e("❌ BACKEND ERROR: Delete failed - ${error.message}", error)
                        _uiState.value = PatientsUiState.Error(errorMessage)
                    }
            } catch (exception: Exception) {
                val errorMessage = buildErrorMessage(
                    operation = "Delete Patient",
                    error = exception,
                    context = "DELETE /patients/$id"
                )
                Napier.e("❌ DELETE FAILED: ${exception.message}", exception)
                _uiState.value = PatientsUiState.Error(errorMessage)
            }
        }
    }

    fun refresh() {
        loadPatients(currentPage)
    }

    fun loadNextPage() {
        if (hasNextPage()) {
            loadPatients(currentPage + 1)
        }
    }

    fun loadPreviousPage() {
        if (currentPage > 1) {
            loadPatients(currentPage - 1)
        }
    }

    private fun hasNextPage(): Boolean {
        return _patients.value.size * currentPage < totalPatients
    }

    /**
     * Build detailed error message for user
     * Shows REAL error cause - NO hiding behind generic messages
     */
    private fun buildErrorMessage(
        operation: String,
        error: Throwable,
        context: String
    ): String {
        val errorType = when {
            error.message?.contains("timeout", ignoreCase = true) == true ->
                "⏱️ TIMEOUT"
            error.message?.contains("connect", ignoreCase = true) == true ->
                "🔌 CONNECTION FAILED"
            error.message?.contains("404", ignoreCase = true) == true ->
                "🔍 NOT FOUND (404)"
            error.message?.contains("500", ignoreCase = true) == true ->
                "💥 SERVER ERROR (500)"
            error.message?.contains("401", ignoreCase = true) == true ||
                    error.message?.contains("403", ignoreCase = true) == true ->
                "🔒 AUTH ERROR"
            error.message?.contains("CORS", ignoreCase = true) == true ->
                "🌐 CORS ERROR"
            else -> "❌ ERROR"
        }

        return buildString {
            appendLine("$errorType: $operation Failed")
            appendLine()
            appendLine("Context: $context")
            appendLine()
            appendLine("Error Details:")
            appendLine(error.message ?: "Unknown error")
            appendLine()
            if (error.message?.contains("timeout", ignoreCase = true) == true) {
                appendLine("💡 Tip: Render.com cold start may take up to 60 seconds.")
                appendLine("Wait and try again.")
            } else if (error.message?.contains("connect", ignoreCase = true) == true) {
                appendLine("💡 Tip: Check if backend is running:")
                appendLine("   - Render Dashboard: https://dashboard.render.com")
                appendLine("   - Backend URL should respond")
            } else {
                appendLine("💡 Check backend logs for details.")
            }
        }
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
