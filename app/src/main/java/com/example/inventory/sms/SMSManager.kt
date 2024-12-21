package com.example.inventory.sms

import android.telephony.SmsManager
import android.util.Log
import android.util.Patterns

object SMSManager {
    private const val TAG = "SMSManager"

    fun sendSMS(phoneNumber: String?, message: String?): Boolean {
        if (phoneNumber.isNullOrBlank() || message.isNullOrBlank()) return false
        if (!Patterns.PHONE.matcher(phoneNumber).matches()) return false

        return try {
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
            Log.d(TAG, "SMS sent successfully to $phoneNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS: ${e.message}", e)
            false
        }
    }
}
