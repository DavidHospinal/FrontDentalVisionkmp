package com.dentalvision.ai.domain.util

/**
 * A generic sealed class representing the result of an operation
 * Used for handling success and error states in a type-safe manner
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with an error message
     */
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()

    /**
     * Returns true if this result is a success
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns true if this result is an error
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Returns the data if success, or null if error
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the data if success, or throws exception if error
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception ?: Exception(message)
    }

    /**
     * Maps the success value using the given transformation
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    /**
     * Executes the given action if this result is a success
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Executes the given action if this result is an error
     */
    inline fun onError(action: (String) -> Unit): Result<T> {
        if (this is Error) action(message)
        return this
    }
}
