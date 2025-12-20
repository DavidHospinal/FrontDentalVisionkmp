package com.dentalvision.ai.domain.repository

import com.dentalvision.ai.data.remote.api.dto.UserDTO

/**
 * Repository interface for Authentication operations
 * Defines contract for user authentication and token management
 */
interface AuthRepository {

    /**
     * Login user with username and password
     * @param username User's username
     * @param password User's password
     * @return Result containing JWT token and user data
     */
    suspend fun login(username: String, password: String): Result<Pair<String, UserDTO>>

    /**
     * Logout current user
     * @return Result success or error
     */
    suspend fun logout(): Result<Unit>

    /**
     * Get current JWT token
     * @return Current token or null if not authenticated
     */
    fun getCurrentToken(): String?

    /**
     * Save JWT token to local storage
     * @param token JWT token to save
     */
    suspend fun saveToken(token: String)

    /**
     * Clear saved token (logout locally)
     */
    suspend fun clearToken()
}
