package com.dentalvision.ai.presentation.navigation

/**
 * Sealed class defining all navigation routes in the Dental Vision AI app
 * Provides type-safe navigation with compile-time route validation
 */
sealed class Screen(val route: String) {

    /**
     * Login screen - Entry point for unauthenticated users
     */
    data object Login : Screen("login")

    /**
     * Dashboard screen - Main overview after login
     */
    data object Dashboard : Screen("dashboard")

    /**
     * Patient list screen - Shows all patients
     */
    data object PatientList : Screen("patients")

    /**
     * Patient detail screen - Shows individual patient information
     * @param patientId The unique identifier for the patient
     */
    data object PatientDetail : Screen("patients/{patientId}") {
        fun createRoute(patientId: String) = "patients/$patientId"

        const val ARG_PATIENT_ID = "patientId"
    }

    /**
     * Create new patient screen
     */
    data object CreatePatient : Screen("patients/new")

    /**
     * Edit existing patient screen
     * @param patientId The unique identifier for the patient to edit
     */
    data object EditPatient : Screen("patients/{patientId}/edit") {
        fun createRoute(patientId: String) = "patients/$patientId/edit"

        const val ARG_PATIENT_ID = "patientId"
    }

    /**
     * New analysis screen - Create analysis (standalone, patient selected inside)
     * Route changed from "analysis/new" to "new-analysis" to avoid conflict with "analysis/new/{patientId}"
     */
    data object NewAnalysisStandalone : Screen("new-analysis")

    /**
     * New analysis screen - Create analysis for a specific patient
     * @param patientId The unique identifier for the patient
     */
    data object NewAnalysis : Screen("analysis/new/{patientId}") {
        fun createRoute(patientId: String) = "analysis/new/$patientId"

        const val ARG_PATIENT_ID = "patientId"
    }

    /**
     * Analysis result screen - View analysis details
     * @param analysisId The unique identifier for the analysis
     */
    data object AnalysisResult : Screen("analysis/{analysisId}") {
        fun createRoute(analysisId: String) = "analysis/$analysisId"

        const val ARG_ANALYSIS_ID = "analysisId"
    }

    /**
     * Analysis list screen - View all analyses for a patient
     * @param patientId The unique identifier for the patient
     */
    data object AnalysisList : Screen("patients/{patientId}/analyses") {
        fun createRoute(patientId: String) = "patients/$patientId/analyses"

        const val ARG_PATIENT_ID = "patientId"
    }

    /**
     * Appointments screen - Calendar view for managing appointments
     */
    data object Appointments : Screen("appointments")

    /**
     * Reports list screen - View all generated reports
     */
    data object Reports : Screen("reports")

    /**
     * Report detail screen - View generated report
     * @param reportId The unique identifier for the report
     */
    data object ReportDetail : Screen("reports/{reportId}") {
        fun createRoute(reportId: String) = "reports/$reportId"

        const val ARG_REPORT_ID = "reportId"
    }

    /**
     * Settings screen - Application configuration
     */
    data object Settings : Screen("settings")

    /**
     * About screen - App information and credits
     */
    data object About : Screen("about")
}
