package com.dentalvision.ai.domain.repository

import com.dentalvision.ai.domain.model.Analysis

/**
 * Repository interface for Analysis operations
 * Defines contract for dental image analysis data access
 */
interface AnalysisRepository {

    /**
     * Submit dental image for AI analysis
     * @param patientId Patient ID
     * @param imageData Image file bytes
     * @param imageName Image filename
     * @return Result containing analysis or error
     */
    suspend fun submitAnalysis(
        patientId: String,
        imageData: ByteArray,
        imageName: String
    ): Result<Analysis>

    /**
     * Get analysis by ID
     * @param id Analysis ID
     * @return Result containing analysis or error
     */
    suspend fun getAnalysisById(id: String): Result<Analysis>

    /**
     * Get all analyses for a patient
     * @param patientId Patient ID
     * @return Result containing list of analyses and total count
     */
    suspend fun getPatientAnalyses(patientId: String): Result<Pair<List<Analysis>, Int>>
}
