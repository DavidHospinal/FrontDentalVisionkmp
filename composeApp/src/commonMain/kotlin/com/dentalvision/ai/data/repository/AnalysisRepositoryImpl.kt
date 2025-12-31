package com.dentalvision.ai.data.repository

import com.dentalvision.ai.data.remote.api.ApiClient
import com.dentalvision.ai.data.remote.api.ApiConfig
import com.dentalvision.ai.data.remote.api.dto.DentalAnalysisDTO
import com.dentalvision.ai.data.remote.gradio.GradioApiClient
import com.dentalvision.ai.data.remote.gradio.GradioDetection
import com.dentalvision.ai.domain.model.Analysis
import com.dentalvision.ai.domain.model.ToothDetection
import com.dentalvision.ai.domain.repository.AnalysisRepository
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

/**
 * Wrapper for parsing technical data JSON from Gradio API
 */
@Serializable
private data class TechnicalDataWrapper(
    val detections: List<GradioDetection> = emptyList(),
    val summary: SummaryWrapper? = null
)

@Serializable
private data class SummaryWrapper(
    val total_teeth_detected: Int = 0,
    val healthy_count: Int = 0,
    val cavity_count: Int = 0,
    val average_confidence: Double = 0.0
)

/**
 * DTOs for backend analysis registration
 */
@Serializable
private data class AnalysisRegistrationRequest(
    val patient_id: String,
    val image_filename: String,
    val confidence_threshold: Double,
    val detections: List<DetectionData>,
    val summary: SummaryData
)

@Serializable
private data class DetectionData(
    val fdi_number: String,
    val has_caries: Boolean,
    val confidence: Double,
    val bbox: List<Double>
)

@Serializable
private data class SummaryData(
    val total_teeth_detected: Int,
    val cavity_count: Int,
    val healthy_count: Int,
    val health_percentage: Double,
    val average_confidence: Double
)

/**
 * Implementation of AnalysisRepository
 * Handles dental image analysis using:
 * - GradioApiClient for AI processing (YOLOv12)
 * - Backend API for storing and retrieving analysis results
 */
class AnalysisRepositoryImpl(
    private val backendClient: ApiClient,
    private val gradioClient: GradioApiClient
) : AnalysisRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Submit dental image for AI analysis (PREVIEW ONLY)
     * Flow:
     * 1. Send image to Gradio API (HuggingFace Space)
     * 2. Parse AI results
     * 3. Return Analysis domain model for preview
     *
     * Note: Does NOT save to backend database.
     * Use saveAnalysisToBackend() explicitly to persist.
     */
    override suspend fun submitAnalysis(
        patientId: String,
        imageData: ByteArray,
        imageName: String
    ): Result<Analysis> {
        return try {
            Napier.d("Starting analysis submission for patient: $patientId")

            // Step 1: Submit to Gradio API for AI processing
            val gradioResponse = gradioClient.analyzeDentalImage(
                imageData = imageData,
                imageName = imageName,
                confidenceThreshold = 0.25
            )

            // Check for errors
            if (gradioResponse.error != null) {
                Napier.e("Gradio API error: ${gradioResponse.error}")
                return Result.failure(Exception(gradioResponse.error))
            }

            // Step 2: Parse results
            val analysisData = gradioResponse.data?.firstOrNull()
                ?: return Result.failure(Exception("No analysis data returned from AI"))

            // Parse detections JSON if present
            // El JSON viene como objeto: {"detections": [...], "summary": {...}}
            val detections = try {
                analysisData.detections?.let { detectionsJson ->
                    Napier.d("Parsing detections JSON: ${detectionsJson.take(200)}")

                    // Parsear el objeto completo
                    val technicalData = json.decodeFromString<TechnicalDataWrapper>(detectionsJson)
                    Napier.d("Successfully parsed ${technicalData.detections.size} detections")
                    technicalData.detections
                } ?: emptyList()
            } catch (e: Exception) {
                Napier.e("Failed to parse detections JSON: ${e.message}", e)
                emptyList()
            }

            // Convert to domain model detections
            val analysisId = generateAnalysisId()
            val toothDetections = detections.mapIndexed { index, detection ->
                val fdiNumber = detection.fdiNumber?.toIntOrNull() ?: 0
                val bbox = if (detection.bbox != null && detection.bbox.size >= 4) {
                    ToothDetection.BoundingBox(
                        x = detection.bbox[0],
                        y = detection.bbox[1],
                        width = detection.bbox[2],
                        height = detection.bbox[3]
                    )
                } else {
                    ToothDetection.BoundingBox(0.0, 0.0, 0.0, 0.0)
                }

                // Detectar caries basado en className (backend envía "cavity" no "caries")
                val hasCavity = detection.hasCaries ||
                    detection.className?.contains("cavity", ignoreCase = true) == true ||
                    detection.className?.contains("caries", ignoreCase = true) == true

                if (index < 3) {
                    Napier.d("Detection $index: className='${detection.className}', hasCaries=${detection.hasCaries}, calculated=$hasCavity")
                }

                ToothDetection(
                    id = "$analysisId-DET-$index",
                    analysisId = analysisId,
                    toothNumberFDI = fdiNumber,
                    hasCaries = hasCavity,
                    confidence = detection.confidence,
                    boundingBox = bbox
                )
            }

            // Step 3: Create Analysis domain model
            Napier.d("Processed image URL from backend: ${analysisData.image}")
            Napier.d("Total detections: ${detections.size}")
            Napier.d("Average confidence from detections: ${detections.map { it.confidence }.average().takeIf { !it.isNaN() } ?: 0.0}")

            val analysis = Analysis(
                id = analysisId,
                patientId = patientId,
                imageUrl = analysisData.image ?: imageName,
                analysisDate = Clock.System.now(),
                totalTeethDetected = analysisData.summary?.totalDetections ?: detections.size,
                totalCariesDetected = analysisData.summary?.cariesDetected
                    ?: detections.count { it.hasCaries },
                confidenceScore = analysisData.summary?.averageConfidence
                    ?: detections.map { it.confidence }.average().takeIf { !it.isNaN() } ?: 0.0,
                status = Analysis.AnalysisStatus.COMPLETED,
                detections = toothDetections,
                notes = analysisData.summary?.recommendations?.joinToString("\n"),
                performedBy = "AI System",
                synced = false
            )

            Napier.i("Analysis created successfully with ${analysis.detections.size} detections and confidence ${analysis.confidenceScore}")
            Napier.i("Analysis preview ready: ${analysis.id} (NOT saved to backend yet)")
            Result.success(analysis)

        } catch (e: Exception) {
            Napier.e("Analysis submission failed", e)
            Result.failure(e)
        }
    }

    /**
     * Get analysis by ID from backend
     */
    override suspend fun getAnalysisById(id: String): Result<Analysis> {
        return try {
            val dto: DentalAnalysisDTO = backendClient.get("${ApiConfig.Endpoints.ANALYSIS}/$id")
            Result.success(dto.toDomainModel())
        } catch (e: Exception) {
            Napier.e("Failed to get analysis by ID: $id", e)
            Result.failure(e)
        }
    }

    /**
     * Get all analyses for a patient from backend
     */
    override suspend fun getPatientAnalyses(patientId: String): Result<Pair<List<Analysis>, Int>> {
        return try {
            val dtoList: List<DentalAnalysisDTO> = backendClient.get(
                "${ApiConfig.Endpoints.ANALYSIS}/patient/$patientId"
            )
            val analyses = dtoList.map { it.toDomainModel() }
            Result.success(Pair(analyses, analyses.size))
        } catch (e: Exception) {
            Napier.e("Failed to get patient analyses: $patientId", e)
            Result.failure(e)
        }
    }

    // --- Private Helper Methods ---

    /**
     * Save analysis to backend database
     * Calls /analysis/register to save with sequential ID
     */
    override suspend fun saveAnalysis(analysis: Analysis): Result<Unit> {
        try {
            Napier.d("Registering analysis to backend for patient: ${analysis.patientId}")
            Napier.d("Local preview ID: ${analysis.id} (will NOT be sent, backend generates its own ID)")

            // Prepare serializable request data
            val requestData = AnalysisRegistrationRequest(
                patient_id = analysis.patientId,
                image_filename = analysis.imageUrl.substringAfterLast("/").ifBlank { "analysis.jpg" },
                confidence_threshold = analysis.confidenceScore,
                detections = analysis.detections.map { detection ->
                    DetectionData(
                        fdi_number = detection.toothNumberFDI.toString(),
                        has_caries = detection.hasCaries,
                        confidence = detection.confidence,
                        bbox = listOf(
                            detection.boundingBox.x,
                            detection.boundingBox.y,
                            detection.boundingBox.width,
                            detection.boundingBox.height
                        )
                    )
                },
                summary = SummaryData(
                    total_teeth_detected = analysis.totalTeethDetected,
                    cavity_count = analysis.totalCariesDetected,
                    healthy_count = analysis.totalTeethDetected - analysis.totalCariesDetected,
                    health_percentage = if (analysis.totalTeethDetected > 0) {
                        (analysis.totalTeethDetected - analysis.totalCariesDetected).toDouble() / analysis.totalTeethDetected * 100
                    } else 0.0,
                    average_confidence = analysis.confidenceScore
                )
            )

            Napier.d("Request data prepared - ${requestData.detections.size} detections, patient=${requestData.patient_id}")

            // Make POST request to /analysis/register
            val response: Map<String, Any> = backendClient.post(
                "${ApiConfig.Endpoints.ANALYSIS}/register",
                requestData
            )

            Napier.d("Backend response received: success=${response["success"]}")

            if (response["success"] == true) {
                val data = response["data"] as? Map<*, *>
                val backendAnalysisId = data?.get("analysis_id") as? String
                Napier.i("Analysis registered successfully with backend ID: $backendAnalysisId")
                return Result.success(Unit)
            } else {
                val message = response["message"] ?: "Unknown error"
                Napier.w("Failed to register analysis to backend: $message")
                return Result.failure(Exception(message.toString()))
            }

        } catch (e: Exception) {
            Napier.e("Error registering analysis to backend for patient ${analysis.patientId}", e)
            return Result.failure(e)
        }
    }

    /**
     * Convert DTO to domain model
     */
    private fun DentalAnalysisDTO.toDomainModel(): Analysis {
        val detections = this.detections.mapIndexed { index, detection ->
            val fdiNumber = detection.fdi_number?.toIntOrNull() ?: 0
            val bbox = if (detection.bbox.size >= 4) {
                ToothDetection.BoundingBox(
                    x = detection.bbox[0],
                    y = detection.bbox[1],
                    width = detection.bbox[2],
                    height = detection.bbox[3]
                )
            } else {
                ToothDetection.BoundingBox(0.0, 0.0, 0.0, 0.0)
            }

            ToothDetection(
                id = "${this.id}-DET-$index",
                analysisId = this.id,
                toothNumberFDI = fdiNumber,
                hasCaries = detection.`class`.contains("caries", ignoreCase = true),
                confidence = detection.confidence,
                boundingBox = bbox
            )
        }

        return Analysis(
            id = this.id,
            patientId = this.patient_id ?: "Unknown",
            imageUrl = this.image_path,
            analysisDate = parseInstant(this.created_at),
            totalTeethDetected = this.summary.total_detections,
            totalCariesDetected = this.summary.caries_detected,
            confidenceScore = this.confidence_threshold,
            status = Analysis.AnalysisStatus.COMPLETED,
            detections = detections,
            notes = this.summary.recommendations.joinToString("\n"),
            performedBy = "AI System",
            synced = true
        )
    }

    /**
     * Parse ISO 8601 timestamp to Instant
     */
    private fun parseInstant(timestamp: String): Instant {
        return try {
            Instant.parse(timestamp)
        } catch (e: Exception) {
            Clock.System.now()
        }
    }

    /**
     * Extract quadrant from FDI number
     * FDI system: 11-18 (Q1), 21-28 (Q2), 31-38 (Q3), 41-48 (Q4)
     */
    private fun extractQuadrant(fdiNumber: String): Int {
        return try {
            val number = fdiNumber.toIntOrNull() ?: return 0
            when {
                number in 11..18 -> 1
                number in 21..28 -> 2
                number in 31..38 -> 3
                number in 41..48 -> 4
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Generate unique analysis ID
     */
    private fun generateAnalysisId(): String {
        return "AN-${Clock.System.now().toEpochMilliseconds()}"
    }
}
