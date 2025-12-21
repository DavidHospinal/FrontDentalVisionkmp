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
                println("DASHBOARD: Fetching real data from backend...")
                val response = systemService.getSystemStatistics()

                if (response.success && response.data != null) {
                    val stats = mapDTOToModel(response.data)
                    println("DASHBOARD: Successfully loaded ${stats.monthlyTrend.size} months of data")
                    _uiState.value = DashboardUiState.Success(stats)
                } else {
                    println("DASHBOARD: Backend returned no data, using demo fallback")
                    _uiState.value = DashboardUiState.Success(getDemoStatistics())
                }
            } catch (e: Exception) {
                println("DASHBOARD: Error loading from backend: ${e.message}")
                println("DASHBOARD: Falling back to demo data")
                _uiState.value = DashboardUiState.Success(getDemoStatistics())
            }
        }
    }

    private fun mapDTOToModel(dto: com.dentalvision.ai.data.remote.api.dto.SystemStatisticsDTO): SystemStatistics {
        return SystemStatistics(
            patients = com.dentalvision.ai.domain.model.PatientStats(
                total = dto.patients.total,
                new_this_month = dto.patients.new_this_month
            ),
            analyses = com.dentalvision.ai.domain.model.AnalysisStats(
                total = dto.analyses.total,
                this_month = dto.analyses.this_month
            ),
            appointments = com.dentalvision.ai.domain.model.AppointmentStats(
                scheduled = dto.appointments.scheduled,
                completed = dto.appointments.completed
            ),
            reports = com.dentalvision.ai.domain.model.ReportStats(
                generated = dto.reports.generated,
                this_month = dto.reports.this_month
            ),
            monthlyTrend = dto.monthlyTrend.map { monthDTO ->
                com.dentalvision.ai.domain.model.MonthlyData(
                    month = monthDTO.month,
                    analyses = monthDTO.analyses,
                    appointments = monthDTO.appointments
                )
            }
        )
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
            ),
            monthlyTrend = listOf(
                com.dentalvision.ai.domain.model.MonthlyData("Jul", 18),
                com.dentalvision.ai.domain.model.MonthlyData("Aug", 22),
                com.dentalvision.ai.domain.model.MonthlyData("Sep", 15),
                com.dentalvision.ai.domain.model.MonthlyData("Oct", 28),
                com.dentalvision.ai.domain.model.MonthlyData("Nov", 20),
                com.dentalvision.ai.domain.model.MonthlyData("Dec", 23)
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
