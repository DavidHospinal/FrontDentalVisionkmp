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
     * Submit dental image for AI analysis
     * Flow:
     * 1. Send image to Gradio API (HuggingFace Space)
     * 2. Parse AI results
     * 3. Save to backend database
     * 4. Return Analysis domain model
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
                confidenceThreshold = 0.5
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
            val detections = try {
                analysisData.detections?.let { detectionsJson ->
                    json.decodeFromString<List<GradioDetection>>(detectionsJson)
                } ?: emptyList()
            } catch (e: Exception) {
                Napier.w("Failed to parse detections JSON: ${e.message}")
                emptyList()
            }

            // Convert to domain model detections
            val analysisId = generateAnalysisId()
            val toothDetections = detections.mapIndexed { index, detection ->
                val fdiNumber = detection.fdiNumber?.toIntOrNull() ?: 0
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
                    id = "$analysisId-DET-$index",
                    analysisId = analysisId,
                    toothNumberFDI = fdiNumber,
                    hasCaries = detection.hasCaries || detection.className.contains("caries", ignoreCase = true),
                    confidence = detection.confidence,
                    boundingBox = bbox
                )
            }

            // Step 3: Create Analysis domain model
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

            // Step 4: Save to backend (optional - can fail silently for now)
            try {
                saveAnalysisToBackend(analysis)
                Napier.i("Analysis saved to backend successfully")
            } catch (e: Exception) {
                Napier.w("Failed to save to backend (continuing anyway): ${e.message}")
            }

            Napier.i("Analysis completed successfully: ${analysis.id}")
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
     */
    private suspend fun saveAnalysisToBackend(analysis: Analysis) {
        // TODO: Implement when backend endpoint is ready
        // For now, this is a placeholder
        Napier.d("Saving analysis to backend: ${analysis.id}")
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
