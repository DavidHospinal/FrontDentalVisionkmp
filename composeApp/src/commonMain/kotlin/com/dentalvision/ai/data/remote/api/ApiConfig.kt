package com.dentalvision.ai.data.remote.api

object ApiConfig {
    const val BACKEND_URL = "https://backenddental-vision-ai.onrender.com"
    const val HUGGINGFACE_URL = "https://davidhosp-dental-vision-yolo12.hf.space"

    object Endpoints {
        const val PATIENTS = "/api/v1/patients"
        const val APPOINTMENTS = "/api/v1/patients/{id}/appointments"
        const val REPORTS = "/api/v1/reports"
        const val SYSTEM_STATS = "/api/v1/system/statistics"
        const val ANALYSIS = "/api/v1/analysis"
    }

    object Headers {
        const val CONTENT_TYPE_JSON = "application/json"
        const val AUTHORIZATION = "Authorization"
        const val BEARER = "Bearer"
    }
}
