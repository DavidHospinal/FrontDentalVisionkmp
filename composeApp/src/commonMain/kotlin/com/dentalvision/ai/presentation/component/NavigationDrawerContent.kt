package com.dentalvision.ai.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dentalvision.ai.presentation.navigation.Screen
import com.dentalvision.ai.presentation.theme.DentalColors
import com.dentalvision.ai.presentation.component.ExtendedIcons

/**
 * Navigation Drawer Content
 * Dark sidebar with navigation menu items
 */
@Composable
fun NavigationDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(260.dp),
        drawerContainerColor = Color(0xFF2C3E50),
        drawerContentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Logo/Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DentalColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DV",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Dental Vision AI",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Professional Dental Analysis",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Items
            NavigationDrawerItem(
                label = "Dashboard",
                icon = ExtendedIcons.Dashboard,
                selected = currentRoute == Screen.Dashboard.route,
                onClick = { onNavigate(Screen.Dashboard.route) }
            )

            NavigationDrawerItem(
                label = "Patients",
                icon = ExtendedIcons.People,
                selected = currentRoute.startsWith("patients"),
                onClick = { onNavigate(Screen.PatientList.route) }
            )

            NavigationDrawerItem(
                label = "Appointments",
                icon = ExtendedIcons.CalendarToday,
                selected = currentRoute.startsWith("appointments"),
                onClick = { onNavigate("appointments") } // TODO: Add appointments route
            )

            NavigationDrawerItem(
                label = "New Analysis",
                icon = ExtendedIcons.Analytics,
                selected = currentRoute.startsWith("analysis/new"),
                onClick = {
                    // Note: New analysis requires patient selection first
                    onNavigate(Screen.PatientList.route)
                }
            )

            NavigationDrawerItem(
                label = "Reports",
                icon = ExtendedIcons.Description,
                selected = currentRoute.startsWith("reports"),
                onClick = { onNavigate("reports") } // TODO: Add reports route
            )

            Spacer(modifier = Modifier.weight(1f))

            // Current Patient Info (if any)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF34495E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Current Patient",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No patient selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Navigation Drawer Item
 * Custom styled navigation item for the drawer
 */
@Composable
private fun NavigationDrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = DentalColors.Primary.copy(alpha = 0.9f),
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = Color.White.copy(alpha = 0.7f),
            unselectedTextColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(8.dp)
    )
}
