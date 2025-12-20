package com.dentalvision.ai.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a detected tooth with FDI numbering
 * FDI (Fédération Dentaire Internationale) World Dental Federation notation
 */
@Serializable
data class ToothDetection(
    val id: String,
    val analysisId: String,
    val toothNumberFDI: Int,        // FDI two-digit notation (11-18, 21-28, 31-38, 41-48)
    val hasCaries: Boolean,
    val confidence: Double,         // 0.0 to 1.0
    val boundingBox: BoundingBox
) {
    /**
     * Bounding box coordinates for tooth detection
     */
    @Serializable
    data class BoundingBox(
        val x: Double,
        val y: Double,
        val width: Double,
        val height: Double
    ) {
        /**
         * Center point of the bounding box
         */
        val centerX: Double
            get() = x + (width / 2)

        val centerY: Double
            get() = y + (height / 2)

        /**
         * Area of the bounding box
         */
        val area: Double
            get() = width * height
    }

    /**
     * FDI quadrant (1 = upper right, 2 = upper left, 3 = lower left, 4 = lower right)
     */
    val quadrant: Int
        get() = toothNumberFDI / 10

    /**
     * Tooth position within quadrant (1-8)
     */
    val positionInQuadrant: Int
        get() = toothNumberFDI % 10

    /**
     * Human-readable tooth name
     */
    val toothName: String
        get() = when (positionInQuadrant) {
            1, 2 -> "Incisor"
            3 -> "Canine"
            4, 5 -> "Premolar"
            6, 7, 8 -> "Molar"
            else -> "Unknown"
        }

    /**
     * Full description of the tooth
     * Example: "Upper Right First Molar (#16)"
     */
    val description: String
        get() {
            val position = when (quadrant) {
                1 -> "Upper Right"
                2 -> "Upper Left"
                3 -> "Lower Left"
                4 -> "Lower Right"
                else -> "Unknown"
            }

            val toothType = when (positionInQuadrant) {
                1 -> "Central Incisor"
                2 -> "Lateral Incisor"
                3 -> "Canine"
                4 -> "First Premolar"
                5 -> "Second Premolar"
                6 -> "First Molar"
                7 -> "Second Molar"
                8 -> "Third Molar (Wisdom)"
                else -> "Unknown"
            }

            return "$position $toothType (#$toothNumberFDI)"
        }

    /**
     * Status of the tooth (Healthy or Caries Detected)
     */
    val status: String
        get() = if (hasCaries) "Caries Detected" else "Healthy"

    /**
     * Confidence percentage (0-100%)
     */
    val confidencePercentage: String
        get() = "${(confidence * 100).toInt()}%"

    /**
     * Validates if detection is reliable (confidence > 70%)
     */
    val isReliable: Boolean
        get() = confidence >= 0.70
}
