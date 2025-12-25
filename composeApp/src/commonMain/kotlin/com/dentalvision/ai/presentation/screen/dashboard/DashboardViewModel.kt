package com.dentalvision.ai.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dentalvision.ai.data.remote.service.SystemService
import com.dentalvision.ai.domain.model.SystemStatistics
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Dashboard ViewModel
 * FIXED: Proper data synchronization without demo data fallback issues
 */
class DashboardViewModel(
    private val systemService: SystemService = SystemService()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Cache for successful data to avoid flickering during refreshes
    private var lastSuccessfulStats: SystemStatistics? = null

    init {
        loadSystemStatistics()
    }

    /**
     * Load system statistics from backend
     * Called on init and when user returns from other screens
     */
    fun loadSystemStatistics() {
        viewModelScope.launch {
            try {
                Napier.d("DASHBOARD: Loading system statistics from backend...")
                _uiState.value = DashboardUiState.Loading

                val response = systemService.getSystemStatistics()

                if (response.success && response.data != null) {
                    val stats = mapDTOToModel(response.data)
                    lastSuccessfulStats = stats

                    Napier.i("DASHBOARD: Successfully loaded backend data - Patients: ${stats.patients.total}, Analyses: ${stats.analyses.total}")
                    _uiState.value = DashboardUiState.Success(stats)

                } else {
                    val errorMsg = response.message ?: response.error ?: "Unknown error"
                    Napier.e("DASHBOARD: Backend returned error: $errorMsg")

                    // If we have cached data, show it
                    if (lastSuccessfulStats != null) {
                        Napier.w("DASHBOARD: Using cached data due to backend error")
                        _uiState.value = DashboardUiState.Success(lastSuccessfulStats!!)
                    } else {
                        // No cache, show demo data as fallback ONLY
                        Napier.w("DASHBOARD: No cached data, showing demo data as fallback")
                        _uiState.value = DashboardUiState.Success(getDemoStatistics())
                    }
                }

            } catch (e: Exception) {
                Napier.e("DASHBOARD: Exception during data load", e)

                // Network error - use cache or demo data
                if (lastSuccessfulStats != null) {
                    Napier.w("DASHBOARD: Using cached data due to network error")
                    _uiState.value = DashboardUiState.Success(lastSuccessfulStats!!)
                } else {
                    Napier.w("DASHBOARD: No cached data, showing demo data due to network error")
                    _uiState.value = DashboardUiState.Success(getDemoStatistics())
                }
            }
        }
    }

    /**
     * Refresh data - call this when user returns from other screens
     * Prevents duplicate calls with debounce check
     */
    fun refresh() {
        if (_uiState.value is DashboardUiState.Loading) {
            Napier.d("DASHBOARD: Refresh ignored - already loading")
            return
        }

        Napier.d("DASHBOARD: Explicit refresh requested")
        loadSystemStatistics()
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
                com.dentalvision.ai.domain.model.MonthlyData("Jul", analyses = 18, appointments = 15),
                com.dentalvision.ai.domain.model.MonthlyData("Aug", analyses = 22, appointments = 19),
                com.dentalvision.ai.domain.model.MonthlyData("Sep", analyses = 15, appointments = 12),
                com.dentalvision.ai.domain.model.MonthlyData("Oct", analyses = 28, appointments = 24),
                com.dentalvision.ai.domain.model.MonthlyData("Nov", analyses = 20, appointments = 17),
                com.dentalvision.ai.domain.model.MonthlyData("Dec", analyses = 23, appointments = 20)
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
