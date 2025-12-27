package com.dentalvision.ai.presentation.screen.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dentalvision.ai.domain.model.Patient
import com.dentalvision.ai.presentation.component.ExtendedIcons
import com.dentalvision.ai.presentation.component.MainScaffold
import com.dentalvision.ai.presentation.theme.DentalColors
import com.dentalvision.ai.presentation.viewmodel.AnalysisUiState
import com.dentalvision.ai.presentation.viewmodel.NewAnalysisViewModel
import com.dentalvision.ai.presentation.viewmodel.PatientsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * New Analysis Screen - REAL INTEGRATION
 * Connects to YOLOv12 API via HuggingFace for actual dental analysis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAnalysisScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    analysisViewModel: NewAnalysisViewModel = koinViewModel(),
    patientsViewModel: PatientsViewModel = koinViewModel()
) {
    // Load patients on first composition
    LaunchedEffect(Unit) {
        patientsViewModel.loadPatients()
    }

    MainScaffold(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        onLogout = onLogout
    ) { paddingValues ->
        NewAnalysisContent(
            modifier = Modifier.padding(paddingValues),
            analysisViewModel = analysisViewModel,
            patientsViewModel = patientsViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewAnalysisContent(
    modifier: Modifier = Modifier,
    analysisViewModel: NewAnalysisViewModel,
    patientsViewModel: PatientsViewModel
) {
    val scope = rememberCoroutineScope()
    val uiState by analysisViewModel.uiState.collectAsState()
    val selectedImage by analysisViewModel.selectedImage.collectAsState()
    val analysisResult by analysisViewModel.analysisResult.collectAsState()

    val patients by patientsViewModel.patients.collectAsState()
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var patientDropdownExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DentalColors.Background)
            .padding(24.dp)
    ) {
        val isMobile = maxWidth < 600.dp

        if (isMobile) {
            // Mobile Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MainContentSection(
                    selectedPatient = selectedPatient,
                    patients = patients,
                    patientDropdownExpanded = patientDropdownExpanded,
                    onPatientDropdownExpandedChange = { patientDropdownExpanded = it },
                    onPatientSelected = {
                        selectedPatient = it
                        patientDropdownExpanded = false
                    },
                    selectedImage = selectedImage,
                    onSelectImage = { analysisViewModel.selectImage() },
                    onAnalyze = {
                        selectedPatient?.let { patient ->
                            analysisViewModel.analyzeImage(patient.id)
                        }
                    },
                    onClearImage = { analysisViewModel.clearImage() },
                    uiState = uiState,
                    analysisResult = analysisResult,
                    isMobile = true
                )

                if (selectedImage != null) {
                    AnalysisPreviewSection(
                        selectedImage = selectedImage,
                        analysisResult = analysisResult,
                        uiState = uiState,
                        isMobile = true
                    )
                }
            }
        } else {
            // Desktop Layout
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                MainContentSection(
                    selectedPatient = selectedPatient,
                    patients = patients,
                    patientDropdownExpanded = patientDropdownExpanded,
                    onPatientDropdownExpandedChange = { patientDropdownExpanded = it },
                    onPatientSelected = {
                        selectedPatient = it
                        patientDropdownExpanded = false
                    },
                    selectedImage = selectedImage,
                    onSelectImage = { analysisViewModel.selectImage() },
                    onAnalyze = {
                        selectedPatient?.let { patient ->
                            analysisViewModel.analyzeImage(patient.id)
                        }
                    },
                    onClearImage = { analysisViewModel.clearImage() },
                    uiState = uiState,
                    analysisResult = analysisResult,
                    modifier = Modifier.weight(2f),
                    isMobile = false
                )

                AnalysisPreviewSection(
                    selectedImage = selectedImage,
                    analysisResult = analysisResult,
                    uiState = uiState,
                    modifier = Modifier.weight(1f),
                    isMobile = false
                )
            }
        }
    }

    // Error Dialog
    if (uiState is AnalysisUiState.Error) {
        AlertDialog(
            onDismissRequest = { analysisViewModel.reset() },
            title = { Text("Analysis Error") },
            text = { Text((uiState as AnalysisUiState.Error).message) },
            confirmButton = {
                TextButton(onClick = { analysisViewModel.reset() }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContentSection(
    selectedPatient: Patient?,
    patients: List<Patient>,
    patientDropdownExpanded: Boolean,
    onPatientDropdownExpandedChange: (Boolean) -> Unit,
    onPatientSelected: (Patient) -> Unit,
    selectedImage: com.dentalvision.ai.presentation.viewmodel.ImageData?,
    onSelectImage: () -> Unit,
    onAnalyze: () -> Unit,
    onClearImage: () -> Unit,
    uiState: AnalysisUiState,
    analysisResult: com.dentalvision.ai.domain.model.Analysis?,
    modifier: Modifier = Modifier,
    isMobile: Boolean
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isMobile) Modifier.fillMaxHeight() else Modifier)
                .padding(if (isMobile) 16.dp else 24.dp)
                .then(if (!isMobile) Modifier.verticalScroll(rememberScrollState()) else Modifier)
        ) {
            Text(
                text = "New Dental Analysis",
                style = if (isMobile) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "AI-powered dental cavity detection using YOLOv12",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step 1: Select Patient
            StepCard(
                stepNumber = 1,
                title = "Select Patient",
                isActive = true,
                isCompleted = selectedPatient != null
            ) {
                ExposedDropdownMenuBox(
                    expanded = patientDropdownExpanded,
                    onExpandedChange = onPatientDropdownExpandedChange
                ) {
                    OutlinedTextField(
                        value = selectedPatient?.name ?: "Select patient...",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly = true,
                        label = { Text("Patient") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = patientDropdownExpanded,
                        onDismissRequest = { onPatientDropdownExpandedChange(false) }
                    ) {
                        if (patients.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No patients found. Please add patients first.") },
                                onClick = {}
                            )
                        } else {
                            patients.forEach { patient ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(patient.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "${patient.age} years - ${patient.phone ?: "No phone"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = { onPatientSelected(patient) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(DentalColors.Primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                patient.name.take(2).uppercase(),
                                                color = DentalColors.Primary,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedPatient != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    PatientInfoCard(selectedPatient)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Upload Image
            StepCard(
                stepNumber = 2,
                title = "Upload Dental X-Ray",
                isActive = selectedPatient != null,
                isCompleted = selectedImage != null
            ) {
                if (selectedImage == null) {
                    UploadArea(
                        enabled = selectedPatient != null,
                        onSelectImage = onSelectImage,
                        isMobile = isMobile
                    )
                } else {
                    ImageSelectedCard(
                        imageName = selectedImage.name,
                        imageSize = selectedImage.bytes.size,
                        onClear = onClearImage,
                        uiState = uiState
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 3: Analyze
            if (selectedImage != null && selectedPatient != null) {
                Button(
                    onClick = onAnalyze,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is AnalysisUiState.Analyzing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DentalColors.Primary
                    )
                ) {
                    if (uiState is AnalysisUiState.Analyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Analyzing... ${(uiState as AnalysisUiState.Analyzing).progress}%")
                    } else {
                        Icon(Icons.Default.Search, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Analyze with AI")
                    }
                }
            }

            // Results Summary
            if (analysisResult != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ResultsSummaryCard(analysisResult)
            }
        }
    }
}

@Composable
private fun AnalysisPreviewSection(
    selectedImage: com.dentalvision.ai.presentation.viewmodel.ImageData?,
    analysisResult: com.dentalvision.ai.domain.model.Analysis?,
    uiState: AnalysisUiState,
    modifier: Modifier = Modifier,
    isMobile: Boolean
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isMobile) Modifier.fillMaxHeight() else Modifier)
                .padding(if (isMobile) 16.dp else 20.dp)
        ) {
            Text(
                text = "Analysis Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                selectedImage == null -> {
                    EmptyPreview()
                }
                uiState is AnalysisUiState.Analyzing -> {
                    AnalyzingPreview((uiState as AnalysisUiState.Analyzing).progress)
                }
                analysisResult != null -> {
                    CompletedAnalysisPreview(selectedImage, analysisResult)
                }
                else -> {
                    ImageReadyPreview(selectedImage)
                }
            }
        }
    }
}

@Composable
private fun CompletedAnalysisPreview(
    imageData: com.dentalvision.ai.presentation.viewmodel.ImageData,
    analysis: com.dentalvision.ai.domain.model.Analysis
) {
    // Processed Image with Detections
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Processed Image",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            // Show processed image from backend with detection boxes
            io.kamel.image.KamelImage(
                resource = io.kamel.image.asyncPainterResource(data = analysis.imageUrl),
                contentDescription = "Processed dental X-ray with detections",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                onLoading = { progress ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color(0xFF34495E)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DentalColors.Primary)
                    }
                },
                onFailure = { exception ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color(0xFF34495E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = DentalColors.Error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Failed to load processed image",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    // Oral Health Progress Bars (Verde/Rojo)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Oral Health Index",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            val healthyPercentage = if (analysis.totalTeethDetected > 0) {
                (analysis.totalTeethDetected - analysis.totalCariesDetected).toFloat() / analysis.totalTeethDetected
            } else 0f

            val cariesPercentage = if (analysis.totalTeethDetected > 0) {
                analysis.totalCariesDetected.toFloat() / analysis.totalTeethDetected
            } else 0f

            // Healthy Teeth Bar (Green)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Healthy",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(60.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(healthyPercentage)
                            .background(DentalColors.Success)
                    )
                }

                Text(
                    text = "${(healthyPercentage * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = DentalColors.Success,
                    modifier = Modifier.padding(start = 8.dp).width(40.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Caries Bar (Red)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Caries",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(60.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(cariesPercentage)
                            .background(DentalColors.Error)
                    )
                }

                Text(
                    text = "${(cariesPercentage * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = DentalColors.Error,
                    modifier = Modifier.padding(start = 8.dp).width(40.dp)
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // Quick stats
    QuickStatsCard(analysis)

    Spacer(Modifier.height(12.dp))

    // Detections List
    if (analysis.detections.isNotEmpty()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Detected Teeth (${analysis.detections.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                analysis.detections.take(5).forEach { detection ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tooth #${detection.toothNumberFDI}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Badge(
                            containerColor = if (detection.hasCaries) DentalColors.Error else DentalColors.Success,
                            contentColor = Color.White
                        ) {
                            Text(
                                if (detection.hasCaries) "Cavity" else "Healthy",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                if (analysis.detections.size > 5) {
                    Text(
                        text = "... and ${analysis.detections.size - 5} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsCard(analysis: com.dentalvision.ai.domain.model.Analysis) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            StatRow("Teeth Detected", analysis.totalTeethDetected.toString(), DentalColors.Primary)
            StatRow("Cavities Found", analysis.totalCariesDetected.toString(), DentalColors.Error)
            StatRow("Confidence", "${(analysis.confidenceScore * 100).toInt()}%", DentalColors.Success)
            StatRow("Status", analysis.severityLevel.displayName, Color.Gray)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodySmall)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ============= HELPER COMPOSABLES =============

@Composable
private fun StepCard(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surface else Color(0xFFF5F7FA)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Step number badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> DentalColors.Success
                                isActive -> DentalColors.Primary
                                else -> Color.Gray
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun PatientInfoCard(patient: Patient) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DentalColors.Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    patient.name.take(2).uppercase(),
                    color = DentalColors.Primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Column {
                Text(
                    text = patient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${patient.age} years old",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                patient.phone?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadArea(
    enabled: Boolean,
    onSelectImage: () -> Unit,
    isMobile: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isMobile) 180.dp else 200.dp)
            .border(
                width = 2.dp,
                color = if (enabled) DentalColors.Primary.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (enabled) DentalColors.Primary.copy(alpha = 0.05f) else Color.Gray.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (enabled) DentalColors.Primary else Color.Gray
            )

            Text(
                text = if (enabled) "Click to upload X-Ray image" else "Select a patient first",
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "PNG, JPG, JPEG (Max 10MB)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (enabled) {
                Button(
                    onClick = onSelectImage,
                    colors = ButtonDefaults.buttonColors(containerColor = DentalColors.Primary)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Select Image")
                }
            }
        }
    }
}

@Composable
private fun ImageSelectedCard(
    imageName: String,
    imageSize: Int,
    onClear: () -> Unit,
    uiState: AnalysisUiState
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = DentalColors.Success,
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    Text(
                        text = imageName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${imageSize / 1024} KB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState !is AnalysisUiState.Analyzing) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, "Clear image", tint = DentalColors.Error)
                }
            }
        }
    }
}

@Composable
private fun ResultsSummaryCard(analysis: com.dentalvision.ai.domain.model.Analysis) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                analysis.totalCariesDetected > 5 -> DentalColors.Error.copy(alpha = 0.1f)
                analysis.totalCariesDetected > 0 -> DentalColors.Warning.copy(alpha = 0.1f)
                else -> DentalColors.Success.copy(alpha = 0.1f)
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analysis Complete",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Badge(
                    containerColor = when {
                        analysis.totalCariesDetected > 5 -> DentalColors.Error
                        analysis.totalCariesDetected > 0 -> DentalColors.Warning
                        else -> DentalColors.Success
                    },
                    contentColor = Color.White
                ) {
                    Text(analysis.severityLevel.displayName)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SummaryItem(
                    label = "Teeth",
                    value = analysis.totalTeethDetected.toString(),
                    icon = ExtendedIcons.LocalHospital,
                    color = DentalColors.Primary
                )

                SummaryItem(
                    label = "Cavities",
                    value = analysis.totalCariesDetected.toString(),
                    icon = Icons.Default.Warning,
                    color = DentalColors.Error
                )

                SummaryItem(
                    label = "Confidence",
                    value = "${(analysis.confidenceScore * 100).toInt()}%",
                    icon = Icons.Default.Check,
                    color = DentalColors.Success
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "No image selected",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun AnalyzingPreview(progress: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(DentalColors.Primary.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = DentalColors.Primary
            )

            Text(
                text = "Analyzing with AI...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "$progress%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DentalColors.Primary
            )

            Text(
                text = "Processing dental image with YOLOv12",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ImageReadyPreview(imageData: com.dentalvision.ai.presentation.viewmodel.ImageData) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = DentalColors.Primary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Image Ready",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DentalColors.Primary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = imageData.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${imageData.bytes.size / 1024} KB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = DentalColors.Warning,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Click 'Analyze with AI' to start detection",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
