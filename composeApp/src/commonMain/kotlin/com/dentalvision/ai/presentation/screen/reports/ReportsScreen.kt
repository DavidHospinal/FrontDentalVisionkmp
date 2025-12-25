package com.dentalvision.ai.presentation.screen.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dentalvision.ai.presentation.component.ExtendedIcons
import com.dentalvision.ai.presentation.component.MainScaffold
import com.dentalvision.ai.presentation.theme.DentalColors

/**
 * Reports Screen - Responsive
 * Manage, download and visualize dental analysis reports
 */
@Composable
fun ReportsScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    MainScaffold(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        onLogout = onLogout
    ) { paddingValues ->
        ReportsContent(
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun ReportsContent(
    modifier: Modifier = Modifier
) {
    // Demo reports data
    val reports = remember {
        listOf(
            ReportData("REP-2025-001", "Maria Gonzalez", "Dec 25, 2025", "AI Analysis", "Completed"),
            ReportData("REP-2025-002", "Juan Perez", "Dec 24, 2025", "Diagnosis", "Completed"),
            ReportData("REP-2025-003", "Ana Lopez", "Dec 23, 2025", "AI Analysis", "Completed"),
            ReportData("REP-2025-004", "Carlos Gomez", "Dec 22, 2025", "Treatment", "Pending")
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                if (isMobile) {
                    Column {
                        Text(
                            text = "Reports",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage and download reports",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DentalColors.Primary
                            )
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Generate Report")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Reports Management",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manage, download and share all generated reports",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DentalColors.Primary
                            )
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Generate Report")
                        }
                    }
                }
            }

            // Stats Cards
            item {
                if (isMobile) {
                    // Mobile: 2x2 Grid
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Total",
                                value = "4",
                                icon = ExtendedIcons.Description,
                                color = DentalColors.Primary,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Analysis",
                                value = "4",
                                icon = ExtendedIcons.BarChart,
                                color = DentalColors.Success,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Diagnosis",
                                value = "0",
                                icon = ExtendedIcons.LocalHospital,
                                color = DentalColors.Warning,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Treatment",
                                value = "0",
                                icon = ExtendedIcons.MedicalServices,
                                color = DentalColors.Secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    // Desktop: 1x4 Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Total Reports",
                            value = "4",
                            icon = ExtendedIcons.Description,
                            color = DentalColors.Primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Analysis",
                            value = "4",
                            icon = ExtendedIcons.BarChart,
                            color = DentalColors.Success,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Diagnoses",
                            value = "0",
                            icon = ExtendedIcons.LocalHospital,
                            color = DentalColors.Warning,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Treatments",
                            value = "0",
                            icon = ExtendedIcons.MedicalServices,
                            color = DentalColors.Secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search reports by patient...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, "Search")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Reports List/Grid
            if (isMobile) {
                // Mobile: Vertical List
                items(reports) { report ->
                    ReportCardMobile(report)
                }
            } else {
                // Desktop: Grid layout
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.heightIn(max = 2000.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(reports) { report ->
                            ReportCardDesktop(report)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
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
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReportCardMobile(report: ReportData) {
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.id,
                        style = MaterialTheme.typography.labelMedium,
                        color = DentalColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = report.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = report.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Badge(
                    containerColor = when (report.status) {
                        "Completado" -> DentalColors.Success
                        "Pendiente" -> DentalColors.Warning
                        else -> Color.Gray
                    },
                    contentColor = Color.White
                ) {
                    Text(report.status)
                }
            }

            Spacer(Modifier.height(12.dp))

            Badge(
                containerColor = Color(0xFFF0F4FF),
                contentColor = DentalColors.Primary
            ) {
                Text(
                    report.type,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DentalColors.Primary
                    )
                ) {
                    Icon(ExtendedIcons.Visibility, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("View")
                }

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share")
                }
            }
        }
    }
}

@Composable
private fun ReportCardDesktop(report: ReportData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = report.id,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DentalColors.Primary
                )

                Badge(
                    containerColor = when (report.status) {
                        "Completado" -> DentalColors.Success
                        "Pendiente" -> DentalColors.Warning
                        else -> Color.Gray
                    },
                    contentColor = Color.White
                ) {
                    Text(report.status)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = report.patientName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = report.date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Badge(
                containerColor = Color(0xFFF0F4FF),
                contentColor = DentalColors.Primary
            ) {
                Text(
                    report.type,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DentalColors.Primary
                    )
                ) {
                    Icon(ExtendedIcons.Visibility, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("View Report")
                }

                IconButton(onClick = {}) {
                    Icon(Icons.Default.Share, "Share")
                }

                IconButton(onClick = {}) {
                    Icon(Icons.Default.Add, "Download")
                }
            }
        }
    }
}

/**
 * Data class for demo reports
 */
private data class ReportData(
    val id: String,
    val patientName: String,
    val date: String,
    val type: String,
    val status: String
)
