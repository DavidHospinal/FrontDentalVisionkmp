package com.dentalvision.ai.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dentalvision.ai.domain.model.AppointmentType
import com.dentalvision.ai.domain.model.Patient
import com.dentalvision.ai.presentation.theme.DentalColors
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentDialog(
    patients: List<Patient>,
    onDismiss: () -> Unit,
    onCreateAppointment: (
        patientId: String,
        appointmentDate: Instant,
        appointmentType: AppointmentType,
        observations: String
    ) -> Unit
) {
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedType by remember { mutableStateOf<AppointmentType?>(null) }
    var observations by remember { mutableStateOf("") }
    var showPatientDropdown by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Schedule New Appointment",
                        style = MaterialTheme.typography.titleLarge,
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

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Patient",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = showPatientDropdown,
                    onExpandedChange = { showPatientDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedPatient?.name ?: "Select patient",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Select patient"
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DentalColors.Primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = showPatientDropdown,
                        onDismissRequest = { showPatientDropdown = false }
                    ) {
                        patients.forEach { patient ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = patient.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "ID: ${patient.id}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedPatient = patient
                                    showPatientDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Date",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date field with clickable overlay
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedDate?.toString() ?: "Select date",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                imageVector = ExtendedIcons.CalendarToday,
                                contentDescription = "Select date"
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    // CRITICAL: Transparent Box AFTER TextField to capture clicks (z-index layering)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                                selectedDate = today
                                showError = false
                                println("DEBUG: Date selected - $selectedDate")
                            }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Time",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time field with clickable overlay
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedTime?.toString() ?: "Select time",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                imageVector = ExtendedIcons.Schedule,
                                contentDescription = "Select time"
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    // CRITICAL: Transparent Box AFTER TextField to capture clicks (z-index layering)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                selectedTime = LocalTime(9, 0)
                                showError = false
                                println("DEBUG: Time selected - $selectedTime")
                            }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Consultation Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = showTypeDropdown,
                    onExpandedChange = { showTypeDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedType?.displayName ?: "Select consultation type",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DentalColors.Primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false }
                    ) {
                        AppointmentType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = type.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (type == AppointmentType.AI_ANALYSIS)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedType = type
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Observations",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = observations,
                    onValueChange = { observations = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter any observations or notes...") },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DentalColors.Primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DentalColors.Primary
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            when {
                                selectedPatient == null -> {
                                    showError = true
                                    errorMessage = "Please select a patient"
                                }
                                selectedDate == null -> {
                                    showError = true
                                    errorMessage = "Please select a date"
                                }
                                selectedTime == null -> {
                                    showError = true
                                    errorMessage = "Please select a time"
                                }
                                selectedType == null -> {
                                    showError = true
                                    errorMessage = "Please select consultation type"
                                }
                                else -> {
                                    val dateTime = LocalDateTime(selectedDate!!, selectedTime!!)
                                    val instant = dateTime.toInstant(TimeZone.currentSystemDefault())

                                    onCreateAppointment(
                                        selectedPatient!!.id,
                                        instant,
                                        selectedType!!,
                                        observations
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DentalColors.Primary
                        )
                    ) {
                        Text("Create Appointment")
                    }
                }
            }
        }
    }
}
