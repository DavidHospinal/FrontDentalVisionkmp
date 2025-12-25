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
import androidx.compose.ui.unit.dp
import com.dentalvision.ai.presentation.component.ExtendedIcons
import com.dentalvision.ai.presentation.component.MainScaffold
import com.dentalvision.ai.presentation.theme.DentalColors

/**
 * Appointments Screen - Responsive
 * Gestión de citas dentales con calendario
 */
@Composable
fun AppointmentsScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    MainScaffold(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        onLogout = onLogout
    ) { paddingValues ->
        AppointmentsContent(
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun AppointmentsContent(
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf(15) }

    // Demo appointments data
    val appointments = remember {
        listOf(
            AppointmentData("14:30", "Maria Gonzalez", "Análisis IA", "Programada"),
            AppointmentData("16:00", "Juan Perez", "Limpieza Dental", "Confirmada"),
            AppointmentData("17:30", "Ana Lopez", "Revisión General", "Pendiente")
        )
    }

    // Responsive Layout
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DentalColors.Background)
            .padding(24.dp)
    ) {
        val isMobile = maxWidth < 600.dp

        if (isMobile) {
            // Mobile Layout - Vertical Column
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
                        onClick = {},
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

                // Appointment Cards (Mobile)
                items(appointments) { appointment ->
                    AppointmentCardMobile(appointment)
                }
            }
        } else {
            // Desktop Layout - Row with Calendar + List
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Calendar Section
                CalendarSection(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    modifier = Modifier.weight(2f)
                )

                // Appointment List Section
                AppointmentListSection(
                    appointments = appointments,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CalendarSection(
    selectedDate: Int,
    onDateSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gestión de Citas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = {}) {
                        Icon(ExtendedIcons.FilterList, "Filtros")
                    }
                    Button(
                        onClick = {},
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
                text = "Programa, gestiona y controla todas las citas médicas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Calendar Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(ExtendedIcons.ChevronLeft, "Mes anterior")
                }

                Text(
                    text = "Diciembre 2025",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = {}) {
                    Icon(ExtendedIcons.ChevronRight, "Mes siguiente")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb").forEach { day ->
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

            // Calendar Grid (Simplified for December 2025)
            Column {
                val weeks = listOf(
                    listOf(1, 2, 3, 4, 5, 6, 7),
                    listOf(8, 9, 10, 11, 12, 13, 14),
                    listOf(15, 16, 17, 18, 19, 20, 21),
                    listOf(22, 23, 24, 25, 26, 27, 28),
                    listOf(29, 30, 31, 0, 0, 0, 0)
                )

                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        week.forEach { day ->
                            CalendarDayCell(
                                day = day,
                                isSelected = day == selectedDate,
                                hasAppointment = day in listOf(5, 9, 12, 15, 18),
                                onClick = { if (day > 0) onDateSelected(day) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentListSection(
    appointments: List<AppointmentData>,
    modifier: Modifier = Modifier
) {
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
                text = "Lunes, 15 Diciembre",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            appointments.forEach { appointment ->
                AppointmentCard(appointment)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            QuickActionButton("Programar Nueva Cita", Icons.Default.Add) {}
            QuickActionButton("Confirmar Cita", Icons.Default.Check) {}
            QuickActionButton("Enviar Recordatorio", Icons.Default.Notifications) {}
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
private fun AppointmentCard(appointment: AppointmentData) {
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
                    text = appointment.time,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                Badge(
                    containerColor = when (appointment.status) {
                        "Programada" -> DentalColors.Primary
                        "Confirmada" -> DentalColors.Success
                        else -> Color.Gray
                    },
                    contentColor = Color.White
                ) {
                    Text(appointment.status, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = appointment.patientName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = appointment.type,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DentalColors.Success
                    )
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Confirmar", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DentalColors.Error
                    )
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cancelar", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun AppointmentCardMobile(appointment: AppointmentData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        ExtendedIcons.AccessTime,
                        null,
                        tint = DentalColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        appointment.time,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Badge(
                    containerColor = when (appointment.status) {
                        "Programada" -> DentalColors.Primary
                        "Confirmada" -> DentalColors.Success
                        else -> Color.Gray
                    },
                    contentColor = Color.White
                ) {
                    Text(appointment.status)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                appointment.patientName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                appointment.type,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DentalColors.Success
                    )
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Confirmar")
                }

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DentalColors.Primary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

/**
 * Data class for demo appointments
 */
private data class AppointmentData(
    val time: String,
    val patientName: String,
    val type: String,
    val status: String
)
