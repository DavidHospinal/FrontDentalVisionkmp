package com.dentalvision.ai.presentation.screen.insights

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dentalvision.ai.data.remote.gemini.ClinicalInsight
import com.dentalvision.ai.data.remote.gemini.RiskLevel
import com.dentalvision.ai.presentation.component.AmberGlowAnimation
import com.dentalvision.ai.presentation.component.ConfettiAnimation
import com.dentalvision.ai.presentation.component.PulseAnimation
import com.dentalvision.ai.presentation.component.SerpentineAnimation
import com.dentalvision.ai.presentation.theme.DentalColors
import com.dentalvision.ai.presentation.viewmodel.ClinicalInsightsUiState
import com.dentalvision.ai.presentation.viewmodel.ClinicalInsightsViewModel
import com.dentalvision.ai.presentation.navigation.LocalDoctorName
import org.koin.compose.koinInject

@Composable
fun ClinicalInsightsDialog(
    patientName: String,
    cavityCount: Int,
    healthyCount: Int,
    confidence: Float,
    onDismiss: () -> Unit,
    viewModel: ClinicalInsightsViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val doctorName = LocalDoctorName.current

    LaunchedEffect(Unit) {
        viewModel.generateInsight(
            doctorName = doctorName,
            patientName = patientName,
            cavityCount = cavityCount,
            healthyCount = healthyCount,
            confidence = confidence
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.reset()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is ClinicalInsightsUiState.Loading -> {
                    LoadingContent()
                }
                is ClinicalInsightsUiState.Success -> {
                    ClinicalInsightsContent(
                        insight = state.insight,
                        onDismiss = onDismiss
                    )
                }
                is ClinicalInsightsUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onDismiss = onDismiss
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = DentalColors.Primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Generating AI Clinical Insights...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Analyzing dental health data with Gemini AI",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.4f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFF44336)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Failed to Generate Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DentalColors.Primary
                )
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun ClinicalInsightsContent(
    insight: ClinicalInsight,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background layer with conditional pulsing for HIGH RISK
        if (insight.riskLevel == RiskLevel.HIGH) {
            HighRiskPulsingBackground()
        } else if (insight.riskLevel == RiskLevel.MODERATE) {
            AmberGlowAnimation()
        }

        // Main content card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.Center),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                ClinicalInsightsHeader(
                    greeting = insight.greeting,
                    riskLevel = insight.riskLevel,
                    onDismiss = onDismiss
                )

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Diagnosis Summary
                    DiagnosisSummarySection(insight.diagnosisSummary)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Prevention Tips
                    PreventionTipsSection(insight.preventionTips)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Corrective Actions
                    CorrectiveActionsSection(insight.correctiveActions)

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Footer disclaimer
                MedicalDisclaimerFooter()
            }
        }

        // Celebration overlay - MUST be on top for LOW RISK
        // This creates a fullscreen overlay that renders ABOVE the card
        if (insight.riskLevel == RiskLevel.LOW) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ConfettiAnimation()
                SerpentineAnimation()
            }
        }
    }
}

/**
 * Pulsing red background for HIGH RISK level
 * Creates a subtle, rhythmic red glow to indicate medical attention needed
 */
@Composable
private fun HighRiskPulsingBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "high_risk_pulse")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "background_alpha_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF44336).copy(alpha = alpha))
    )
}

@Composable
private fun ClinicalInsightsHeader(
    greeting: String,
    riskLevel: RiskLevel,
    onDismiss: () -> Unit
) {
    val headerColor = when (riskLevel) {
        RiskLevel.LOW -> Color(0xFF4CAF50)
        RiskLevel.MODERATE -> Color(0xFFFFC107)
        RiskLevel.HIGH -> Color(0xFFF44336)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerColor.copy(alpha = 0.1f))
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI CLINICAL INSIGHTS",
                    style = MaterialTheme.typography.headlineSmall,
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

            Spacer(modifier = Modifier.height(8.dp))

            Badge(
                containerColor = headerColor,
                contentColor = Color.White
            ) {
                Text(
                    text = riskLevel.displayName.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DiagnosisSummarySection(summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CLINICAL DIAGNOSIS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DentalColors.Primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PreventionTipsSection(tips: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PREVENTIVE RECOMMENDATIONS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(12.dp))
            tips.forEachIndexed { index, tip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(32.dp),
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CorrectiveActionsSection(actions: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF9C4)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CORRECTIVE ACTIONS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57C00)
            )
            Spacer(modifier = Modifier.height(12.dp))
            actions.forEachIndexed { index, action ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(32.dp),
                        color = Color(0xFFF57C00)
                    )
                    Text(
                        text = action,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicalDisclaimerFooter() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE0B2)
        ),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "Important Medical Disclaimer: This system is designed as a diagnostic support tool. Results must always be validated by a qualified dental professional. It does not replace professional clinical judgment.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE65100)
            )
        }
    }
}
