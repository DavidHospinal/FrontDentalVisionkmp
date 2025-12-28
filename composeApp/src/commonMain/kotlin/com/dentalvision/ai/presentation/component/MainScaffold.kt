package com.dentalvision.ai.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dentalvision.ai.presentation.navigation.Screen
import kotlinx.coroutines.launch
import io.github.aakira.napier.Napier

/**
 * Main Scaffold Layout
 * Provides consistent layout with navigation drawer and top bar across authenticated screens
 */
@Composable
fun MainScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    Napier.d("MAIN SCAFFOLD: Navigation requested to route: $route")
                    Napier.d("MAIN SCAFFOLD: Calling parent onNavigate callback")
                    onNavigate(route)
                    Napier.d("MAIN SCAFFOLD: Closing drawer")
                    scope.launch {
                        drawerState.close()
                    }
                },
                onLogout = onLogout
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onLogout = onLogout
                )
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}
