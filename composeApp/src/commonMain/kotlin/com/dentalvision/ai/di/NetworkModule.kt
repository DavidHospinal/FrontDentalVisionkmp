package com.dentalvision.ai.di

import com.dentalvision.ai.data.remote.HttpClientFactory
import com.dentalvision.ai.data.remote.api.ApiClient
import com.dentalvision.ai.data.remote.api.ApiClientFactory
import com.dentalvision.ai.data.remote.api.ApiConfig
import com.dentalvision.ai.data.remote.gemini.GeminiApiClient
import com.dentalvision.ai.data.remote.gemini.GeminiConfig
import com.dentalvision.ai.data.remote.gradio.GradioApiClient
import com.dentalvision.ai.data.remote.service.AnalysisService
import com.dentalvision.ai.data.remote.service.AppointmentService
import com.dentalvision.ai.data.remote.service.PatientService
import com.dentalvision.ai.data.remote.service.ReportService
import com.dentalvision.ai.data.remote.service.SystemService
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for Networking layer
 * Provides HTTP clients and API services
 */
val networkModule = module {
    // API Clients (Backend and HuggingFace)
    single(named("backendClient")) {
        ApiClientFactory.backendClient
    }

    single(named("huggingFaceClient")) {
        ApiClientFactory.huggingFaceClient
    }

    // Gradio API Client for YOLOv12 analysis
    single {
        GradioApiClient(
            baseUrl = ApiConfig.HUGGINGFACE_URL,
            timeout = 60_000L
        )
    }

    // Gemini API Client for Clinical Insights
    single {
        GeminiApiClient(
            httpClient = HttpClientFactory.createGenericHttpClient(),
            apiKey = GeminiConfig.getApiKey()
        )
    }

    // API Services
    single {
        PatientService(
            apiClient = get(named("backendClient"))
        )
    }

    single {
        AnalysisService(
            backendClient = get(named("backendClient")),
            huggingFaceClient = get(named("huggingFaceClient"))
        )
    }

    single {
        ReportService(
            apiClient = get(named("backendClient"))
        )
    }

    single {
        SystemService(
            apiClient = get(named("backendClient"))
        )
    }

    single {
        AppointmentService(
            apiClient = get(named("backendClient"))
        )
    }
}
