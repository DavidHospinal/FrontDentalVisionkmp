package com.dentalvision.ai.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dentalvision.ai.data.remote.api.dto.*
import com.dentalvision.ai.presentation.theme.DentalColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailDialog(
    analysisReport: AnalysisReport,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DENTAL VISION AI",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DentalColors.Primary
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = "Professional Dental Analysis Report",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Patient Information Section
                    analysisReport.patient?.let { patient ->
                        PatientInformationSection(patient, analysisReport.analysisTimestamp)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Analysis Results Section
                    AnalysisResultsSection(analysisReport)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Detections List
                    DetectionsSection(analysisReport.detections)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Clinical Recommendations
                    ClinicalRecommendationsSection(analysisReport.getHealthStatus())

                    Spacer(modifier = Modifier.height(24.dp))

                    // Disclaimer
                    DisclaimerSection()
                }
            }
        }
    }
}

@Composable
private fun PatientInformationSection(patient: PatientInfo, analysisDate: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PATIENT INFORMATION",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DentalColors.Primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("Name:", patient.name)
            patient.age?.let { InfoRow("Age:", it.toString()) }
            patient.phone?.let { InfoRow("Phone:", it) }

            // Format analysis date
            try {
                val instant = Instant.parse(analysisDate)
                val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                val formattedDate = "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}/${localDateTime.year}"
                InfoRow("Analysis Date:", formattedDate)
            } catch (e: Exception) {
                InfoRow("Analysis Date:", analysisDate)
            }
        }
    }
}

@Composable
private fun AnalysisResultsSection(report: AnalysisReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ANALYSIS RESULTS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Teeth",
                    value = report.summary.totalTeethDetected.toString(),
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF2196F3)
                )

                StatCard(
                    title = "Cavities",
                    value = report.summary.cavityCount.toString(),
                    modifier = Modifier.weight(1f),
                    color = if (report.summary.cavityCount > 0) Color(0xFFF44336) else Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Healthy Teeth",
                    value = report.summary.healthyCount.toString(),
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF4CAF50)
                )

                StatCard(
                    title = "AI Confidence",
                    value = "${(report.getAverageConfidence() * 100).toInt()}%",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health Status Badge
            val healthStatus = report.getHealthStatus()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (healthStatus == HealthStatus.REQUIRES_ATTENTION)
                        Icons.Default.Warning
                    else
                        Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(parseColor(healthStatus.colorCode)),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Status: ${healthStatus.displayName.uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(parseColor(healthStatus.colorCode))
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetectionsSection(detections: List<Detection>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FINDINGS DETAIL",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (detections.isEmpty()) {
                Text(
                    text = "No detections found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                detections.sortedByDescending { it.confidence }.forEach { detection ->
                    DetectionItem(detection)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DetectionItem(detection: Detection) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (detection.isCavity()) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Object #${detection.objectId}: ${detection.className.uppercase()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (detection.isCavity()) Color(0xFFC62828) else Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Confidence: ${detection.getConfidencePercentage()} - ${detection.getSeverity().displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (detection.isCavity()) {
            Badge(
                containerColor = Color(0xFFF44336),
                contentColor = Color.White
            ) {
                Text("ACTION REQUIRED", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ClinicalRecommendationsSection(healthStatus: HealthStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF9C4)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CLINICAL RECOMMENDATIONS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            RecommendationItem(1, "Analysis completed successfully")
            RecommendationItem(2, "Review detections marked in the image")
            RecommendationItem(3, "Consult with a dental professional")

            if (healthStatus == HealthStatus.REQUIRES_ATTENTION) {
                RecommendationItem(4, "Urgent treatment recommended for detected cavities")
            }
        }
    }
}

@Composable
private fun RecommendationItem(number: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(24.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DisclaimerSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE0B2)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "IMPORTANT MEDICAL DISCLAIMER",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "System YOLOv12 for Automated Dental Analysis. This is a support tool, not a definitive diagnosis. Always consult with a qualified dental professional for accurate diagnosis and treatment planning.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Helper function to parse color from hex string
private fun parseColor(hex: String): Long {
    return try {
        val cleanHex = hex.removePrefix("#")
        ("FF" + cleanHex).toLong(16)
    } catch (e: Exception) {
        0xFF000000
    }
}
