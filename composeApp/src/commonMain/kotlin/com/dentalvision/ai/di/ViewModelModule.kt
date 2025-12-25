package com.dentalvision.ai.di

import com.dentalvision.ai.presentation.viewmodel.AppointmentsViewModel
import com.dentalvision.ai.presentation.viewmodel.NewAnalysisViewModel
import com.dentalvision.ai.presentation.viewmodel.PatientsViewModel
import com.dentalvision.ai.presentation.viewmodel.ReportsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels
 * Registers all presentation layer ViewModels
 */
val viewModelModule = module {
    // Patients ViewModel
    viewModel {
        PatientsViewModel(
            patientRepository = get()
        )
    }

    // New Analysis ViewModel
    viewModel {
        NewAnalysisViewModel(
            analysisRepository = get(),
            filePicker = get()
        )
    }

    // Reports ViewModel
    viewModel {
        ReportsViewModel(
            reportRepository = get()
        )
    }

    // Appointments ViewModel
    viewModel {
        AppointmentsViewModel(
            appointmentRepository = get()
        )
    }
}
