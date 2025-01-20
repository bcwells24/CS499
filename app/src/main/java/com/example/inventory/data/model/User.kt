package com.example.inventory.data.model

import android.util.Base64
import com.google.firebase.firestore.PropertyName
import java.security.SecureRandom
import java.security.KeyStore
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Log

data class User(
    val username: String = "",
    @get:PropertyName("password") @set:PropertyName("password")
    var encryptedPassword: String = ""
) {
    companion object {
        private const val IV_SIZE = 12
        private const val GCM_TAG_LENGTH = 128

        private fun getSecureKey(): SecretKey {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val alias = "UserEncryptionKey"

            return keyStore.getKey(alias, null) as? SecretKey ?: run {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
                keyGenerator.generateKey()
            }
        }

        fun encryptPassword(password: String): String {
            val key = getSecureKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")

            // Initialize cipher for encryption (IV is automatically generated)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            // Retrieve the auto-generated IV
            val iv = cipher.iv
            val encryptedValue = cipher.doFinal(password.toByteArray())

            // Combine IV and encrypted value
            val combined = iv + encryptedValue
            Log.d("Encryption", "IV + Encrypted Data: ${Base64.encodeToString(combined, Base64.DEFAULT)}")
            return Base64.encodeToString(combined, Base64.DEFAULT)
        }


        fun decryptPassword(encryptedPassword: String): String {
            val key = getSecureKey()
            val combined = Base64.decode(encryptedPassword, Base64.DEFAULT)

            if (combined.size < IV_SIZE) {
                throw IllegalArgumentException("Invalid encrypted password format")
            }

            // Extract the IV and encrypted data
            val iv = combined.copyOfRange(0, IV_SIZE)
            val encryptedValue = combined.copyOfRange(IV_SIZE, combined.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")

            // Initialize cipher for decryption using the extracted IV
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            val decryptedValue = cipher.doFinal(encryptedValue)
            return String(decryptedValue)
        }

    }


}
