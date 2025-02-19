package com.example.inventory.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.repository.InventoryRepository
import kotlinx.coroutines.launch

/**
 * MainViewModel acts as a mediator between UI components and the InventoryRepository,
 * handling user-related operations such as authentication and registration. By extending
 * AndroidViewModel, it can access the application context when needed.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Instance of the repository responsible for interacting with Firestore
    private val repository = InventoryRepository()

    /**
     * Authenticates the user by comparing the given credentials with those in Firestore.
     * This operation is performed asynchronously within the ViewModel's scope.
     *
     * @param username The username entered by the user.
     * @param password The plaintext password entered by the user.
     * @param onResult A callback function used to return the result (true if successful, false otherwise).
     */
    fun authenticateUser(username: String, password: String, onResult: (Boolean) -> Unit) {
        // Launch a coroutine on the ViewModel's scope for asynchronous operations
        viewModelScope.launch {
            // Attempt to authenticate the user using the repository
            val success = repository.authenticateUser(username, password)
            // Invoke the callback with the result
            onResult(success)
        }
    }

    /**
     * Registers a new user in Firestore by encrypting their password before saving.
     * This operation is performed asynchronously within the ViewModel's scope.
     *
     * @param username The desired username for the new user.
     * @param password The plaintext password for the new user.
     * @param onResult A callback function used to return the result (true if successful, false otherwise).
     */
    fun registerUser(username: String, password: String, onResult: (Boolean) -> Unit) {
        // Launch a coroutine on the ViewModel's scope for asynchronous operations
        viewModelScope.launch {
            try {
                // Attempt to register the user using the repository
                val success = repository.registerUser(username, password)
                // Invoke the callback with the result
                onResult(success)
            } catch (e: Exception) {
                // Catch any exceptions that may occur and log them if needed
                e.printStackTrace()
                // Return false through the callback to indicate failure
                onResult(false)
            }
        }
    }
}

/** Reference: https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel
 * Created by: Bradley Wells*/
