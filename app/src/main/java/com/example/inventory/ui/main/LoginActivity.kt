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
 */
class LoginActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels() // ViewModel for user authentication

    // UI elements
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize UI elements
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        // Set listeners for buttons
        loginButton.setOnClickListener { handleLogin() }
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Handles the login process by validating user credentials through the ViewModel.
     */
    private fun handleLogin() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validate inputs
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            return
        }

        // Authenticate user securely
        mainViewModel.authenticateUser(username, password) { success ->
            if (success) {
                // Navigate to OverviewActivity on successful login
                startActivity(Intent(this, OverviewActivity::class.java))
                finish()
            } else {
                // Show error message on failed login
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
