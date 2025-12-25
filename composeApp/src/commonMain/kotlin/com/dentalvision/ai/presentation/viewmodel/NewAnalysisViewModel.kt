package com.dentalvision.ai.presentation.viewmodel

import com.dentalvision.ai.domain.model.Analysis
import com.dentalvision.ai.domain.repository.AnalysisRepository
import com.dentalvision.ai.platform.FilePicker
import com.dentalvision.ai.platform.FilePickerResult
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for New Analysis screen
 * Handles dental image analysis workflow with AI integration
 */
class NewAnalysisViewModel(
    private val analysisRepository: AnalysisRepository,
    private val filePicker: FilePicker
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private val _selectedImage = MutableStateFlow<ImageData?>(null)
    val selectedImage: StateFlow<ImageData?> = _selectedImage.asStateFlow()

    private val _analysisResult = MutableStateFlow<Analysis?>(null)
    val analysisResult: StateFlow<Analysis?> = _analysisResult.asStateFlow()

    /**
     * Open file picker to select dental image
     */
    fun selectImage() {
        launchWithErrorHandler {
            _uiState.value = AnalysisUiState.SelectingImage
            Napier.d("Opening file picker")

            when (val result = filePicker.pickImage()) {
                is FilePickerResult.Success -> {
                    _selectedImage.value = ImageData(
                        bytes = result.data,
                        name = result.name,
                        mimeType = result.mimeType
                    )
                    _uiState.value = AnalysisUiState.ImageSelected
                    Napier.i("Image selected: ${result.name} (${result.data.size} bytes)")
                }

                is FilePickerResult.Cancelled -> {
                    _uiState.value = AnalysisUiState.Idle
                    Napier.d("Image selection cancelled")
                }

                is FilePickerResult.Error -> {
                    _uiState.value = AnalysisUiState.Error(result.message)
                    Napier.e("Image selection error: ${result.message}")
                }
            }
        }
    }

    /**
     * Analyze selected image using AI
     */
    fun analyzeImage(patientId: String) {
        val imageData = _selectedImage.value

        if (imageData == null) {
            _uiState.value = AnalysisUiState.Error("No image selected")
            return
        }

        launchWithErrorHandler {
            _uiState.value = AnalysisUiState.Analyzing(progress = 0)
            Napier.d("Starting analysis for patient: $patientId")

            // Simulate progress updates
            _uiState.value = AnalysisUiState.Analyzing(progress = 25)

            analysisRepository.submitAnalysis(
                patientId = patientId,
                imageData = imageData.bytes,
                imageName = imageData.name
            )
                .onSuccess { analysis ->
                    _analysisResult.value = analysis
                    _uiState.value = AnalysisUiState.Success(analysis)
                    Napier.i("Analysis completed successfully: ${analysis.id}")
                }
                .onFailure { error ->
                    Napier.e("Analysis failed", error)
                    _uiState.value = AnalysisUiState.Error(
                        error.message ?: "Analysis failed"
                    )
                }
        }
    }

    /**
     * Clear selected image and reset state
     */
    fun clearImage() {
        _selectedImage.value = null
        _analysisResult.value = null
        _uiState.value = AnalysisUiState.Idle
        Napier.d("Image cleared, state reset")
    }

    /**
     * Reset to initial state
     */
    fun reset() {
        _uiState.value = AnalysisUiState.Idle
        _selectedImage.value = null
        _analysisResult.value = null
    }

    /**
     * Retry analysis after error
     */
    fun retryAnalysis(patientId: String) {
        analyzeImage(patientId)
    }
}

/**
 * UI state for New Analysis screen
 */
sealed class AnalysisUiState {
    object Idle : AnalysisUiState()
    object SelectingImage : AnalysisUiState()
    object ImageSelected : AnalysisUiState()
    data class Analyzing(val progress: Int) : AnalysisUiState()
    data class Success(val analysis: Analysis) : AnalysisUiState()
    data class Error(val message: String) : AnalysisUiState()
}

/**
 * Represents selected image data
 */
data class ImageData(
    val bytes: ByteArray,
    val name: String,
    val mimeType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ImageData

        if (!bytes.contentEquals(other.bytes)) return false
        if (name != other.name) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}
