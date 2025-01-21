package com.example.inventory.sms

import android.telephony.SmsManager
import android.util.Log
import android.util.Patterns

/**
 * SMSManager is a utility object responsible for sending SMS messages.
 * It includes basic validation for phone numbers and ensures proper logging for success or failure.
 */
object SMSManager {
    // Tag for logging messages related to this class
    private const val TAG = "SMSManager"

    /**
     * Sends an SMS message to the specified phone number.
     *
     * @param phoneNumber The recipient's phone number. Must be a valid phone number.
     * @param message The content of the SMS message. Must not be null or blank.
     * @return true if the SMS was sent successfully, false otherwise.
     */
    fun sendSMS(phoneNumber: String?, message: String?): Boolean {
        // Validate that the phone number and message are not null or blank
        if (phoneNumber.isNullOrBlank() || message.isNullOrBlank()) {
            Log.e(TAG, "Invalid input: Phone number or message is null or blank")
            return false
        }

        // Validate that the phone number matches the required format
        if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            Log.e(TAG, "Invalid phone number format: $phoneNumber")
            return false
        }

        return try {
            // Use SmsManager to send the SMS message
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
            Log.d(TAG, "SMS sent successfully to $phoneNumber")
            true // Indicate success
        } catch (e: Exception) {
            // Log any exception that occurs during the sending process
            Log.e(TAG, "Failed to send SMS: ${e.message}", e)
            false // Indicate failure
        }
    }
}
