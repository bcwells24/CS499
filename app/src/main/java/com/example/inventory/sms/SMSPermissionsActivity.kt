package com.example.inventory.sms

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.ToggleButton
import android.widget.TextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.inventory.R
import com.example.inventory.ui.main.LoginActivity

/**
 * This activity manages SMS permissions and stores user preferences such as phone number
 * and SMS permission state using SharedPreferences.
 */
class SMSPermissionsActivity : AppCompatActivity() {

    // UI elements
    private lateinit var phoneNumberEditText: EditText
    private lateinit var smsPermissionToggle: ToggleButton
    private lateinit var permissionStatusTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var returnButton: ImageButton

    // SharedPreferences instance for storing user preferences
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "SMSPrefs" // Name of the SharedPreferences file
        private const val KEY_PHONE_NUMBER = "phone_number" // Key for storing phone number
        private const val KEY_SMS_PERMISSION = "sms_permission" // Key for storing SMS permission state
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_permissions)
        try {
            // Initialize UI elements
            phoneNumberEditText = findViewById(R.id.editTextPhoneNumber)
            smsPermissionToggle = findViewById(R.id.toggleButtonSmsPermission)
            permissionStatusTextView = findViewById(R.id.textViewPermissionStatus)
            logoutButton = findViewById(R.id.logoutButton)
            returnButton = findViewById(R.id.returnButton)

            // Initialize SharedPreferences
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

            // Load saved preferences and update the UI
            loadPreferences()

            // Handle SMS permission toggle
            smsPermissionToggle.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && phoneNumberEditText.text.isNullOrEmpty()) {
                    // Show an error message if the phone number is empty
                    Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                    smsPermissionToggle.isChecked = false
                    return@setOnCheckedChangeListener
                }
                // Save the updated preferences and update the UI
                savePreferences(isChecked)
                updatePermissionStatus(isChecked)
            }

            // Handle logout button click
            logoutButton.setOnClickListener {
                // Navigate to the LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            // Handle return button click
            returnButton.setOnClickListener {
                // Close the current activity and return to the previous screen
                finish()
            }

        } catch (e: Exception) {
            // Handle unexpected errors during activity initialization
            e.printStackTrace()
            Toast.makeText(this, "Error loading activity: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Loads the saved preferences for phone number and SMS permission state
     * and updates the corresponding UI elements.
     */
    private fun loadPreferences() {
        val phoneNumber = sharedPreferences.getString(KEY_PHONE_NUMBER, "") ?: ""
        val isSmsEnabled = sharedPreferences.getBoolean(KEY_SMS_PERMISSION, false)

        // Update the phone number input field and toggle button state
        phoneNumberEditText.setText(phoneNumber)
        smsPermissionToggle.isChecked = isSmsEnabled
        updatePermissionStatus(isSmsEnabled)
    }

    /**
     * Saves the provided SMS permission state and the entered phone number
     * into SharedPreferences.
     *
     * @param smsPermission The current state of the SMS permission toggle.
     */
    private fun savePreferences(smsPermission: Boolean) {
        sharedPreferences.edit().apply {
            putString(KEY_PHONE_NUMBER, phoneNumberEditText.text.toString().trim())
            putBoolean(KEY_SMS_PERMISSION, smsPermission)
            apply()
        }
    }

    /**
     * Updates the UI to display the current SMS permission status.
     *
     * @param isGranted True if SMS permission is granted; false otherwise.
     */
    private fun updatePermissionStatus(isGranted: Boolean) {
        permissionStatusTextView.text =
            if (isGranted) "Permission Status: Granted" else "Permission Status: Denied"
    }
}
