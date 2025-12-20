package com.dentalvision.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dentalvision.ai.presentation.screen.login.LoginScreen
import com.dentalvision.ai.presentation.screen.dashboard.DashboardScreen
import com.dentalvision.ai.presentation.screen.appointments.AppointmentsScreen
import com.dentalvision.ai.presentation.screen.analysis.NewAnalysisScreen
import com.dentalvision.ai.presentation.screen.reports.ReportsScreen

/**
 * Main navigation graph for Dental Vision AI application
 * Configures all routes and navigation logic using AndroidX Navigation Compose
 *
 * @param navController Navigation controller managing the back stack
 * @param startDestination Initial route to display (default: Login)
 */
@Composable
fun DentalVisionNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { email ->
                    navController.navigate(Screen.Dashboard.route) {
                        // Clear login from back stack after successful login
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard Screen
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                currentRoute = Screen.Dashboard.route,
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Appointments Screen
        composable(route = Screen.Appointments.route) {
            AppointmentsScreen(
                currentRoute = Screen.Appointments.route,
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Reports Screen
        composable(route = Screen.Reports.route) {
            ReportsScreen(
                currentRoute = Screen.Reports.route,
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Patient List Screen
        composable(route = Screen.PatientList.route) {
            PatientListScreenPlaceholder(
                onPatientClick = { patientId ->
                    navController.navigate(Screen.PatientDetail.createRoute(patientId))
                },
                onCreatePatient = {
                    navController.navigate(Screen.CreatePatient.route)
                },
                onNewAnalysis = { patientId ->
                    navController.navigate(Screen.NewAnalysis.createRoute(patientId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Patient Detail Screen
        composable(
            route = Screen.PatientDetail.route,
            arguments = listOf(
                navArgument(Screen.PatientDetail.ARG_PATIENT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString(Screen.PatientDetail.ARG_PATIENT_ID)
                ?: return@composable

            PatientDetailScreenPlaceholder(
                patientId = patientId,
                onBack = {
                    navController.popBackStack()
                },
                onEdit = {
                    navController.navigate(Screen.EditPatient.createRoute(patientId))
                },
                onNewAnalysis = {
                    navController.navigate(Screen.NewAnalysis.createRoute(patientId))
                },
                onViewAnalyses = {
                    navController.navigate(Screen.AnalysisList.createRoute(patientId))
                }
            )
        }

        // Create Patient Screen
        composable(route = Screen.CreatePatient.route) {
            CreatePatientScreenPlaceholder(
                onPatientCreated = { patientId ->
                    // Navigate to patient detail and clear create screen
                    navController.navigate(Screen.PatientDetail.createRoute(patientId)) {
                        popUpTo(Screen.PatientList.route)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Edit Patient Screen
        composable(
            route = Screen.EditPatient.route,
            arguments = listOf(
                navArgument(Screen.EditPatient.ARG_PATIENT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString(Screen.EditPatient.ARG_PATIENT_ID)
                ?: return@composable

            EditPatientScreenPlaceholder(
                patientId = patientId,
                onPatientUpdated = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // New Analysis Screen
        composable(
            route = Screen.NewAnalysis.route,
            arguments = listOf(
                navArgument(Screen.NewAnalysis.ARG_PATIENT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString(Screen.NewAnalysis.ARG_PATIENT_ID)
                ?: return@composable

            NewAnalysisScreen(
                currentRoute = "analysis/new/$patientId",
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Analysis Result Screen
        composable(
            route = Screen.AnalysisResult.route,
            arguments = listOf(
                navArgument(Screen.AnalysisResult.ARG_ANALYSIS_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val analysisId = backStackEntry.arguments?.getString(Screen.AnalysisResult.ARG_ANALYSIS_ID)
                ?: return@composable

            AnalysisResultScreenPlaceholder(
                analysisId = analysisId,
                onGenerateReport = { reportId ->
                    navController.navigate(Screen.ReportDetail.createRoute(reportId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Analysis List Screen
        composable(
            route = Screen.AnalysisList.route,
            arguments = listOf(
                navArgument(Screen.AnalysisList.ARG_PATIENT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString(Screen.AnalysisList.ARG_PATIENT_ID)
                ?: return@composable

            AnalysisListScreenPlaceholder(
                patientId = patientId,
                onAnalysisClick = { analysisId ->
                    navController.navigate(Screen.AnalysisResult.createRoute(analysisId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Report Detail Screen
        composable(
            route = Screen.ReportDetail.route,
            arguments = listOf(
                navArgument(Screen.ReportDetail.ARG_REPORT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString(Screen.ReportDetail.ARG_REPORT_ID)
                ?: return@composable

            ReportDetailScreenPlaceholder(
                reportId = reportId,
                onBack = {
                    navController.popBackStack()
                },
                onShare = {
                    // TODO: Implement platform-specific share functionality
                }
            )
        }

        // Settings Screen
        composable(route = Screen.Settings.route) {
            SettingsScreenPlaceholder(
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // About Screen
        composable(route = Screen.About.route) {
            AboutScreenPlaceholder(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// Placeholder composables - Replace with actual screen implementations
// These allow the navigation graph to compile before all screens are implemented

@Composable
private fun LoginScreenPlaceholder(onLoginSuccess: () -> Unit) {
    // TODO: Implement actual LoginScreen from presentation.screen.login package
}

@Composable
private fun DashboardScreenPlaceholder(
    onNavigateToPatients: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // TODO: Implement actual DashboardScreen from presentation.screen.dashboard package
}

@Composable
private fun PatientListScreenPlaceholder(
    onPatientClick: (String) -> Unit,
    onCreatePatient: () -> Unit,
    onNewAnalysis: (String) -> Unit,
    onBack: () -> Unit
) {
    // TODO: Implement actual PatientListScreen from presentation.screen.patient package
}

@Composable
private fun PatientDetailScreenPlaceholder(
    patientId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onNewAnalysis: () -> Unit,
    onViewAnalyses: () -> Unit
) {
    // TODO: Implement actual PatientDetailScreen from presentation.screen.patient package
}

@Composable
private fun CreatePatientScreenPlaceholder(
    onPatientCreated: (String) -> Unit,
    onBack: () -> Unit
) {
    // TODO: Implement actual CreatePatientScreen from presentation.screen.patient package
}

@Composable
private fun EditPatientScreenPlaceholder(
    patientId: String,
    onPatientUpdated: () -> Unit,
    onBack: () -> Unit
) {
    // TODO: Implement actual EditPatientScreen from presentation.screen.patient package
}

@Composable
private fun NewAnalysisScreenPlaceholder(
    patientId: String,
    onAnalysisComplete: (String) -> Unit,
    onBack: () -> Unit
) {
    // TODO: Implement actual NewAnalysisScreen from presentation.screen.analysis package
}

@Composable
private fun AnalysisResultScreenPlaceholder(
    analysisId: String,
    onGenerateReport: (String) -> Unit,
    onBack: () -> Unit
) {
    // TODO: Implement actual AnalysisResultScreen from presentation.screen.analysis package
}

@Composable
private fun AnalysisListScreenPlaceholder(
    patientId: String,
    onAnalysisClick: (String) -> Unit,
    onBack: () -> Unit
) {
    // TODO: Implement actual AnalysisListScreen from presentation.screen.analysis package
}

@Composable
private fun ReportDetailScreenPlaceholder(
    reportId: String,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    // TODO: Implement actual ReportDetailScreen from presentation.screen.report package
}

@Composable
private fun SettingsScreenPlaceholder(
    onNavigateToAbout: () -> Unit,
    onBack: () -> Unit
) {
    // TODO: Implement actual SettingsScreen from presentation.screen.settings package
}

@Composable
private fun AboutScreenPlaceholder(onBack: () -> Unit) {
    // TODO: Implement actual AboutScreen from presentation.screen.settings package
}
