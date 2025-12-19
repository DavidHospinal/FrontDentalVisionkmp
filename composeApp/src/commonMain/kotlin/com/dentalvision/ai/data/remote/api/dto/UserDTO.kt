package com.dentalvision.ai.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val created_at: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val user: UserDTO
)
