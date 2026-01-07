package com.dentalvision.ai.presentation.viewmodel

import com.dentalvision.ai.domain.model.Analysis
import com.dentalvision.ai.domain.model.Appointment
import com.dentalvision.ai.domain.model.AppointmentStatus
import com.dentalvision.ai.domain.model.AppointmentType
import com.dentalvision.ai.domain.model.Patient
import com.dentalvision.ai.domain.repository.AnalysisRepository
import com.dentalvision.ai.domain.repository.AppointmentRepository
import com.dentalvision.ai.domain.repository.PatientRepository
import com.dentalvision.ai.platform.FilePicker
import com.dentalvision.ai.platform.FilePickerResult
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class NewAnalysisViewModel(
    private val analysisRepository: AnalysisRepository,
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val filePicker: FilePicker
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private val _selectedImage = MutableStateFlow<ImageData?>(null)
    val selectedImage: StateFlow<ImageData?> = _selectedImage.asStateFlow()

    private val _analysisResult = MutableStateFlow<Analysis?>(null)
    val analysisResult: StateFlow<Analysis?> = _analysisResult.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val _eligiblePatients = MutableStateFlow<List<Patient>>(emptyList())
    val eligiblePatients: StateFlow<List<Patient>> = _eligiblePatients.asStateFlow()

    init {
        loadEligiblePatients()
    }

    fun loadEligiblePatients() {
        launchWithErrorHandler {
            Napier.d("Loading eligible patients for AI Analysis")

            appointmentRepository.getAppointments()
                .onSuccess { (appointments, _) ->
                    val eligiblePatientIds = appointments
                        .filter {
                            it.status == AppointmentStatus.CONFIRMED &&
                            it.appointmentType == AppointmentType.AI_ANALYSIS
                        }
                        .map { it.patientId }
                        .toSet()

                    Napier.d("Found ${eligiblePatientIds.size} patients with confirmed AI Analysis appointments")

                    if (eligiblePatientIds.isNotEmpty()) {
                        patientRepository.getPatients(page = 1, limit = 100)
                            .onSuccess { (patients, _) ->
                                val filtered = patients.filter { it.id in eligiblePatientIds }
                                _eligiblePatients.value = filtered
                                Napier.i("Loaded ${filtered.size} eligible patients for analysis")
                            }
                            .onFailure { error ->
                                Napier.e("Failed to load patients", error)
                                _eligiblePatients.value = emptyList()
                            }
                    } else {
                        _eligiblePatients.value = emptyList()
                        Napier.w("No eligible patients found. Patients need CONFIRMED appointment with AI_ANALYSIS type.")
                    }
                }
                .onFailure { error ->
                    Napier.e("Failed to load appointments", error)
                    _eligiblePatients.value = emptyList()
                }
        }
    }

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

    fun analyzeImage(patientId: String) {
        val imageData = _selectedImage.value

        if (imageData == null) {
            _uiState.value = AnalysisUiState.Error("No image selected")
            return
        }

        if (_eligiblePatients.value.none { it.id == patientId }) {
            _uiState.value = AnalysisUiState.Error(
                "Patient not eligible. Patient must have a CONFIRMED appointment with AI Analysis type."
            )
            Napier.w("Attempted analysis for non-eligible patient: $patientId")
            return
        }

        launchWithErrorHandler {
            _uiState.value = AnalysisUiState.Analyzing(progress = 0)
            Napier.d("Starting analysis for patient: $patientId")

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

    fun clearImage() {
        _selectedImage.value = null
        _analysisResult.value = null
        _uiState.value = AnalysisUiState.Idle
        Napier.d("Image cleared, state reset")
    }

    fun reset() {
        _uiState.value = AnalysisUiState.Idle
        _selectedImage.value = null
        _analysisResult.value = null
    }

    fun retryAnalysis(patientId: String) {
        analyzeImage(patientId)
    }

    fun refresh() {
        loadEligiblePatients()
    }

    /**
     * Manually save analysis to backend
     * Called when user clicks "Save Analysis" button
     * ALSO creates a historical "Completed" appointment record
     */
    fun saveAnalysisToBackend() {
        val analysis = _analysisResult.value
        if (analysis == null) {
            _saveState.value = SaveState.Error("No analysis to save")
            return
        }

        launchWithErrorHandler {
            _saveState.value = SaveState.Saving
            Napier.d("Saving existing analysis to backend: ${analysis.id} for patient ${analysis.patientId}")

            // Save the existing analysis object to backend
            // This will call /analysis/register endpoint with the analysis data
            analysisRepository.saveAnalysis(analysis)
                .onSuccess {
                    _saveState.value = SaveState.Success(analysis.id)
                    Napier.i("✅ Analysis saved to backend successfully for patient ${analysis.patientId}")

                    // Create historical appointment record for this AI Analysis
                    createHistoricalAppointment(analysis)
                }
                .onFailure { error ->
                    _saveState.value = SaveState.Error(error.message ?: "Failed to save")
                    Napier.e("❌ Failed to save analysis to backend", error)
                }
        }
    }

    /**
     * Create a "phantom" historical appointment record for AI Analysis
     * This links the analysis to the Appointments module history
     */
    private fun createHistoricalAppointment(analysis: Analysis) {
        launchWithErrorHandler {
            val now = Clock.System.now()
            val historicalAppointment = Appointment(
                id = "", // Backend will generate
                patientId = analysis.patientId,
                patientName = null, // Backend will fill from patient data
                appointmentDate = now,
                appointmentType = AppointmentType.AI_ANALYSIS,
                status = AppointmentStatus.COMPLETED,
                durationMinutes = 30,
                treatmentDescription = "Auto-generated from AI Analysis module. Analysis ID: ${analysis.id}",
                doctorName = "Dr. David",
                clinicLocation = "Dental Vision AI Clinic",
                createdAt = now,
                updatedAt = now
            )

            appointmentRepository.createAppointment(
                patientId = analysis.patientId,
                appointment = historicalAppointment
            )
                .onSuccess { createdAppointment ->
                    Napier.i("✅ Historical appointment created successfully: ${createdAppointment.id} for analysis ${analysis.id}")
                }
                .onFailure { error ->
                    // Don't fail the save operation, just log the error
                    Napier.w("⚠️ Failed to create historical appointment for analysis ${analysis.id}: ${error.message}")
                }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}

sealed class SaveState {
    data object Idle : SaveState()
    data object Saving : SaveState()
    data class Success(val analysisId: String) : SaveState()
    data class Error(val message: String) : SaveState()
}

sealed class AnalysisUiState {
    data object Idle : AnalysisUiState()
    data object SelectingImage : AnalysisUiState()
    data object ImageSelected : AnalysisUiState()
    data class Analyzing(val progress: Int) : AnalysisUiState()
    data class Success(val analysis: Analysis) : AnalysisUiState()
    data class Error(val message: String) : AnalysisUiState()
}

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
