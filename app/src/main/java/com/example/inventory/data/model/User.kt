package com.example.inventory.data.model

import android.util.Base64
import com.google.firebase.firestore.PropertyName
import com.example.inventory.utils.KeyStoreManager
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import android.util.Log

/**
 * Represents a user in the inventory system, including secure password handling.
 *
 * @property username          The unique username of the user.
 * @property encryptedPassword The Base64-encoded encrypted password stored in Firestore.
 */
data class User(
    val username: String = "",
    @get:PropertyName("password") @set:PropertyName("password")
    var encryptedPassword: String = "" // Maps Firestore "password" field to this property
) {
    companion object {
        private const val IV_SIZE = 12 // Size (in bytes) of the IV used in AES/GCM
        private const val GCM_TAG_LENGTH = 128 // Authentication tag length (in bits) for AES/GCM

        /**
         * Encrypts a plaintext password using AES in GCM mode, retrieving the key from KeyStoreManager.
         *
         * @param password The plaintext password to be encrypted.
         * @return A Base64-encoded string containing the IV and the encrypted password.
         */
        fun encryptPassword(password: String): String {
            val key = KeyStoreManager.getSecretKey() // Obtain the encryption key from Android's Keystore
            val cipher = Cipher.getInstance("AES/GCM/NoPadding") // Encryption algorithm and mode

            // Initialize the cipher for encryption; generates an IV automatically
            cipher.init(Cipher.ENCRYPT_MODE, key)

            // Capture the auto-generated IV
            val iv = cipher.iv

            // Encrypt the plaintext password
            val encryptedValue = cipher.doFinal(password.toByteArray())

            // Combine the IV with the encrypted data
            val combined = iv + encryptedValue

            // Log the combined data (in Base64) for debugging
            Log.d("Encryption", "IV + Encrypted Data: ${Base64.encodeToString(combined, Base64.DEFAULT)}")

            // Return the combined data in Base64 form
            return Base64.encodeToString(combined, Base64.DEFAULT)
        }

        /**
         * Decrypts an encrypted password from Base64, using AES in GCM mode.
         *
         * @param encryptedPassword A Base64-encoded string containing the IV + encrypted password.
         * @return The plaintext password as a String.
         * @throws IllegalArgumentException If the encrypted data is in an invalid format.
         */
        fun decryptPassword(encryptedPassword: String): String {
            val key = KeyStoreManager.getSecretKey() // Obtain the decryption key from the Keystore
            val combined = Base64.decode(encryptedPassword, Base64.DEFAULT) // Decode the IV + encrypted data

            // Check that data includes space for both IV and encrypted password
            if (combined.size < IV_SIZE) {
                throw IllegalArgumentException("Invalid encrypted password format")
            }

            // Extract the IV from the start of the byte array
            val iv = combined.copyOfRange(0, IV_SIZE)
            // Extract the encrypted password from the remaining bytes
            val encryptedValue = combined.copyOfRange(IV_SIZE, combined.size)

            // Prepare the cipher for decryption
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            // Decrypt the password and convert back to a String
            val decryptedValue = cipher.doFinal(encryptedValue)
            return String(decryptedValue)
        }
    }
}

/** Reference: https://firebase.google.com/docs/firestore/quickstart#kotlin
  * Created by: Bradley Wells*/
