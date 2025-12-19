package com.dentalvision.ai.data.remote.service

import com.dentalvision.ai.data.remote.api.ApiClient
import com.dentalvision.ai.data.remote.api.dto.PatientDTO
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
 * Unit tests for PatientService
 * Tests CRUD operations for patient management
 */
class PatientServiceTest {

    private lateinit var mockEngine: MockEngine
    private lateinit var httpClient: HttpClient
    private lateinit var apiClient: ApiClient
    private lateinit var patientService: PatientService

    @BeforeTest
    fun setup() {
        mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/v1/patients" -> {
                    when (request.method) {
                        HttpMethod.Get -> {
                            respond(
                                content = ByteReadChannel("""
                                    {
                                        "success": true,
                                        "data": [
                                            {
                                                "id": "1",
                                                "name": "Test Patient",
                                                "age": 30,
                                                "gender": "MALE",
                                                "email": "test@example.com",
                                                "phone": "+1234567890",
                                                "created_at": "2025-01-01T00:00:00Z",
                                                "updated_at": "2025-01-01T00:00:00Z"
                                            }
                                        ],
                                        "message": "Success"
                                    }
                                """.trimIndent()),
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }
                        HttpMethod.Post -> {
                            respond(
                                content = ByteReadChannel("""
                                    {
                                        "success": true,
                                        "data": {
                                            "id": "2",
                                            "name": "New Patient",
                                            "age": 25,
                                            "gender": "FEMALE",
                                            "email": "new@example.com",
                                            "phone": "+0987654321",
                                            "created_at": "2025-01-02T00:00:00Z",
                                            "updated_at": "2025-01-02T00:00:00Z"
                                        },
                                        "message": "Patient created successfully"
                                    }
                                """.trimIndent()),
                                status = HttpStatusCode.Created,
                                headers = headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }
                        else -> error("Unhandled ${request.method}")
                    }
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }

        httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }
        }

        apiClient = ApiClient(httpClient)
        patientService = PatientService(apiClient)
    }

    @AfterTest
    fun tearDown() {
        httpClient.close()
    }

    @Test
    fun testGetAllPatientsSuccess() = runTest {
        val response = patientService.getAllPatients()

        assertTrue(response.success)
        assertEquals(1, response.data?.size)
        assertEquals("Test Patient", response.data?.first()?.name)
        assertEquals(30, response.data?.first()?.age)
    }

    @Test
    fun testCreatePatientSuccess() = runTest {
        val newPatient = PatientDTO(
            id = "",
            name = "New Patient",
            age = 25,
            gender = "FEMALE",
            email = "new@example.com",
            phone = "+0987654321",
            createdAt = "2025-01-02T00:00:00Z",
            updatedAt = "2025-01-02T00:00:00Z"
        )

        val response = patientService.createPatient(newPatient)

        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals("New Patient", response.data?.name)
        assertEquals(25, response.data?.age)
        assertEquals("FEMALE", response.data?.gender)
    }

    @Test
    fun testPatientDTOSerialization() {
        val patient = PatientDTO(
            id = "test-id",
            name = "John Doe",
            age = 35,
            gender = "MALE",
            email = "john@example.com",
            phone = "+1234567890",
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z"
        )

        assertNotNull(patient.id)
        assertEquals("John Doe", patient.name)
        assertEquals(35, patient.age)
        assertEquals("MALE", patient.gender)
    }
}
