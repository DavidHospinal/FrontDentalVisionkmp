package com.dentalvision.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Base ViewModel providing common functionality for all ViewModels
 * Includes error handling and coroutine utilities
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * Global exception handler for coroutines
     * Override in child classes to customize error handling
     */
    protected open val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    /**
     * Handle errors that occur in ViewModels
     * Override in child classes to customize error handling behavior
     */
    protected open fun handleError(throwable: Throwable) {
        // Default implementation - log error
        // Child classes can override to update UI state
        println("ViewModel Error: ${throwable.message}")
        throwable.printStackTrace()
    }

    /**
     * Launch a coroutine with automatic error handling
     * @param block The coroutine code to execute
     */
    protected fun launchWithErrorHandler(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }

    /**
     * Execute a suspend function safely, catching and handling exceptions
     * Returns null if an exception occurs
     */
    protected suspend fun <T> executeSafely(block: suspend () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            handleError(e)
            null
        }
    }
}
