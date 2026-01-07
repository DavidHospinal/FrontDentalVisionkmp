package com.dentalvision.ai.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dentalvision.ai.presentation.navigation.Screen
import com.dentalvision.ai.presentation.theme.DentalColors
import dentalvisionai.composeapp.generated.resources.Res
import dentalvisionai.composeapp.generated.resources.dental0
import org.jetbrains.compose.resources.painterResource
import io.github.aakira.napier.Napier

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
                Image(
                    painter = painterResource(Res.drawable.dental0),
                    contentDescription = "Dental Vision AI Logo",
                    modifier = Modifier.size(64.dp)
                )

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
                icon = Icons.Filled.Home,
                selected = currentRoute == Screen.Dashboard.route,
                onClick = { onNavigate(Screen.Dashboard.route) }
            )

            NavigationDrawerItem(
                label = "Patients",
                icon = Icons.Filled.Person,
                selected = currentRoute.startsWith("patients"),
                onClick = { onNavigate(Screen.PatientList.route) }
            )

            NavigationDrawerItem(
                label = "Appointments",
                icon = Icons.Filled.DateRange,
                selected = currentRoute.startsWith("appointments"),
                onClick = { onNavigate("appointments") }
            )

            NavigationDrawerItem(
                label = "New Analysis",
                icon = Icons.Filled.Add,
                selected = currentRoute.startsWith("analysis/new"),
                onClick = {
                    Napier.d("NAVIGATION DRAWER: 'New Analysis' menu item clicked")
                    Napier.d("NAVIGATION DRAWER: Navigating to route: ${Screen.NewAnalysisStandalone.route}")
                    onNavigate(Screen.NewAnalysisStandalone.route)
                }
            )

            NavigationDrawerItem(
                label = "Reports",
                icon = Icons.AutoMirrored.Filled.List,
                selected = currentRoute.startsWith("reports"),
                onClick = { onNavigate("reports") }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.White.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            NavigationDrawerItem(
                label = {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout"
                    )
                },
                selected = false,
                onClick = onLogout,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color(0xFFE74C3C).copy(alpha = 0.9f),
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedContainerColor = Color(0xFFE74C3C).copy(alpha = 0.1f),
                    unselectedIconColor = Color(0xFFE74C3C),
                    unselectedTextColor = Color(0xFFE74C3C)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Doctor Info
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
                        text = "Welcome,",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dr. David",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Navigation Drawer Item
 * Custom styled navigation item with state animations
 *
 * Features:
 * - Scale animation: 1.0f to 1.15f when selected
 * - Smooth transitions with 300ms tween
 * - Primary color background with 0.1 alpha when selected
 */
@Composable
private fun NavigationDrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "nav_item_scale"
    )

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
                contentDescription = label,
                modifier = Modifier.scale(scale)
            )
        },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = DentalColors.Primary.copy(alpha = 0.1f),
            selectedIconColor = DentalColors.Primary,
            selectedTextColor = Color.White,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = Color.White.copy(alpha = 0.7f),
            unselectedTextColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(8.dp)
    )
}
