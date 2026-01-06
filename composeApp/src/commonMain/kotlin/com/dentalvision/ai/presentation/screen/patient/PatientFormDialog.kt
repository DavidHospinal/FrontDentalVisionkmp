package com.dentalvision.ai.presentation.screen.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dentalvision.ai.domain.model.Patient
import com.dentalvision.ai.presentation.theme.DentalColors
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFormDialog(
    patient: Patient? = null,
    onDismiss: () -> Unit,
    onSave: (Patient) -> Unit
) {
    var name by remember { mutableStateOf(patient?.name ?: "") }
    var age by remember { mutableStateOf(patient?.age?.toString() ?: "") }
    var phone by remember { mutableStateOf(patient?.phone ?: "") }
    var email by remember { mutableStateOf(patient?.email ?: "") }
    var gender by remember { mutableStateOf(patient?.gender ?: Patient.Gender.MALE) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (patient == null) "Nuevo Paciente" else "Editar Paciente") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it.filter { c -> c.isDigit() } },
                        label = { Text("Edad *") },
                        modifier = Modifier.weight(1f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = gender.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Genero") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                            Patient.Gender.values().forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g.name) },
                                    onClick = {
                                        gender = g
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefono") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newPatient = Patient(
                        id = patient?.id ?: "",
                        name = name,
                        age = age.toIntOrNull() ?: 0,
                        gender = gender,
                        phone = phone.ifBlank { null },
                        email = email.ifBlank { null },
                        createdAt = patient?.createdAt ?: Clock.System.now(),
                        updatedAt = Clock.System.now()
                    )
                    onSave(newPatient)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DentalColors.Primary),
                enabled = name.isNotBlank() && age.isNotBlank()
            ) {
                Text("Guardar Paciente")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
