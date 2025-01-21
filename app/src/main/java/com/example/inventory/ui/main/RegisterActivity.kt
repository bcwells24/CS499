package com.example.inventory.ui.main

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.inventory.R

/**
 * RegisterActivity handles the user registration process and interacts with MainViewModel
 * to create a new user account in the Firestore database.
 */
class RegisterActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels() // ViewModel for user registration

    // UI elements
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var goBackButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize UI elements
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        goBackButton = findViewById(R.id.goBackButton)

        // Set button listeners
        registerButton.setOnClickListener { registerUser() }
        goBackButton.setOnClickListener { finish() }
    }

    /**
     * Validates user input and registers a new user via the MainViewModel.
     */
    private fun registerUser() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validate input fields
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
            return
        }

        if (!password.any { it.isDigit() } || !password.any { it.isLetter() }) {
            Toast.makeText(this, "Password must contain both letters and numbers", Toast.LENGTH_SHORT).show()
            return
        }

        // Call ViewModel to register the user securely
        mainViewModel.registerUser(username, password) { success ->
            if (success) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                finish() // Go back to the previous screen
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/** Reference: https://www.geeksforgeeks.org/user-authentication-using-firebase-in-android/ */