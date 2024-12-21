package com.example.inventory

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * MainActivity class that handles user login and navigation to the registration page.
 * This is the entry point of the application where users can log in or register.
 */
class MainActivity : AppCompatActivity() {

    // DatabaseHelper instance to manage user authentication
    private lateinit var db: DatabaseHelper

    // UI components for user input fields
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize DatabaseHelper for user authentication
        db = DatabaseHelper(this)

        // Initialize UI components for username and password inputs
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val registerButton: Button = findViewById(R.id.registerButton)

        // Set click listener for the login button to handle user login
        loginButton.setOnClickListener {
            handleLogin() // Calls method to handle login logic
        }

        // Set click listener for the registration button to navigate to RegisterActivity
        registerButton.setOnClickListener {
            // Start RegisterActivity to allow user registration
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Handles the login process by validating the username and password.
     * If authentication is successful, the user is redirected to the inventory overview screen.
     * If the credentials are invalid, an error message is shown.
     */
    private fun handleLogin() {
        // Get username and password from the input fields
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Validate credentials using DatabaseHelper
        if (db.authenticateUser(username, password)) {
            // If login is successful, navigate to the inventory overview screen
            val intent = Intent(this@MainActivity, OverviewActivity::class.java)
            startActivity(intent)
            finish() // End MainActivity to prevent returning to login screen
        } else {
            // If credentials are invalid, show a toast message
            Toast.makeText(this@MainActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
        }
    }
}
