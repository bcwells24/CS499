package com.example.inventory

import android.telephony.SmsManager
import android.util.Log

object SMSManager {
    private const val TAG = "SMSManager"

    /**
     * Sends an SMS message.
     *
     * @param phoneNumber The recipient's phone number.
     * @param message The message to send.
     * @return `true` if the SMS was sent successfully, `false` otherwise.
     */
    fun sendSMS(phoneNumber: String?, message: String?): Boolean {
        if (!isValidPhoneNumber(phoneNumber) || message.isNullOrEmpty()) {
            Log.e(TAG, "Invalid phone number or empty message")
            return false
        }

        return try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d(TAG, "SMS sent successfully to $phoneNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS: ${e.message}", e)
            false
        }
    }

    /**
     * Validates the phone number format.
     *
     * @param phoneNumber The phone number to validate.
     * @return `true` if the phone number is valid, `false` otherwise.
     */
    private fun isValidPhoneNumber(phoneNumber: String?): Boolean {
        return !phoneNumber.isNullOrEmpty() && android.util.Patterns.PHONE.matcher(phoneNumber).matches()
    }
}
