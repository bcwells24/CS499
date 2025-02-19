package com.example.inventory.sms

import android.telephony.SmsManager
import android.util.Log
import android.util.Patterns

/**
 * SMSManager is a utility object responsible for sending SMS messages.
 * It includes basic validation for phone numbers and ensures proper logging for success or failure.
 */
object SMSManager {

    // Tag used to identify log messages from this class
    private const val TAG = "SMSManager"

    /**
     * Sends an SMS message to the specified phone number after performing basic validation.
     *
     * @param phoneNumber The recipient's phone number. Must be a non-blank string that matches a valid phone pattern.
     * @param message     The content of the SMS message. Must be a non-blank string.
     * @return true if the SMS was sent successfully, false otherwise.
     */
    fun sendSMS(phoneNumber: String?, message: String?): Boolean {
        // Check that the phone number and message are neither null nor empty
        if (phoneNumber.isNullOrBlank() || message.isNullOrBlank()) {
            Log.e(TAG, "Invalid input: phone number or message is null or blank")
            return false
        }

        // Validate phone number format using Android's built-in phone number pattern
        if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            Log.e(TAG, "Invalid phone number format: $phoneNumber")
            return false
        }

        return try {
            // Use SmsManager to send the SMS
            // The sendTextMessage method takes the destination address, a service center address (null here),
            // the text message, a sentIntent, and a deliveryIntent (both null here).
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
            Log.d(TAG, "SMS sent successfully to $phoneNumber")
            true
        } catch (e: Exception) {
            // Log the exception if the SMS sending fails
            Log.e(TAG, "Failed to send SMS: ${e.message}", e)
            false
        }
    }
}

/** Reference: https://www.tutorialspoint.com/android/android_sending_sms.html
 * Created by: Bradley Wells */
