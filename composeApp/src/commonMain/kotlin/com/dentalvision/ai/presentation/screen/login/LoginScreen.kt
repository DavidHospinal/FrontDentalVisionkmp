package com.dentalvision.ai.presentation.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dentalvision.ai.presentation.component.WelcomeDialog
import com.dentalvision.ai.presentation.theme.DentalColors

/**
 * Login Screen
 * Simplified white minimal design for doctor name entry
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (email: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var doctorName by remember { mutableStateOf("") }
    var showWelcomeDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo Placeholder (replace with actual dental0.png resource)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(DentalColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DV",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Dental Vision AI Pro",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DentalColors.Primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sistema Integral de Análisis Dental con IA",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Medical Warning
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = Color(0xFFFF9800),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF8E1)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Advertencia Médica",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Esta es una herramienta de soporte diagnóstico. Los resultados deben ser validados por un profesional dental calificado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF827717),
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Doctor Name Field
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre del Doctor") },
                    placeholder = { Text("Ingrese su primer nombre para ingresar al sistema") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Doctor Icon",
                            tint = DentalColors.Primary
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DentalColors.Primary,
                        focusedLabelColor = DentalColors.Primary,
                        cursorColor = DentalColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Login Button
                Button(
                    onClick = {
                        if (doctorName.isNotBlank()) {
                            showWelcomeDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = doctorName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DentalColors.Primary,
                        disabledContainerColor = DentalColors.Primary.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Entrar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer
                Text(
                    text = "Acceso seguro para profesionales médicos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Welcome Dialog
    if (showWelcomeDialog) {
        WelcomeDialog(
            doctorName = doctorName,
            onDismiss = {
                showWelcomeDialog = false
                onLoginSuccess(doctorName)
            }
        )
    }
}
