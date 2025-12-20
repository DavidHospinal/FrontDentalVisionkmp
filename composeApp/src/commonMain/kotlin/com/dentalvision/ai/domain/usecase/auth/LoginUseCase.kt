package com.dentalvision.ai.domain.usecase.auth

import com.dentalvision.ai.domain.util.Result
import kotlinx.coroutines.delay

/**
 * Use case for authenticating users
 * Validates credentials and returns authentication result
 */
class LoginUseCase {

    /**
     * Authenticate user with email and password
     * @param email User email address
     * @param password User password
     * @return Result containing authentication success or error message
     */
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        // Validate input
        if (email.isBlank()) {
            return Result.Error("Email is required")
        }

        if (password.isBlank()) {
            return Result.Error("Password is required")
        }

        if (!email.contains("@")) {
            return Result.Error("Invalid email format")
        }

        if (password.length < 6) {
            return Result.Error("Password must be at least 6 characters")
        }

        // Simulate network delay
        delay(1000)

        // TODO: Replace with actual API call
        // For now, accept demo credentials
        return if (email == "admin@dentalvision.ai" && password == "admin123") {
            Result.Success(Unit)
        } else {
            Result.Error("Invalid credentials")
        }
    }
}
