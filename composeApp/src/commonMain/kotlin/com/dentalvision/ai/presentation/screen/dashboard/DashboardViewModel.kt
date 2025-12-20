package com.dentalvision.ai.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dentalvision.ai.data.remote.service.SystemService
import com.dentalvision.ai.domain.model.SystemStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Dashboard ViewModel
 * Manages dashboard state and backend communication
 */
class DashboardViewModel(
    private val systemService: SystemService = SystemService()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadSystemStatistics()
    }

    fun loadSystemStatistics() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            try {
                val response = systemService.getSystemStatistics()

                if (response.success && response.data != null) {
                    val statistics = SystemStatistics(
                        patients = com.dentalvision.ai.domain.model.PatientStats(
                            total = response.data.patients.total,
                            new_this_month = response.data.patients.new_this_month
                        ),
                        analyses = com.dentalvision.ai.domain.model.AnalysisStats(
                            total = response.data.analyses.total,
                            this_month = response.data.analyses.this_month
                        ),
                        appointments = com.dentalvision.ai.domain.model.AppointmentStats(
                            scheduled = response.data.appointments.scheduled,
                            completed = response.data.appointments.completed
                        ),
                        reports = com.dentalvision.ai.domain.model.ReportStats(
                            generated = response.data.reports.generated,
                            this_month = response.data.reports.this_month
                        )
                    )
                    _uiState.value = DashboardUiState.Success(statistics)
                } else {
                    _uiState.value = DashboardUiState.Error(
                        response.message ?: "Failed to load system statistics"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(
                    e.message ?: "Network error occurred"
                )
            }
        }
    }

    fun retry() {
        loadSystemStatistics()
    }
}

/**
 * Dashboard UI State
 */
sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(val statistics: SystemStatistics) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
