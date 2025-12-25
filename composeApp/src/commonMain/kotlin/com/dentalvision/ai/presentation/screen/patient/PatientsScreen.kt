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
import com.dentalvision.ai.presentation.component.NavigationDrawerContent
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: PatientsViewModel = koinViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showNewPatientDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                    onLogout = onLogout,
                    onCloseDrawer = {
                        kotlinx.coroutines.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
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
                    navigationIcon = {
                        IconButton(onClick = { kotlinx.coroutines.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                        }
                    },
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
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchPatients(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por nombre o telefono...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                // Content based on UI State
                when (val state = uiState) {
                    is PatientsUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is PatientsUiState.Error -> {
                        // Show demo data when error occurs
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Backend no disponible",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            "Mostrando datos de demostración",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Show demo patients
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(getDemoPatients()) { patient ->
                                    PatientListItem(
                                        patient = patient,
                                        onEdit = { showNewPatientDialog = true },
                                        onDelete = { }
                                    )
                                }
                            }
                        }
                    }
                    is PatientsUiState.Empty -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(16.dp))
                                Text("No hay pacientes registrados")
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = { showNewPatientDialog = true }) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Agregar Primer Paciente")
                                }
                            }
                        }
                    }
                    is PatientsUiState.Success -> {
                        if (patients.isEmpty()) {
                            // Show demo data if backend returns empty
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(getDemoPatients()) { patient ->
                                    PatientListItem(
                                        patient = patient,
                                        onEdit = { showNewPatientDialog = true },
                                        onDelete = { }
                                    )
                                }
                            }
                        } else {
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

/**
 * Demo patients shown when backend is unavailable
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
        )
    )
}
