package com.dentalvision.ai.data.remote.gemini

import com.dentalvision.ai.data.remote.api.Secrets

/**
 * Gemini API Configuration
 *
 * SECURITY:
 * - API key is stored in Secrets.kt (excluded from version control)
 * - To setup: rename Secrets.sample.kt to Secrets.kt and insert your key
 * - Get a free key at: https://aistudio.google.com/app/apikey
 */
object GeminiConfig {
    fun getApiKey(): String {
        return Secrets.GEMINI_API_KEY
    }
}
