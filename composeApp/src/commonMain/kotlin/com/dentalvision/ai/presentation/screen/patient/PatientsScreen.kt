package com.dentalvision.ai.presentation.screen.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
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
import com.dentalvision.ai.presentation.component.AppSearchField
import com.dentalvision.ai.presentation.component.ShimmerListItem
import com.dentalvision.ai.presentation.component.EmptyStates
import org.koin.compose.viewmodel.koinViewModel
import io.github.aakira.napier.Napier

/**
 * Patients Screen - REAL BACKEND INTEGRATION
 * Shows ONLY real data from backend, NO demo data fallback
 */
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

    // LOGGING: Track state changes
    LaunchedEffect(uiState) {
        Napier.d("PATIENTS SCREEN: UI State changed to ${uiState::class.simpleName}")
        when (val state = uiState) {
            is PatientsUiState.Error -> {
                Napier.e("PATIENTS SCREEN: Error state - ${state.message}")
            }
            is PatientsUiState.Success -> {
                Napier.i("PATIENTS SCREEN: Success state - ${patients.size} patients loaded")
            }
            is PatientsUiState.Empty -> {
                Napier.w("PATIENTS SCREEN: Empty state - no patients in backend")
            }
            is PatientsUiState.Loading -> {
                Napier.d("PATIENTS SCREEN: Loading state")
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                    onLogout = onLogout
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Patient Management", fontWeight = FontWeight.Bold)
                            Text(
                                text = "Manage and consult all patient information",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DentalColors.Primary,
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                Napier.d("PATIENTS SCREEN: Refresh button clicked")
                                viewModel.refresh()
                            }
                        ) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                        }
                        Button(
                            onClick = { showNewPatientDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = DentalColors.Primary
                            )
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("New Patient")
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
                // Search Bar with enhanced UX
                AppSearchField(
                    value = searchQuery,
                    onValueChange = {
                        Napier.d("PATIENTS SCREEN: Search query changed to: $it")
                        viewModel.searchPatients(it)
                    },
                    placeholder = "Search by name or phone...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Content based on UI State - NO DEMO DATA
                when (val state = uiState) {
                    is PatientsUiState.Loading -> {
                        // Professional shimmer loading effect
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Loading patients from backend...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            repeat(5) {
                                ShimmerListItem()
                            }
                        }
                    }
                    is PatientsUiState.Error -> {
                        // SHOW REAL ERROR - NO DEMO DATA
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
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Backend Connection Failed",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            Text(
                                                "Unable to load patients from server",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    HorizontalDivider()

                                    Spacer(Modifier.height(12.dp))

                                    // Show full error message
                                    Text(
                                        "Error Details:",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    SelectionContainer {
                                        Text(
                                            state.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            Napier.d("PATIENTS SCREEN: Retry button clicked after error")
                                            viewModel.refresh()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = DentalColors.Primary
                                        )
                                    ) {
                                        Icon(Icons.Default.Refresh, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Retry Connection")
                                    }
                                }
                            }
                        }
                    }
                    is PatientsUiState.Empty -> {
                        // Professional empty state
                        EmptyStates.NoPatients(
                            onAddPatient = {
                                Napier.d("PATIENTS SCREEN: Add first patient clicked from empty state")
                                showNewPatientDialog = true
                            }
                        )
                    }
                    is PatientsUiState.Success -> {
                        if (patients.isEmpty()) {
                            // Backend returned success but list is empty
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "No patients found",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    if (searchQuery.isNotEmpty()) {
                                        Text(
                                            "No results for: \"$searchQuery\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Text(
                                            "Start by adding your first patient",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Button(onClick = {
                                        Napier.d("PATIENTS SCREEN: Add patient clicked from success-empty state")
                                        showNewPatientDialog = true
                                    }) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Add Patient")
                                    }
                                }
                            }
                        } else {
                            // Show REAL patients from backend
                            Column {
                                Text(
                                    "Loaded ${patients.size} patient${if (patients.size != 1) "s" else ""} from backend",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DentalColors.Success,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(8.dp))
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(patients) { patient ->
                                        PatientListItem(
                                            patient = patient,
                                            onEdit = {
                                                Napier.d("PATIENTS SCREEN: Edit patient ${patient.id}")
                                                showNewPatientDialog = true
                                            },
                                            onDelete = {
                                                Napier.d("PATIENTS SCREEN: Delete patient ${patient.id}")
                                                viewModel.deletePatient(patient.id) {}
                                            }
                                        )
                                    }
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
            onDismiss = {
                Napier.d("PATIENTS SCREEN: Patient form dialog dismissed")
                showNewPatientDialog = false
            },
            onSave = { patient ->
                Napier.d("PATIENTS SCREEN: Creating new patient: ${patient.name}")
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
                Text("${patient.age} years - ${patient.phone ?: "No phone"}", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
            }
        }
    }
}
