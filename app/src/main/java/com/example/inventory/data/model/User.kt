package com.example.inventory.data.model

import android.util.Base64
import com.google.firebase.firestore.PropertyName
import com.example.inventory.utils.KeyStoreManager
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import android.util.Log

/**
 * Represents a user in the inventory system with secure password encryption and decryption.
 *
 * @property username The unique username of the user.
 * @property encryptedPassword The Base64-encoded encrypted password stored in Firestore.
 */
data class User(
    val username: String = "",
    @get:PropertyName("password") @set:PropertyName("password")
    var encryptedPassword: String = "" // Maps Firestore password field to this property
) {
    companion object {
        private const val IV_SIZE = 12 // Size of the initialization vector in bytes
        private const val GCM_TAG_LENGTH = 128 // Authentication tag length for AES/GCM in bits

        /**
         * Encrypts a plaintext password using AES/GCM with a securely managed key.
         *
         * @param password The plaintext password to encrypt.
         * @return The Base64-encoded string containing the IV and the encrypted password.
         */
        fun encryptPassword(password: String): String {
            val key = KeyStoreManager.getSecretKey() // Retrieve the encryption key from KeyStoreManager
            val cipher = Cipher.getInstance("AES/GCM/NoPadding") // Specify the encryption algorithm and mode

            // Initialize the cipher for encryption; an IV is automatically generated
            cipher.init(Cipher.ENCRYPT_MODE, key)

            // Retrieve the generated IV for this encryption operation
            val iv = cipher.iv

            // Encrypt the plaintext password
            val encryptedValue = cipher.doFinal(password.toByteArray())

            // Combine the IV and the encrypted password into a single array
            val combined = iv + encryptedValue

            // Log the combined data for debugging purposes (in Base64 format)
            Log.d("Encryption", "IV + Encrypted Data: ${Base64.encodeToString(combined, Base64.DEFAULT)}")

            // Return the Base64-encoded combined data
            return Base64.encodeToString(combined, Base64.DEFAULT)
        }

        /**
         * Decrypts an encrypted password stored in Base64 format using AES/GCM.
         *
         * @param encryptedPassword The Base64-encoded string containing the IV and the encrypted password.
         * @return The plaintext password after decryption.
         * @throws IllegalArgumentException If the encrypted password format is invalid.
         */
        fun decryptPassword(encryptedPassword: String): String {
            val key = KeyStoreManager.getSecretKey() // Retrieve the decryption key from KeyStoreManager
            val combined = Base64.decode(encryptedPassword, Base64.DEFAULT) // Decode the Base64-encoded data

            // Ensure the combined data is large enough to contain both the IV and encrypted password
            if (combined.size < IV_SIZE) {
                throw IllegalArgumentException("Invalid encrypted password format")
            }

            // Extract the IV and the encrypted password from the combined data
            val iv = combined.copyOfRange(0, IV_SIZE)
            val encryptedValue = combined.copyOfRange(IV_SIZE, combined.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding") // Specify the decryption algorithm and mode

            // Initialize the cipher for decryption using the extracted IV
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            // Decrypt the encrypted password
            val decryptedValue = cipher.doFinal(encryptedValue)

            // Return the plaintext password as a string
            return String(decryptedValue)
        }
    }
}

/** https://firebase.google.com/docs/firestore/quickstart#kotlin */