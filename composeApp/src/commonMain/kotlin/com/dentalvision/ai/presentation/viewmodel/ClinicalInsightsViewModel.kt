package com.dentalvision.ai.presentation.viewmodel

import com.dentalvision.ai.data.remote.gemini.ClinicalInsight
import com.dentalvision.ai.data.remote.gemini.GeminiApiClient
import com.dentalvision.ai.data.remote.gemini.dto.ClinicalInsightRequest
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClinicalInsightsViewModel(
    private val geminiClient: GeminiApiClient
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<ClinicalInsightsUiState>(ClinicalInsightsUiState.Idle)
    val uiState: StateFlow<ClinicalInsightsUiState> = _uiState.asStateFlow()

    fun generateInsight(
        doctorName: String,
        patientName: String,
        cavityCount: Int,
        healthyCount: Int,
        confidence: Float
    ) {
        launchWithErrorHandler {
            _uiState.value = ClinicalInsightsUiState.Loading

            Napier.d("CLINICAL INSIGHTS: Generating for doctor=$doctorName, patient=$patientName, cavities=$cavityCount, healthy=$healthyCount")

            val request = ClinicalInsightRequest(
                doctorName = doctorName,
                patientName = patientName,
                cavityCount = cavityCount,
                healthyCount = healthyCount,
                confidence = confidence
            )

            geminiClient.getClinicalInsight(request)
                .onSuccess { insight ->
                    Napier.i("CLINICAL INSIGHTS: Successfully generated - Risk: ${insight.riskLevel}")
                    _uiState.value = ClinicalInsightsUiState.Success(insight)
                }
                .onFailure { error ->
                    Napier.e("CLINICAL INSIGHTS: Failed to generate insight", error)
                    _uiState.value = ClinicalInsightsUiState.Error(
                        error.message ?: "Failed to generate clinical insights"
                    )
                }
        }
    }

    fun reset() {
        _uiState.value = ClinicalInsightsUiState.Idle
    }
}

sealed class ClinicalInsightsUiState {
    data object Idle : ClinicalInsightsUiState()
    data object Loading : ClinicalInsightsUiState()
    data class Success(val insight: ClinicalInsight) : ClinicalInsightsUiState()
    data class Error(val message: String) : ClinicalInsightsUiState()
}
