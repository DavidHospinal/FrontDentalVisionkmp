package com.dentalvision.ai.data.remote.gemini

object GeminiConfig {
    // SECURITY: This key should be replaced with environment variable in production
    // For local development only - DO NOT commit real keys
    const val API_KEY_PLACEHOLDER = "YOUR_GEMINI_API_KEY_HERE"

    // In production, use platform-specific environment variable loading
    // Android: BuildConfig.GEMINI_API_KEY
    // Desktop: System.getenv("GEMINI_API_KEY")
    // Web: process.env.GEMINI_API_KEY or similar

    fun getApiKey(): String {
        // TEMPORARY: Replace with actual key for testing
        // TODO: Implement secure key management per platform
        return "AIzaSyCx5G1rYWQuAmuN769KjNNnMHyB6YMmN9s"
    }
}
