package com.dentalvision.ai.data.remote

import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Factory object for creating configured Ktor HttpClient instances
 * Provides separate clients for backend API and HuggingFace API
 */
object HttpClientFactory {

    /**
     * JSON configuration for serialization/deserialization
     */
    private val jsonConfig = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Creates HTTP client for Dental Vision Backend API
     * Base URL: https://backenddental-vision-ai.onrender.com
     *
     * Features:
     * - JWT authentication
     * - Content negotiation (JSON)
     * - Request/response logging
     * - Timeout configuration
     */
    fun createBackendClient(
        baseUrl: String = "https://backenddental-vision-ai.onrender.com",
        tokenProvider: () -> String? = { null }
    ): HttpClient {
        return HttpClient {
            // Base URL configuration
            defaultRequest {
                url(baseUrl)
            }

            // JSON content negotiation
            install(ContentNegotiation) {
                json(jsonConfig)
            }

            // Authentication (JWT Bearer Token)
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = tokenProvider()
                        if (token != null) {
                            BearerTokens(accessToken = token, refreshToken = "")
                        } else {
                            null
                        }
                    }
                }
            }

            // Logging
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d(message, tag = "HttpClient")
                    }
                }
                level = LogLevel.INFO
            }

            // Timeout configuration
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000      // 60 seconds
                connectTimeoutMillis = 30_000      // 30 seconds
                socketTimeoutMillis = 60_000       // 60 seconds
            }

            // Default request configuration
            install(DefaultRequest) {
                headers.append("Accept", "application/json")
                headers.append("Content-Type", "application/json")
                // Request uncompressed responses for WASM compatibility
                headers.append("Accept-Encoding", "identity")
            }
        }
    }

    /**
     * Creates HTTP client for HuggingFace Spaces API
     * Used for YOLOv12 dental caries detection
     *
     * Features:
     * - Extended timeout for ML inference
     * - Content negotiation
     * - Logging
     */
    fun createHuggingFaceClient(
        baseUrl: String = "https://huggingface.co"
    ): HttpClient {
        return HttpClient {
            // Base URL configuration
            defaultRequest {
                url(baseUrl)
            }

            // JSON content negotiation
            install(ContentNegotiation) {
                json(jsonConfig)
            }

            // Logging
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d(message, tag = "HuggingFace")
                    }
                }
                level = LogLevel.INFO
            }

            // Extended timeout for ML inference
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000     // 120 seconds (ML takes time)
                connectTimeoutMillis = 30_000      // 30 seconds
                socketTimeoutMillis = 120_000      // 120 seconds
            }

            // Default request configuration
            install(DefaultRequest) {
                headers.append("Accept", "application/json")
                // Request uncompressed responses for WASM compatibility
                headers.append("Accept-Encoding", "identity")
            }
        }
    }
}
