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

            println("DASHBOARD: Using demo data (backend has empty database)")
            _uiState.value = DashboardUiState.Success(getDemoStatistics())
        }
    }

    private fun getDemoStatistics(): SystemStatistics {
        return SystemStatistics(
            patients = com.dentalvision.ai.domain.model.PatientStats(
                total = 156,
                new_this_month = 12
            ),
            analyses = com.dentalvision.ai.domain.model.AnalysisStats(
                total = 156,
                this_month = 23
            ),
            appointments = com.dentalvision.ai.domain.model.AppointmentStats(
                scheduled = 23,
                completed = 89
            ),
            reports = com.dentalvision.ai.domain.model.ReportStats(
                generated = 45,
                this_month = 12
            )
        )
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
