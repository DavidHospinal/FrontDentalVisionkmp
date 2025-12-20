package com.dentalvision.ai.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Extension functions for type-safe navigation
 * Provides convenient methods for common navigation patterns
 */

/**
 * Navigate to patient detail screen
 */
fun NavController.navigateToPatientDetail(patientId: String) {
    navigate(Screen.PatientDetail.createRoute(patientId))
}

/**
 * Navigate to new analysis screen for a patient
 */
fun NavController.navigateToNewAnalysis(patientId: String) {
    navigate(Screen.NewAnalysis.createRoute(patientId))
}

/**
 * Navigate to analysis result screen
 */
fun NavController.navigateToAnalysisResult(analysisId: String) {
    navigate(Screen.AnalysisResult.createRoute(analysisId))
}

/**
 * Navigate to report detail screen
 */
fun NavController.navigateToReportDetail(reportId: String) {
    navigate(Screen.ReportDetail.createRoute(reportId))
}

/**
 * Navigate to patient list and clear back stack
 */
fun NavController.navigateToPatientListAndClearBackStack() {
    navigate(Screen.PatientList.route) {
        popUpTo(0) { inclusive = true }
    }
}

/**
 * Navigate to dashboard and clear back stack
 */
fun NavController.navigateToDashboardAndClearBackStack() {
    navigate(Screen.Dashboard.route) {
        popUpTo(0) { inclusive = true }
    }
}

/**
 * Navigate to login and clear back stack (logout)
 */
fun NavController.navigateToLoginAndClearBackStack() {
    navigate(Screen.Login.route) {
        popUpTo(0) { inclusive = true }
    }
}

/**
 * Navigate with single top launch mode
 * Prevents duplicate screens in back stack
 */
fun NavController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

/**
 * Navigate and pop up to a specific route
 */
fun NavController.navigateAndPopUpTo(
    route: String,
    popUpToRoute: String,
    inclusive: Boolean = false
) {
    navigate(route) {
        popUpTo(popUpToRoute) {
            this.inclusive = inclusive
        }
    }
}

/**
 * Safe navigation - only navigate if not already on the destination
 */
fun NavController.safeNavigate(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    if (currentBackStackEntry?.destination?.route != route) {
        navigate(route, builder)
    }
}
