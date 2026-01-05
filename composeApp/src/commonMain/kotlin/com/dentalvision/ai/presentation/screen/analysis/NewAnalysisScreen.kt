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
import com.dentalvision.ai.presentation.component.DentalImagePreview
import com.dentalvision.ai.presentation.component.ShimmerListItem
import com.dentalvision.ai.presentation.theme.DentalColors
import com.dentalvision.ai.presentation.viewmodel.AnalysisUiState
import com.dentalvision.ai.presentation.viewmodel.NewAnalysisViewModel
import com.dentalvision.ai.presentation.viewmodel.PatientsViewModel
import com.dentalvision.ai.presentation.viewmodel.SaveState
import io.github.aakira.napier.Napier
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
    // CRITICAL: Add logging to detect if screen is reached
    Napier.d("NEW ANALYSIS SCREEN: Composing - route: $currentRoute")

    // Track initialization errors
    var initError by remember { mutableStateOf<String?>(null) }

    // Load patients on first composition
    LaunchedEffect(Unit) {
        Napier.d("NEW ANALYSIS SCREEN: LaunchedEffect started")
        try {
            Napier.d("NEW ANALYSIS SCREEN: Loading patients...")
            patientsViewModel.loadPatients()
            Napier.d("NEW ANALYSIS SCREEN: Patients load initiated successfully")
        } catch (e: Exception) {
            Napier.e("NEW ANALYSIS SCREEN: CRITICAL - Failed to load patients", e)
            initError = "Failed to load patients: ${e.message}"
        }
    }

    // Show error UI if initialization failed
    if (initError != null) {
        Napier.e("NEW ANALYSIS SCREEN: Showing error UI - $initError")
        ErrorScreen(
            errorMessage = initError ?: "Unknown error",
            onNavigate = onNavigate
        )
        return
    }

    Napier.d("NEW ANALYSIS SCREEN: Rendering MainScaffold")
    MainScaffold(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        onLogout = onLogout
    ) { paddingValues ->
        Napier.d("NEW ANALYSIS SCREEN: Rendering NewAnalysisContent")
        NewAnalysisContent(
            modifier = Modifier.padding(paddingValues),
            analysisViewModel = analysisViewModel,
            patientsViewModel = patientsViewModel
        )
    }
    Napier.d("NEW ANALYSIS SCREEN: MainScaffold rendered successfully")
}

/**
 * Error screen shown when NewAnalysisScreen fails to initialize
 */
@Composable
private fun ErrorScreen(
    errorMessage: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error Loading Analysis Screen",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onNavigate("dashboard") }) {
                Text("Return to Dashboard")
            }
        }
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
    val saveState by analysisViewModel.saveState.collectAsState()

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
                    saveState = saveState,
                    onSaveAnalysis = { analysisViewModel.saveAnalysisToBackend() },
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
                    saveState = saveState,
                    onSaveAnalysis = { analysisViewModel.saveAnalysisToBackend() },
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
    saveState: SaveState,
    onSaveAnalysis: () -> Unit,
    modifier: Modifier = Modifier,
    isMobile: Boolean
) {
    var patientSearchQuery by remember { mutableStateOf("") }

    // Filter patients based on search query
    val filteredPatients = remember(patients, patientSearchQuery) {
        if (patientSearchQuery.isBlank()) {
            patients
        } else {
            patients.filter { patient ->
                patient.name.contains(patientSearchQuery, ignoreCase = true) ||
                        patient.id.contains(patientSearchQuery, ignoreCase = true) ||
                        (patient.email?.contains(patientSearchQuery, ignoreCase = true) == true) ||
                        (patient.phone?.contains(patientSearchQuery, ignoreCase = true) == true)
            }
        }
    }

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
                    onExpandedChange = {
                        onPatientDropdownExpandedChange(it)
                        if (!it) {
                            // Clear search when dropdown closes
                            patientSearchQuery = ""
                        }
                    }
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
                        onDismissRequest = {
                            onPatientDropdownExpandedChange(false)
                            patientSearchQuery = ""
                        }
                    ) {
                        // Search field inside dropdown
                        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            OutlinedTextField(
                                value = patientSearchQuery,
                                onValueChange = { patientSearchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search patient by name, ID...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search"
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DentalColors.Primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        if (patients.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No patients found. Please add patients first.") },
                                onClick = {}
                            )
                        } else if (filteredPatients.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No patients found for \"$patientSearchQuery\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            filteredPatients.forEach { patient ->
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
                                    onClick = {
                                        onPatientSelected(patient)
                                        patientSearchQuery = ""
                                    },
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
                title = "Upload Dental Photo",
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

                // Save Analysis Button
                Spacer(modifier = Modifier.height(16.dp))
                SaveAnalysisButton(
                    saveState = saveState,
                    onSave = onSaveAnalysis
                )
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

            // Show processed image with zoom capability
            io.github.aakira.napier.Napier.d("Loading processed image from URL: ${analysis.imageUrl}")

            DentalImagePreview(
                imageData = analysis.imageUrl,
                contentDescription = "Processed dental X-ray with detections",
                showZoomControls = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(Modifier.height(8.dp))

            // Detection box legend
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Green box legend - Healthy teeth
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(DentalColors.Success, RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = "Healthy Teeth",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }

                    // Red box legend - Detected cavities
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(DentalColors.Error, RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = "Detected Cavities",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
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
                    Icon(Icons.Default.Delete, "Clear image", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Progress indicator
        Card(
            colors = CardDefaults.cardColors(containerColor = DentalColors.Primary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = DentalColors.Primary
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Analyzing with AI...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "$progress%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DentalColors.Primary
                )
            }
        }

        // Shimmer loading effects for results preview
        Text(
            text = "Processing results...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        repeat(3) {
            ShimmerListItem()
        }
    }
}

@Composable
private fun ImageReadyPreview(imageData: com.dentalvision.ai.presentation.viewmodel.ImageData) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Image info header
        Card(
            colors = CardDefaults.cardColors(containerColor = DentalColors.Primary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Image Ready",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DentalColors.Primary
                    )
                    Text(
                        text = imageData.name,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "${imageData.bytes.size / 1024} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Dental Image Preview with Zoom/Pan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            DentalImagePreview(
                imageData = imageData.bytes,
                contentDescription = "Dental image: ${imageData.name}",
                showZoomControls = true
            )
        }

        // Info message
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
                text = "Use pinch gestures to zoom. Click 'Analyze with AI' to start detection",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SaveAnalysisButton(
    saveState: SaveState,
    onSave: () -> Unit
) {
    Button(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth(),
        enabled = saveState !is SaveState.Saving,
        colors = ButtonDefaults.buttonColors(
            containerColor = when (saveState) {
                is SaveState.Success -> DentalColors.Success
                is SaveState.Error -> DentalColors.Error
                else -> DentalColors.Primary
            }
        )
    ) {
        when (saveState) {
            is SaveState.Saving -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Saving to Backend...")
            }
            is SaveState.Success -> {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Saved! ID: ${saveState.analysisId.takeLast(8)}")
            }
            is SaveState.Error -> {
                Icon(Icons.Default.Warning, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Error: ${saveState.message}")
            }
            is SaveState.Idle -> {
                Icon(ExtendedIcons.Save, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Analysis to Backend")
            }
        }
    }

    // Show success message
    if (saveState is SaveState.Success) {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = DentalColors.Success.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = DentalColors.Success,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Analysis saved successfully! It will appear in Reports module.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // Show error details
    if (saveState is SaveState.Error) {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = DentalColors.Error.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = DentalColors.Error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Failed to save analysis to backend",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = saveState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
