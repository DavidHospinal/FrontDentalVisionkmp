package com.dentalvision.ai.presentation.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dentalvision.ai.domain.model.SystemStatistics
import com.dentalvision.ai.presentation.component.DonutChart
import com.dentalvision.ai.presentation.component.DoubleBarChart
import com.dentalvision.ai.presentation.component.DoubleBarChartData
import com.dentalvision.ai.presentation.component.ExtendedIcons
import com.dentalvision.ai.presentation.component.MainScaffold
import com.dentalvision.ai.presentation.component.PieChartData
import com.dentalvision.ai.presentation.theme.DentalColors
import io.github.aakira.napier.Napier

/**
 * Dashboard Screen
 * Main overview screen showing system metrics and statistics
 */
@Composable
fun DashboardScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = viewModel { DashboardViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    // LOGGING: Track state changes
    LaunchedEffect(uiState) {
        Napier.d("DASHBOARD: UI State changed to ${uiState::class.simpleName}")
        when (val state = uiState) {
            is DashboardUiState.Error -> {
                Napier.e("DASHBOARD: Error state - ${state.message}")
            }
            is DashboardUiState.Success -> {
                val stats = state.statistics
                Napier.i("DASHBOARD: Success state - Loaded statistics (Patients: ${stats.patients.total}, Analyses: ${stats.analyses.total})")
            }
            is DashboardUiState.Loading -> {
                Napier.d("DASHBOARD: Loading state - Fetching system statistics")
            }
        }
    }

    MainScaffold(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        onLogout = onLogout
    ) { paddingValues ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                LoadingContent(modifier = Modifier.padding(paddingValues))
            }
            is DashboardUiState.Success -> {
                DashboardContent(
                    statistics = state.statistics,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is DashboardUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = {
                        Napier.d("DASHBOARD: Retry button clicked after error")
                        viewModel.retry()
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DentalColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = DentalColors.Primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading Dashboard...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DentalColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = DentalColors.Error,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Error Loading Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DentalColors.OnBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DentalColors.Primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    statistics: SystemStatistics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DentalColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "System Overview",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = DentalColors.OnBackground
        )

        Text(
            text = "General dental analysis system summary",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Metrics Cards - Responsive Grid
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isSmallScreen = maxWidth < 600.dp

            if (isSmallScreen) {
                // Mobile layout: 2 columns grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Total Patients",
                            value = statistics.patients.total.toString(),
                            subtitle = "+${statistics.patients.new_this_month} this month",
                            change = "",
                            icon = ExtendedIcons.People,
                            iconColor = DentalColors.Primary,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Analysis This Month",
                            value = statistics.analyses.this_month.toString(),
                            subtitle = "Total: ${statistics.analyses.total}",
                            change = "",
                            icon = ExtendedIcons.Analytics,
                            iconColor = DentalColors.Success,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Scheduled Appointments",
                            value = statistics.appointments.scheduled.toString(),
                            subtitle = "Completed: ${statistics.appointments.completed}",
                            change = "",
                            icon = ExtendedIcons.CalendarToday,
                            iconColor = DentalColors.Warning,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Generated Reports",
                            value = statistics.reports.generated.toString(),
                            subtitle = "${statistics.reports.this_month} this month",
                            change = "",
                            icon = ExtendedIcons.Description,
                            iconColor = DentalColors.Secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Desktop layout: single row with 4 cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        title = "Total Patients",
                        value = statistics.patients.total.toString(),
                        subtitle = "+${statistics.patients.new_this_month} this month",
                        change = if (statistics.patients.new_this_month > 0) "+${calculatePercentageChange(statistics.patients.new_this_month, statistics.patients.total)}% vs previous month" else "",
                        icon = ExtendedIcons.People,
                        iconColor = DentalColors.Primary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Analysis This Month",
                        value = statistics.analyses.this_month.toString(),
                        subtitle = "Total: ${statistics.analyses.total}",
                        change = if (statistics.analyses.this_month > 0) "+${calculatePercentageChange(statistics.analyses.this_month, statistics.analyses.total)}% vs previous month" else "",
                        icon = ExtendedIcons.Analytics,
                        iconColor = DentalColors.Success,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Scheduled Appointments",
                        value = statistics.appointments.scheduled.toString(),
                        subtitle = "Completed: ${statistics.appointments.completed}",
                        change = "",
                        icon = ExtendedIcons.CalendarToday,
                        iconColor = DentalColors.Warning,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Generated Reports",
                        value = statistics.reports.generated.toString(),
                        subtitle = "${statistics.reports.this_month} this month",
                        change = "",
                        icon = ExtendedIcons.Description,
                        iconColor = DentalColors.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Charts Section - Responsive Layout
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isSmallScreen = maxWidth < 600.dp

            if (isSmallScreen) {
                // Mobile layout: stacked vertically
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Monthly Statistics Chart
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
                            Text(
                                text = "Monthly Statistics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Analysis and appointments per month",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DoubleBarChart(
                                data = statistics.monthlyTrend.map { monthData ->
                                    DoubleBarChartData(
                                        label = monthData.month,
                                        value1 = monthData.analyses.toFloat(),
                                        value2 = monthData.appointments.toFloat(),
                                        color1 = DentalColors.Primary,
                                        color2 = DentalColors.Success,
                                        label1 = "Analyses",
                                        label2 = "Appointments"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                showValues = true
                            )
                        }
                    }

                    // Dental Health Distribution
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
                            Text(
                                text = "Analysis and Trends",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Dental health distribution",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DonutChart(
                                data = listOf(
                                    PieChartData(
                                        label = "Healthy Teeth",
                                        value = 85f,
                                        color = DentalColors.ToothHealthy
                                    ),
                                    PieChartData(
                                        label = "Caries",
                                        value = 15f,
                                        color = DentalColors.ToothCaries
                                    )
                                ),
                                centerText = "85%",
                                strokeWidth = 40f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                // Desktop layout: side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Monthly Statistics Chart
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Monthly Statistics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Analysis and appointments per month",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DoubleBarChart(
                                data = statistics.monthlyTrend.map { monthData ->
                                    DoubleBarChartData(
                                        label = monthData.month,
                                        value1 = monthData.analyses.toFloat(),
                                        value2 = monthData.appointments.toFloat(),
                                        color1 = DentalColors.Primary,
                                        color2 = DentalColors.Success,
                                        label1 = "Analyses",
                                        label2 = "Appointments"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                showValues = true
                            )
                        }
                    }

                    // Dental Health Distribution
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Analysis and Trends",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Dental health distribution",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DonutChart(
                                data = listOf(
                                    PieChartData(
                                        label = "Healthy Teeth",
                                        value = 85f,
                                        color = DentalColors.ToothHealthy
                                    ),
                                    PieChartData(
                                        label = "Caries",
                                        value = 15f,
                                        color = DentalColors.ToothCaries
                                    )
                                ),
                                centerText = "85%",
                                strokeWidth = 40f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Metric Card Component
 * Displays a single metric with icon and change indicator
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    change: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DentalColors.OnBackground
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (change.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = change,
                    style = MaterialTheme.typography.labelSmall,
                    color = DentalColors.Success,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Calculate percentage change for metrics
 */
private fun calculatePercentageChange(newValue: Int, total: Int): Int {
    if (total <= newValue) return 0
    val previousTotal = total - newValue
    if (previousTotal == 0) return 0
    return ((newValue.toFloat() / previousTotal.toFloat()) * 100).toInt()
}
