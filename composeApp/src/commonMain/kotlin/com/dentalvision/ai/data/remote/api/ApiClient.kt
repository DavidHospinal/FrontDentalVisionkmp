package com.dentalvision.ai.data.remote.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient(
    val baseUrl: String,
    val additionalHeaders: Map<String, String> = emptyMap()
) {
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }

        defaultRequest {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            additionalHeaders.forEach { (key, value) ->
                header(key, value)
            }
        }
    }

    suspend inline fun <reified T> get(endpoint: String): T {
        return httpClient.get("$baseUrl$endpoint").body()
    }

    suspend inline fun <reified T> post(
        endpoint: String,
        body: Any? = null
    ): T {
        return httpClient.post("$baseUrl$endpoint") {
            contentType(ContentType.Application.Json)
            if (body != null) {
                setBody(body)
            }
        }.body()
    }

    suspend inline fun <reified T> put(
        endpoint: String,
        body: Any? = null
    ): T {
        return httpClient.put("$baseUrl$endpoint") {
            contentType(ContentType.Application.Json)
            if (body != null) {
                setBody(body)
            }
        }.body()
    }

    suspend inline fun <reified T> delete(endpoint: String): T {
        return httpClient.delete("$baseUrl$endpoint").body()
    }

    fun close() {
        httpClient.close()
    }
}

object ApiClientFactory {
    val backendClient: ApiClient by lazy {
        ApiClient(ApiConfig.BACKEND_URL)
    }

    val huggingFaceClient: ApiClient by lazy {
        ApiClient(
            ApiConfig.HUGGINGFACE_URL,
            mapOf(
                ApiConfig.Headers.AUTHORIZATION to "${ApiConfig.Headers.BEARER} ${getHuggingFaceToken()}"
            )
        )
    }

    private fun getHuggingFaceToken(): String {
        return "ENV_VAR_PLACEHOLDER"
    }
}
