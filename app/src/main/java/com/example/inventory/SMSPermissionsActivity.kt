package com.example.inventory

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SMSPermissionsActivity : AppCompatActivity() {

    private lateinit var phoneNumberEditText: EditText
    private lateinit var smsPermissionToggle: ToggleButton
    private lateinit var permissionStatusTextView: TextView
    private lateinit var returnButton: ImageButton
    private lateinit var logoutButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "SMSPrefs"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_SMS_PERMISSION = "sms_permission"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_permissions)

        initializeViews()
        setupSharedPreferences()
        loadPreferences()
        setupListeners()
    }

    private fun initializeViews() {
        phoneNumberEditText = findViewById(R.id.editTextPhoneNumber)
        smsPermissionToggle = findViewById(R.id.toggleButtonSmsPermission)
        permissionStatusTextView = findViewById(R.id.textViewPermissionStatus)
        returnButton = findViewById(R.id.returnButton)
        logoutButton = findViewById(R.id.logoutButton)
    }

    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    }

    private fun setupListeners() {
        smsPermissionToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isValidPhoneNumber()) {
                smsPermissionToggle.isChecked = false
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }

            savePreferences(isChecked)
            updatePermissionStatus(isChecked)

            Toast.makeText(
                this,
                if (isChecked) "SMS notifications enabled" else "SMS notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        logoutButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        returnButton.setOnClickListener {
            finish()
        }
    }

    private fun loadPreferences() {
        val phoneNumber = sharedPreferences.getString(KEY_PHONE_NUMBER, "") ?: ""
        val smsPermission = sharedPreferences.getBoolean(KEY_SMS_PERMISSION, false)

        phoneNumberEditText.setText(phoneNumber)
        smsPermissionToggle.isChecked = smsPermission
        updatePermissionStatus(smsPermission)
    }

    private fun savePreferences(smsPermission: Boolean) {
        sharedPreferences.edit().apply {
            putString(KEY_PHONE_NUMBER, phoneNumberEditText.text.toString().trim())
            putBoolean(KEY_SMS_PERMISSION, smsPermission)
            apply()
        }
    }

    private fun updatePermissionStatus(isGranted: Boolean) {
        permissionStatusTextView.text = if (isGranted) "Permission Status: Granted" else "Permission Status: Denied"
    }

    private fun isValidPhoneNumber(): Boolean {
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        return phoneNumber.isNotEmpty() && android.util.Patterns.PHONE.matcher(phoneNumber).matches()
    }
}
