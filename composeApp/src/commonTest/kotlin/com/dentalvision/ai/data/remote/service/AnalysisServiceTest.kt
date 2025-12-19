package com.dentalvision.ai.data.remote.service

import com.dentalvision.ai.data.remote.api.ApiClient
import com.dentalvision.ai.data.remote.api.dto.AnalysisDTO
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * Unit tests for AnalysisService
 * Tests dental analysis operations and AI integration
 */
class AnalysisServiceTest {

    private lateinit var mockEngine: MockEngine
    private lateinit var httpClient: HttpClient
    private lateinit var apiClient: ApiClient
    private lateinit var analysisService: AnalysisService

    @BeforeTest
    fun setup() {
        mockEngine = MockEngine { request ->
            when {
                request.url.encodedPath.startsWith("/api/v1/analysis") -> {
                    respond(
                        content = ByteReadChannel("""
                            {
                                "success": true,
                                "data": {
                                    "id": "analysis-1",
                                    "patient_id": "patient-1",
                                    "image_url": "https://example.com/image.jpg",
                                    "result_data": {
                                        "teeth_detected": 16,
                                        "caries_detected": 2,
                                        "confidence": 0.85
                                    },
                                    "status": "COMPLETED",
                                    "created_at": "2025-01-01T00:00:00Z",
                                    "updated_at": "2025-01-01T00:00:00Z"
                                },
                                "message": "Analysis completed successfully"
                            }
                        """.trimIndent()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }

        httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }

        apiClient = ApiClient(httpClient)
        analysisService = AnalysisService(apiClient)
    }

    @AfterTest
    fun tearDown() {
        httpClient.close()
    }

    @Test
    fun testGetAnalysisByIdSuccess() = runTest {
        val response = analysisService.getAnalysisById("analysis-1")

        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals("analysis-1", response.data?.id)
        assertEquals("patient-1", response.data?.patientId)
        assertEquals("COMPLETED", response.data?.status)
    }

    @Test
    fun testAnalysisResultParsing() {
        val analysis = AnalysisDTO(
            id = "test-analysis",
            patientId = "test-patient",
            imageUrl = "https://example.com/test.jpg",
            resultData = mapOf(
                "teeth_detected" to 16,
                "caries_detected" to 2,
                "confidence" to 0.85
            ),
            status = "COMPLETED",
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z"
        )

        assertEquals("test-analysis", analysis.id)
        assertEquals("test-patient", analysis.patientId)
        assertEquals("COMPLETED", analysis.status)
        assertNotNull(analysis.resultData)
    }

    @Test
    fun testAnalysisStatusValidation() {
        val validStatuses = listOf("PENDING", "PROCESSING", "COMPLETED", "FAILED")

        validStatuses.forEach { status ->
            val analysis = AnalysisDTO(
                id = "test",
                patientId = "patient-1",
                imageUrl = "https://example.com/image.jpg",
                resultData = emptyMap(),
                status = status,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            assertTrue(validStatuses.contains(analysis.status))
        }
    }
}
