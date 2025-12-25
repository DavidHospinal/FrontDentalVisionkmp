package com.dentalvision.ai.presentation.screen.analysis

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dentalvision.ai.domain.model.Patient
import com.dentalvision.ai.presentation.component.ExtendedIcons
import com.dentalvision.ai.presentation.component.MainScaffold
import com.dentalvision.ai.presentation.theme.DentalColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * New Analysis Screen - Responsive
 * Permite seleccionar paciente y realizar análisis dental con IA
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAnalysisScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    MainScaffold(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        onLogout = onLogout
    ) { paddingValues ->
        NewAnalysisContent(
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewAnalysisContent(
    modifier: Modifier = Modifier
) {
    // Coroutine scope for async operations
    val scope = rememberCoroutineScope()

    // State management
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var patientDropdownExpanded by remember { mutableStateOf(false) }
    var hasImageUploaded by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisComplete by remember { mutableStateOf(false) }

    val demoPatients = remember { getDemoPatients() }

    // Responsive layout
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DentalColors.Background)
            .padding(24.dp)
    ) {
        val isMobile = maxWidth < 600.dp

        if (isMobile) {
            // Mobile Layout - Vertical Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MainContentSection(
                    selectedPatient = selectedPatient,
                    patientDropdownExpanded = patientDropdownExpanded,
                    onPatientDropdownExpandedChange = { patientDropdownExpanded = it },
                    demoPatients = demoPatients,
                    onPatientSelected = {
                        selectedPatient = it
                        patientDropdownExpanded = false
                    },
                    hasImageUploaded = hasImageUploaded,
                    onUploadImage = {
                        hasImageUploaded = true
                        isAnalyzing = true
                        // Simulate AI processing
                        scope.launch {
                            delay(2000)
                            isAnalyzing = false
                            analysisComplete = true
                        }
                    },
                    onRemoveImage = {
                        hasImageUploaded = false
                        analysisComplete = false
                    },
                    isAnalyzing = isAnalyzing,
                    analysisComplete = analysisComplete,
                    isMobile = true
                )

                if (selectedPatient != null && hasImageUploaded) {
                    AnalysisPreviewSection(
                        analysisComplete = analysisComplete,
                        isAnalyzing = isAnalyzing,
                        isMobile = true
                    )
                }
            }
        } else {
            // Desktop Layout - Horizontal Row
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                MainContentSection(
                    selectedPatient = selectedPatient,
                    patientDropdownExpanded = patientDropdownExpanded,
                    onPatientDropdownExpandedChange = { patientDropdownExpanded = it },
                    demoPatients = demoPatients,
                    onPatientSelected = {
                        selectedPatient = it
                        patientDropdownExpanded = false
                    },
                    hasImageUploaded = hasImageUploaded,
                    onUploadImage = {
                        hasImageUploaded = true
                        isAnalyzing = true
                        // Simulate AI processing
                        scope.launch {
                            delay(2000)
                            isAnalyzing = false
                            analysisComplete = true
                        }
                    },
                    onRemoveImage = {
                        hasImageUploaded = false
                        analysisComplete = false
                    },
                    isAnalyzing = isAnalyzing,
                    analysisComplete = analysisComplete,
                    modifier = Modifier.weight(2f),
                    isMobile = false
                )

                AnalysisPreviewSection(
                    analysisComplete = analysisComplete,
                    isAnalyzing = isAnalyzing,
                    modifier = Modifier.weight(1f),
                    isMobile = false
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContentSection(
    selectedPatient: Patient?,
    patientDropdownExpanded: Boolean,
    onPatientDropdownExpandedChange: (Boolean) -> Unit,
    demoPatients: List<Patient>,
    onPatientSelected: (Patient) -> Unit,
    hasImageUploaded: Boolean,
    onUploadImage: () -> Unit,
    onRemoveImage: () -> Unit,
    isAnalyzing: Boolean,
    analysisComplete: Boolean,
    modifier: Modifier = Modifier,
    isMobile: Boolean
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isMobile) Modifier.fillMaxHeight() else Modifier)
                .padding(if (isMobile) 16.dp else 24.dp)
                .then(if (!isMobile) Modifier.verticalScroll(rememberScrollState()) else Modifier)
        ) {
            // Header
            Text(
                text = if (isMobile) "Nuevo Análisis" else "New Dental Analysis",
                style = if (isMobile) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Realiza un nuevo análisis dental con IA avanzada",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step 1: Select Patient
            StepCard(
                stepNumber = 1,
                title = "Seleccionar Paciente",
                isActive = true,
                isCompleted = selectedPatient != null
            ) {
                ExposedDropdownMenuBox(
                    expanded = patientDropdownExpanded,
                    onExpandedChange = onPatientDropdownExpandedChange
                ) {
                    OutlinedTextField(
                        value = selectedPatient?.name ?: "Seleccionar paciente...",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly = true,
                        label = { Text("Paciente") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = patientDropdownExpanded,
                        onDismissRequest = { onPatientDropdownExpandedChange(false) }
                    ) {
                        demoPatients.forEach { patient ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(patient.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${patient.age} años - ${patient.phone ?: "Sin teléfono"}",
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

                if (selectedPatient != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Patient Info Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF0F4FF)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(DentalColors.Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedPatient.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${selectedPatient.age} años - ${selectedPatient.gender.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tel: ${selectedPatient.phone ?: "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Badge(
                                containerColor = DentalColors.Success,
                                contentColor = Color.White
                            ) {
                                Text("Activo")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Upload Image
            StepCard(
                stepNumber = 2,
                title = "Cargar Imagen Dental",
                isActive = selectedPatient != null,
                isCompleted = hasImageUploaded && analysisComplete
            ) {
                if (!hasImageUploaded) {
                    // Upload Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isMobile) 200.dp else 250.dp)
                            .border(
                                width = 2.dp,
                                color = DentalColors.Primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                color = Color(0xFFF0F4FF),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = ExtendedIcons.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(if (isMobile) 48.dp else 64.dp),
                                tint = DentalColors.Primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Arrastra la imagen dental aquí",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "o",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onUploadImage,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DentalColors.Primary
                                    ),
                                    enabled = selectedPatient != null
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Seleccionar")
                                }
                            }
                        }
                    }
                } else {
                    // Uploaded Image Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isMobile) 200.dp else 250.dp)
                            .background(
                                color = Color(0xFF333333),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAnalyzing) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Analizando con IA...",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = ExtendedIcons.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(100.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Badge(
                                    containerColor = DentalColors.Success,
                                    contentColor = Color.White
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Analizado")
                                    }
                                }
                            }
                        }
                    }

                    if (analysisComplete) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Analysis Results
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F7FA)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Resultados del Análisis",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Dimensiones", "1024x768 px")
                                InfoRow("Tamaño", "0.12 MB")
                                InfoRow("Formato", "PNG")
                                InfoRow("Estado", "Procesado", DentalColors.Success)
                                InfoRow("Dientes detectados", "28", DentalColors.Primary)
                                InfoRow("Caries encontradas", "3", DentalColors.Error)
                                InfoRow("Confianza promedio", "89%", DentalColors.Success)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {},
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DentalColors.Primary
                                )
                            ) {
                                Icon(Icons.Default.Done, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Guardar")
                            }
                            OutlinedButton(
                                onClick = onRemoveImage,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Nuevo")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisPreviewSection(
    analysisComplete: Boolean,
    isAnalyzing: Boolean,
    modifier: Modifier = Modifier,
    isMobile: Boolean
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isMobile) Modifier.fillMaxHeight() else Modifier)
                .padding(if (isMobile) 16.dp else 20.dp)
                .then(if (!isMobile) Modifier.verticalScroll(rememberScrollState()) else Modifier)
        ) {
            Text(
                text = "Vista Previa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (analysisComplete) {
                // Preview of processed image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(
                            color = Color(0xFF333333),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            ExtendedIcons.Image,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Imagen Dental\ncon Anotaciones IA",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Badge(
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = DentalColors.Success
                ) {
                    Text(
                        text = "Análisis completado exitosamente",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Stats
                Text(
                    text = "Resultados Rápidos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                QuickStatCard("Dientes Detectados", "28", Icons.Default.CheckCircle, DentalColors.Primary)
                QuickStatCard("Caries Detectadas", "3", Icons.Default.Warning, DentalColors.Error)
                QuickStatCard("Confianza", "89%", ExtendedIcons.BarChart, DentalColors.Success)

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DentalColors.Primary
                    )
                ) {
                    Icon(ExtendedIcons.Description, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generar Reporte")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir")
                }
            } else if (isAnalyzing) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Procesando imagen...")
                    }
                }
            } else {
                Text(
                    text = "Carga una imagen para ver la vista previa del análisis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color.White else Color(0xFFF5F7FA)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) DentalColors.Success
                            else if (isActive) DentalColors.Primary
                            else Color.Gray
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
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7FA)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

/**
 * Demo patients for when backend is unavailable
 */
private fun getDemoPatients(): List<Patient> {
    val now = Clock.System.now()
    return listOf(
        Patient(
            id = "demo-1",
            name = "Maria Rodriguez",
            age = 28,
            gender = Patient.Gender.FEMALE,
            phone = "+51 987 654 321",
            email = "maria.rodriguez@email.com",
            createdAt = now,
            updatedAt = now,
            synced = false
        ),
        Patient(
            id = "demo-2",
            name = "Juan Perez",
            age = 45,
            gender = Patient.Gender.MALE,
            phone = "+51 912 345 678",
            email = "juan.perez@email.com",
            createdAt = now,
            updatedAt = now,
            synced = false
        ),
        Patient(
            id = "demo-3",
            name = "Ana Lopez",
            age = 35,
            gender = Patient.Gender.FEMALE,
            phone = "+51 998 765 432",
            email = "ana.lopez@email.com",
            createdAt = now,
            updatedAt = now,
            synced = false
        ),
        Patient(
            id = "demo-4",
            name = "Carlos Gomez",
            age = 52,
            gender = Patient.Gender.MALE,
            phone = "+51 923 456 789",
            email = "carlos.gomez@email.com",
            createdAt = now,
            updatedAt = now,
            synced = false
        )
    )
}
