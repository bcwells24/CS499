package com.example.inventory.ui.main

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.inventory.R

/**
 * RegisterActivity manages user registration, interacting with MainViewModel
 * to create a new user account in the Firestore database. It validates user input
 * and provides feedback through Toast messages.
 */
class RegisterActivity : AppCompatActivity() {

    // Obtain MainViewModel instance for registering new users
    private val mainViewModel: MainViewModel by viewModels()

    // UI elements
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var goBackButton: Button

    /**
     * Called when the activity is first created. Initializes UI components and sets up
     * button click listeners for registration and returning to the previous screen.
     *
     * @param savedInstanceState Contains data if the activity is re-initialized.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize UI elements
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        goBackButton = findViewById(R.id.goBackButton)

        // Attach onClick listeners
        registerButton.setOnClickListener { registerUser() }
        goBackButton.setOnClickListener { finish() }
    }

    /**
     * Validates the user input for username and password, then requests the MainViewModel
     * to register a new user in Firestore. Displays appropriate messages based on
     * validation or registration results.
     */
    private fun registerUser() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Check for empty username
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }

        // Check for empty password
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return
        }

        // Check that password is at least 8 characters long
        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
            return
        }

        // Check that password contains both letters and digits
        if (!password.any { it.isDigit() } || !password.any { it.isLetter() }) {
            Toast.makeText(this, "Password must contain both letters and numbers", Toast.LENGTH_SHORT).show()
            return
        }

        // Attempt to register the user with the ViewModel
        mainViewModel.registerUser(username, password) { success ->
            if (success) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                finish() // Return to the previous screen
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/** Reference: https://www.geeksforgeeks.org/user-authentication-using-firebase-in-android/ */
