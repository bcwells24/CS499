package com.example.inventory

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize DatabaseHelper instance
        databaseHelper = DatabaseHelper(this)

        // Initialize EditText views for capturing user input
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        // Initialize buttons for registration and going back
        val registerButton: Button = findViewById(R.id.registerButton)
        val goBackButton: Button = findViewById(R.id.goBackButton)

        // Set listeners for the buttons
        registerButton.setOnClickListener { registerUser() }
        goBackButton.setOnClickListener { finish() }
    }

    private fun registerUser() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
            return
        }

        val isSuccess = databaseHelper.registerUser(username, password)

        if (isSuccess) {
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
        }
    }
}
