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
 * Manages SMS permissions and stores user preferences such as phone number
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
        // Name of the SharedPreferences file
        private const val PREFS_NAME = "SMSPrefs"

        // Key for storing phone number
        private const val KEY_PHONE_NUMBER = "phone_number"

        // Key for storing SMS permission state
        private const val KEY_SMS_PERMISSION = "sms_permission"
    }

    /**
     * Called when the activity is created. Initializes UI components, loads user preferences,
     * and sets up event listeners for SMS permission toggling, logout, and returning to the
     * previous screen.
     *
     * @param savedInstanceState Contains data supplied by the system when the activity is re-initialized.
     */
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

            // Load saved preferences and update UI
            loadPreferences()

            // Handle SMS permission toggle event
            smsPermissionToggle.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && phoneNumberEditText.text.isNullOrEmpty()) {
                    // Show an error if no phone number is provided
                    Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                    smsPermissionToggle.isChecked = false
                    return@setOnCheckedChangeListener
                }
                // Save the updated preferences (phone number + permission) and refresh UI status
                savePreferences(isChecked)
                updatePermissionStatus(isChecked)
            }

            // Handle logout button click
            logoutButton.setOnClickListener {
                // Navigate to the LoginActivity and finish this activity
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            // Handle return button click
            returnButton.setOnClickListener {
                // Close the current activity, returning to the previous screen
                finish()
            }

        } catch (e: Exception) {
            // Log and display any initialization errors
            e.printStackTrace()
            Toast.makeText(this, "Error loading activity: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Loads the saved preferences for phone number and SMS permission state
     * from SharedPreferences, updating the UI elements accordingly.
     */
    private fun loadPreferences() {
        val phoneNumber = sharedPreferences.getString(KEY_PHONE_NUMBER, "") ?: ""
        val isSmsEnabled = sharedPreferences.getBoolean(KEY_SMS_PERMISSION, false)

        // Set the phone number input field and toggle button state based on saved prefs
        phoneNumberEditText.setText(phoneNumber)
        smsPermissionToggle.isChecked = isSmsEnabled
        // Reflect the current permission state in the text view
        updatePermissionStatus(isSmsEnabled)
    }

    /**
     * Saves the current phone number and SMS permission state into SharedPreferences.
     *
     * @param smsPermission The state of the SMS permission toggle (true = granted, false = denied).
     */
    private fun savePreferences(smsPermission: Boolean) {
        sharedPreferences.edit().apply {
            putString(KEY_PHONE_NUMBER, phoneNumberEditText.text.toString().trim())
            putBoolean(KEY_SMS_PERMISSION, smsPermission)
            apply()
        }
    }

    /**
     * Updates the on-screen text to display the current SMS permission status.
     *
     * @param isGranted True if SMS permission is enabled, false otherwise.
     */
    private fun updatePermissionStatus(isGranted: Boolean) {
        permissionStatusTextView.text =
            if (isGranted) "Permission Status: Granted" else "Permission Status: Denied"
    }
}

/** Reference: https://stackoverflow.com/questions/50061698/sms-permissions
 * Created by: Bradley Wells*/
