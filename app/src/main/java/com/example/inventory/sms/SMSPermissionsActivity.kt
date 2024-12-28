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

class SMSPermissionsActivity : AppCompatActivity() {

    private lateinit var phoneNumberEditText: EditText
    private lateinit var smsPermissionToggle: ToggleButton
    private lateinit var permissionStatusTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var returnButton: ImageButton

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "SMSPrefs"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_SMS_PERMISSION = "sms_permission"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_permissions)
        try {

            phoneNumberEditText = findViewById(R.id.editTextPhoneNumber)
            smsPermissionToggle = findViewById(R.id.toggleButtonSmsPermission)
            permissionStatusTextView = findViewById(R.id.textViewPermissionStatus)
            logoutButton = findViewById(R.id.logoutButton)
            returnButton = findViewById(R.id.returnButton)

            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            loadPreferences()

            smsPermissionToggle.setOnCheckedChangeListener { _, isChecked ->
                println("Toggle Button Changed: $isChecked")
                if (isChecked && phoneNumberEditText.text.isNullOrEmpty()) {
                    Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                    smsPermissionToggle.isChecked = false
                    return@setOnCheckedChangeListener
                }
                savePreferences(isChecked)
                updatePermissionStatus(isChecked)
            }


            logoutButton.setOnClickListener {
                // println("Logout Button Clicked")
                // Toast.makeText(this, "Logout Button Clicked", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            returnButton.setOnClickListener {
                // println("Return Button Clicked")
                // Toast.makeText(this, "Return Button Clicked", Toast.LENGTH_SHORT).show()
                finish()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading activity: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun loadPreferences() {
        val phoneNumber = sharedPreferences.getString(KEY_PHONE_NUMBER, "") ?: ""
        val isSmsEnabled = sharedPreferences.getBoolean(KEY_SMS_PERMISSION, false)

        println("Loaded Phone Number: $phoneNumber")
        println("Loaded SMS Permission: $isSmsEnabled")

        phoneNumberEditText.setText(phoneNumber)
        smsPermissionToggle.isChecked = isSmsEnabled
        updatePermissionStatus(isSmsEnabled)
    }

    private fun savePreferences(smsPermission: Boolean) {
        println("Saving Phone Number: ${phoneNumberEditText.text}")
        println("Saving SMS Permission: $smsPermission")

        sharedPreferences.edit().apply {
            putString(KEY_PHONE_NUMBER, phoneNumberEditText.text.toString().trim())
            putBoolean(KEY_SMS_PERMISSION, smsPermission)
            apply()
        }
    }

    private fun updatePermissionStatus(isGranted: Boolean) {
        permissionStatusTextView.text =
            if (isGranted) "Permission Status: Granted" else "Permission Status: Denied"
    }
}
