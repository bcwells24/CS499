package com.example.inventory.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.inventory.R
import com.example.inventory.ui.overview.OverviewActivity

/**
 * LoginActivity handles user authentication and navigation to the overview screen.
 * It retrieves user credentials from input fields and delegates authentication to the ViewModel.
 */
class LoginActivity : AppCompatActivity() {

    // Obtain an instance of MainViewModel for handling user authentication
    private val mainViewModel: MainViewModel by viewModels()

    // UI elements
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    /**
     * Called when the activity is first created. Sets up the layout, initializes UI elements,
     * and establishes click listeners for login and registration.
     *
     * @param savedInstanceState Contains data if the activity is being re-initialized.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize UI elements
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        // Set listeners for login and register actions
        loginButton.setOnClickListener { handleLogin() }
        registerButton.setOnClickListener {
            // Navigate to the RegisterActivity for new user registration
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Gathers the entered username and password, validates them,
     * and triggers the ViewModel to authenticate the user.
     * If successful, it navigates to the OverviewActivity.
     */
    private fun handleLogin() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Check that a username was provided
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show()
            return
        }

        // Check that a password was provided
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            return
        }

        // Delegate authentication to the ViewModel
        mainViewModel.authenticateUser(username, password) { success ->
            if (success) {
                // If credentials are valid, proceed to the overview screen
                startActivity(Intent(this, OverviewActivity::class.java))
                finish()
            } else {
                // If credentials are invalid, display an error
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/** Reference: https://www.tutorialspoint.com/android/android_login_screen.htm */
