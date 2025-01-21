package com.example.inventory.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.repository.InventoryRepository
import kotlinx.coroutines.launch

/**
 * MainViewModel serves as a bridge between the UI and the InventoryRepository for
 * user-related operations such as authentication and registration.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Firestore-backed repository instance
    private val repository = InventoryRepository()

    /**
     * Authenticates the user by verifying the provided username and password.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @param onResult Callback to notify the UI of authentication success or failure.
     */
    fun authenticateUser(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.authenticateUser(username, password)
            onResult(success)
        }
    }

    /**
     * Registers a new user in the Firestore database.
     *
     * @param username The username chosen by the user.
     * @param password The password chosen by the user.
     * @param onResult Callback to notify the UI of registration success or failure.
     */
    fun registerUser(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.registerUser(username, password) // Pass username and password
                onResult(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false) // Notify failure in case of exceptions
            }
        }
    }
}
