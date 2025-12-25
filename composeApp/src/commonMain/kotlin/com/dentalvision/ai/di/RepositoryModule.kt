package com.dentalvision.ai.di

import com.dentalvision.ai.data.repository.AnalysisRepositoryImpl
import com.dentalvision.ai.data.repository.AppointmentRepositoryImpl
import com.dentalvision.ai.data.repository.PatientRepositoryImpl
import com.dentalvision.ai.data.repository.ReportRepositoryImpl
import com.dentalvision.ai.domain.repository.AnalysisRepository
import com.dentalvision.ai.domain.repository.AppointmentRepository
import com.dentalvision.ai.domain.repository.PatientRepository
import com.dentalvision.ai.domain.repository.ReportRepository
import org.koin.dsl.module

/**
 * Koin module for Repository layer
 * Binds repository interfaces to implementations
 */
val repositoryModule = module {
    // Patient Repository
    single<PatientRepository> {
        PatientRepositoryImpl(
            patientService = get()
        )
    }

    // Analysis Repository
    single<AnalysisRepository> {
        AnalysisRepositoryImpl(
            backendClient = get(org.koin.core.qualifier.named("backendClient")),
            gradioClient = get()
        )
    }

    // Report Repository
    single<ReportRepository> {
        ReportRepositoryImpl(
            reportService = get()
        )
    }

    // Appointment Repository
    single<AppointmentRepository> {
        AppointmentRepositoryImpl(
            patientService = get()
        )
    }
}
