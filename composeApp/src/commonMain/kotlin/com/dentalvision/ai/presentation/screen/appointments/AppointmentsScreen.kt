package com.dentalvision.ai.presentation.screen.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dentalvision.ai.domain.model.Appointment
import com.dentalvision.ai.domain.model.AppointmentStatus
import com.dentalvision.ai.domain.model.AppointmentType
import com.dentalvision.ai.presentation.component.ExtendedIcons
import com.dentalvision.ai.presentation.component.MainScaffold
import com.dentalvision.ai.presentation.component.NewAppointmentDialog
import com.dentalvision.ai.presentation.theme.DentalColors
import com.dentalvision.ai.presentation.viewmodel.AppointmentsUiState
import com.dentalvision.ai.presentation.viewmodel.AppointmentsViewModel
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppointmentsScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: AppointmentsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val recentPatients by viewModel.recentPatients.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showNewAppointmentDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf<Appointment?>(null) }
    var showCancelDialog by remember { mutableStateOf<Appointment?>(null) }

    MainScaffold(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        onLogout = onLogout
    ) { paddingValues ->
        AppointmentsContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            appointments = appointments,
            selectedDate = selectedDate,
            onDateSelected = { instant -> viewModel.selectDate(instant) },
            onNewAppointmentClick = { showNewAppointmentDialog = true },
            onConfirmClick = { showConfirmDialog = it },
            onCancelClick = { showCancelDialog = it },
            onRefresh = { viewModel.refresh() }
        )

        if (showNewAppointmentDialog) {
            // Force fresh patient data when dialog opens
            LaunchedEffect(showNewAppointmentDialog) {
                if (showNewAppointmentDialog) {
                    viewModel.resetAppointmentForm() // Reset form and reload patients
                }
            }

            // Use key to force dialog recreation each time it opens
            // This ensures selectedPatient state starts fresh (null)
            key(showNewAppointmentDialog) {
                NewAppointmentDialog(
                    patients = recentPatients,
                    onDismiss = {
                        showNewAppointmentDialog = false
                    },
                    onCreateAppointment = { patientId, date, type, observations ->
                        viewModel.createAppointment(
                            patientId = patientId,
                            appointmentDate = date,
                            appointmentType = type,
                            observations = observations,
                            onSuccess = {
                                showNewAppointmentDialog = false
                                // Refresh appointments list after creation
                                viewModel.refresh()
                            },
                            onError = { error ->
                                // TODO: Show error message
                            }
                        )
                    }
                )
            }
        }

        showConfirmDialog?.let { appointment ->
            ConfirmAppointmentDialog(
                appointment = appointment,
                onConfirm = {
                    viewModel.confirmAppointment(
                        appointment = appointment,
                        onSuccess = {
                            showConfirmDialog = null
                        },
                        onError = { error ->
                            // TODO: Show error message
                        }
                    )
                },
                onDismiss = { showConfirmDialog = null }
            )
        }

        showCancelDialog?.let { appointment ->
            CancelAppointmentDialog(
                appointment = appointment,
                onCancel = { reason ->
                    viewModel.cancelAppointment(
                        appointment = appointment,
                        reason = reason,
                        onSuccess = {
                            showCancelDialog = null
                        },
                        onError = { error ->
                            // TODO: Show error message
                        }
                    )
                },
                onDismiss = { showCancelDialog = null }
            )
        }
    }
}

@Composable
private fun AppointmentsContent(
    modifier: Modifier = Modifier,
    uiState: AppointmentsUiState,
    appointments: List<Appointment>,
    selectedDate: Instant?,
    onDateSelected: (Instant) -> Unit,
    onNewAppointmentClick: () -> Unit,
    onConfirmClick: (Appointment) -> Unit,
    onCancelClick: (Appointment) -> Unit,
    onRefresh: () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DentalColors.Background)
            .padding(24.dp)
    ) {
        val isMobile = maxWidth < 600.dp

        when (uiState) {
            is AppointmentsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DentalColors.Primary)
                }
            }

            is AppointmentsUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = DentalColors.Error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = DentalColors.Error
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRefresh) {
                        Text("Retry")
                    }
                }
            }

            is AppointmentsUiState.Empty -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        ExtendedIcons.EventBusy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No appointments scheduled",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Create your first appointment to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onNewAppointmentClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DentalColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("New Appointment")
                    }
                }
            }

            is AppointmentsUiState.Success -> {
                if (isMobile) {
                    MobileLayout(
                        appointments = appointments,
                        onNewAppointmentClick = onNewAppointmentClick,
                        onConfirmClick = onConfirmClick,
                        onCancelClick = onCancelClick
                    )
                } else {
                    DesktopLayout(
                        appointments = appointments,
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected,
                        onNewAppointmentClick = onNewAppointmentClick,
                        onConfirmClick = onConfirmClick,
                        onCancelClick = onCancelClick
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileLayout(
    appointments: List<Appointment>,
    onNewAppointmentClick: () -> Unit,
    onConfirmClick: (Appointment) -> Unit,
    onCancelClick: (Appointment) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Appointments",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage your medical appointments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNewAppointmentClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DentalColors.Primary
                )
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New Appointment")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(appointments) { appointment ->
            AppointmentCardMobile(
                appointment = appointment,
                onConfirmClick = { onConfirmClick(appointment) },
                onCancelClick = { onCancelClick(appointment) }
            )
        }
    }
}

@Composable
private fun DesktopLayout(
    appointments: List<Appointment>,
    selectedDate: Instant?,
    onDateSelected: (Instant) -> Unit,
    onNewAppointmentClick: () -> Unit,
    onConfirmClick: (Appointment) -> Unit,
    onCancelClick: (Appointment) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CalendarSection(
            selectedDate = selectedDate,
            appointments = appointments,
            onDateSelected = onDateSelected,
            onNewAppointmentClick = onNewAppointmentClick,
            modifier = Modifier.weight(2f)
        )

        AppointmentListSection(
            selectedDate = selectedDate,
            appointments = appointments,
            onConfirmClick = onConfirmClick,
            onCancelClick = onCancelClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CalendarSection(
    selectedDate: Instant?,
    appointments: List<Appointment>,
    onDateSelected: (Instant) -> Unit,
    onNewAppointmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timezone = TimeZone.currentSystemDefault()
    val now = Clock.System.now()
    val currentMonth = now.toLocalDateTime(timezone).month
    val currentYear = now.toLocalDateTime(timezone).year

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Appointment Management",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNewAppointmentClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DentalColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("New Appointment")
                    }
                }
            }

            Text(
                text = "Schedule, manage and track all medical appointments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(ExtendedIcons.ChevronLeft, "Previous month")
                }

                Text(
                    text = "$currentMonth $currentYear",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = {}) {
                    Icon(ExtendedIcons.ChevronRight, "Next month")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CalendarGrid(
                currentMonth = currentMonth.number,
                currentYear = currentYear,
                selectedDate = selectedDate,
                appointments = appointments,
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: Int,
    currentYear: Int,
    selectedDate: Instant?,
    appointments: List<Appointment>,
    onDateSelected: (Instant) -> Unit
) {
    val timezone = TimeZone.currentSystemDefault()
    val firstDay = LocalDate(currentYear, currentMonth, 1)
    // Calculate days in month by finding the last day
    val daysInMonth = when (currentMonth) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (currentYear % 4 == 0 && (currentYear % 100 != 0 || currentYear % 400 == 0)) 29 else 28
        else -> 30
    }
    val firstDayOfWeek = firstDay.dayOfWeek.isoDayNumber % 7

    val appointmentDays = appointments.map { appointment ->
        appointment.appointmentDate.toLocalDateTime(timezone).date.dayOfMonth
    }.toSet()

    Column {
        var dayCounter = 1
        for (week in 0..5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    val day = if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        0
                    } else if (dayCounter > daysInMonth) {
                        0
                    } else {
                        dayCounter++
                    }

                    val isSelected = selectedDate?.let { selected ->
                        val selectedLocal = selected.toLocalDateTime(timezone).date
                        selectedLocal.dayOfMonth == day &&
                        selectedLocal.monthNumber == currentMonth &&
                        selectedLocal.year == currentYear
                    } ?: false

                    CalendarDayCell(
                        day = day,
                        isSelected = isSelected,
                        hasAppointment = day in appointmentDays,
                        onClick = {
                            if (day > 0) {
                                val date = LocalDate(currentYear, currentMonth, day)
                                val dateTime = date.atTime(12, 0)
                                val instant = dateTime.toInstant(timezone)
                                onDateSelected(instant)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    hasAppointment: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .then(
                if (day > 0) {
                    Modifier
                        .clickable { onClick() }
                        .background(
                            color = when {
                                isSelected -> DentalColors.Primary
                                else -> Color.Transparent
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = if (hasAppointment) 2.dp else 0.dp,
                            color = if (hasAppointment) DentalColors.Success else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day > 0) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> Color.White
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isSelected || hasAppointment) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun AppointmentListSection(
    selectedDate: Instant?,
    appointments: List<Appointment>,
    onConfirmClick: (Appointment) -> Unit,
    onCancelClick: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    val timezone = TimeZone.currentSystemDefault()
    val filteredAppointments = selectedDate?.let { date ->
        val selectedLocal = date.toLocalDateTime(timezone).date
        appointments.filter { appointment ->
            val appointmentLocal = appointment.appointmentDate.toLocalDateTime(timezone).date
            appointmentLocal == selectedLocal
        }
    } ?: appointments.take(5)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = selectedDate?.let {
                    it.toLocalDateTime(timezone).date.toString()
                } ?: "Upcoming Appointments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredAppointments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No appointments for this day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                filteredAppointments.forEach { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onConfirmClick = { onConfirmClick(appointment) },
                        onCancelClick = { onCancelClick(appointment) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val timezone = TimeZone.currentSystemDefault()
    val dateTime = appointment.appointmentDate.toLocalDateTime(timezone)
    val time = "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7FA)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ExtendedIcons.AccessTime,
                    contentDescription = null,
                    tint = DentalColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                Badge(
                    containerColor = appointment.status.color,
                    contentColor = Color.White
                ) {
                    Text(appointment.status.displayName, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Display patient name prominently
            Text(
                text = appointment.patientName ?: appointment.patientId,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DentalColors.Primary
            )

            // Show patient ID in smaller text below if name is available
            if (appointment.patientName != null) {
                Text(
                    text = "ID: ${appointment.patientId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = appointment.appointmentType.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (appointment.status == AppointmentStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onConfirmClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DentalColors.Success
                        )
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Confirm", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DentalColors.Error
                        )
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else if (appointment.status == AppointmentStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Finished",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DentalColors.Success
                )
            }
        }
    }
}

@Composable
private fun AppointmentCardMobile(
    appointment: Appointment,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val timezone = TimeZone.currentSystemDefault()
    val dateTime = appointment.appointmentDate.toLocalDateTime(timezone)
    val time = "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        ExtendedIcons.AccessTime,
                        null,
                        tint = DentalColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        time,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Badge(
                    containerColor = appointment.status.color,
                    contentColor = Color.White
                ) {
                    Text(appointment.status.displayName)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                appointment.patientName ?: appointment.patientId,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                appointment.appointmentType.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (appointment.status == AppointmentStatus.PENDING) {
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onConfirmClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DentalColors.Success)
                    ) {
                        Text("Confirm")
                    }
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DentalColors.Error)
                    ) {
                        Text("Cancel")
                    }
                }
            } else if (appointment.status == AppointmentStatus.COMPLETED) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Finished",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DentalColors.Success
                )
            }
        }
    }
}

@Composable
private fun ConfirmAppointmentDialog(
    appointment: Appointment,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Confirm Appointment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DentalColors.Primary
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Patient: ${appointment.patientName ?: appointment.patientId}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Type: ${appointment.appointmentType.displayName}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF4E5)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Upon confirmation, the patient will be available for AI Analysis modules",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF663C00)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DentalColors.Success
                        )
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun CancelAppointmentDialog(
    appointment: Appointment,
    onCancel: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var cancellationReason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Cancel Appointment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DentalColors.Error
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Patient: ${appointment.patientName ?: appointment.patientId}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Type: ${appointment.appointmentType.displayName}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = cancellationReason,
                    onValueChange = { cancellationReason = it },
                    label = { Text("Cancellation Reason") },
                    placeholder = { Text("Enter reason for cancellation...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DentalColors.Primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back")
                    }

                    Button(
                        onClick = {
                            if (cancellationReason.isNotBlank()) {
                                onCancel(cancellationReason)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = cancellationReason.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DentalColors.Error
                        )
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel Appointment")
                    }
                }
            }
        }
    }
}

private val AppointmentStatus.color: Color
    get() = when (this) {
        AppointmentStatus.PENDING -> Color(0xFFFFA726)
        AppointmentStatus.SCHEDULED -> DentalColors.Primary
        AppointmentStatus.CONFIRMED -> DentalColors.Success
        AppointmentStatus.COMPLETED -> Color(0xFF66BB6A)
        AppointmentStatus.CANCELLED -> DentalColors.Error
        AppointmentStatus.NO_SHOW -> Color(0xFF757575)
    }

private val AppointmentStatus.displayName: String
    get() = when (this) {
        AppointmentStatus.PENDING -> "Pending"
        AppointmentStatus.SCHEDULED -> "Scheduled"
        AppointmentStatus.CONFIRMED -> "Confirmed"
        AppointmentStatus.COMPLETED -> "Completed"
        AppointmentStatus.CANCELLED -> "Cancelled"
        AppointmentStatus.NO_SHOW -> "No Show"
    }
