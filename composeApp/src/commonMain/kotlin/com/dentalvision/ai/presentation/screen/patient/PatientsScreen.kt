package com.dentalvision.ai.presentation.screen.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.dentalvision.ai.presentation.viewmodel.PatientsUiState
import com.dentalvision.ai.presentation.viewmodel.PatientsViewModel
import com.dentalvision.ai.presentation.theme.DentalColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    viewModel: PatientsViewModel = koinViewModel(),
    onNavigateToNewPatient: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showNewPatientDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gestion de Pacientes", fontWeight = FontWeight.Bold)
                        Text(
                            "Administra y consulta la informacion de todos los pacientes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DentalColors.Primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    Button(
                        onClick = { showNewPatientDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = DentalColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Nuevo Paciente")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchPatients(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o telefono...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            when (uiState) {
                is PatientsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PatientsUiState.Error -> {
                    Text((uiState as PatientsUiState.Error).message, color = Color.Red)
                }
                is PatientsUiState.Empty -> {
                    Text("No hay pacientes registrados")
                }
                is PatientsUiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(patients) { patient ->
                            PatientListItem(
                                patient = patient,
                                onEdit = { showNewPatientDialog = true },
                                onDelete = { viewModel.deletePatient(patient.id) {} }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewPatientDialog) {
        PatientFormDialog(
            onDismiss = { showNewPatientDialog = false },
            onSave = { patient ->
                viewModel.createPatient(patient) {
                    showNewPatientDialog = false
                }
            }
        )
    }
}

@Composable
fun PatientListItem(
    patient: Patient,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DentalColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    patient.name.take(2).uppercase(),
                    color = DentalColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(patient.name, fontWeight = FontWeight.SemiBold)
                Text("${patient.age} anos - ${patient.phone ?: "Sin telefono"}", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red)
            }
        }
    }
}
