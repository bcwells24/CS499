package com.example.inventory.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * com.example.inventory.utils.KeyStoreManager provides secure key management using the Android Keystore system.
 */
object KeyStoreManager {
    private const val ALIAS = "UserEncryptionKey" // Alias for the encryption key
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    /**
     * Retrieves the secret key from the Android Keystore.
     * If the key does not exist, it generates a new one.
     *
     * @return SecretKey The encryption key.
     */
    fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null) // Load the Android Keystore
        }

        // Check if the key already exists
        return keyStore.getKey(ALIAS, null) as? SecretKey ?: generateKey()
    }

    /**
     * Generates a new secret key and stores it in the Android Keystore.
     *
     * @return SecretKey The newly generated encryption key.
     */
    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256) // Set key size to 256 bits
                .build()
        )
        return keyGenerator.generateKey()
    }
}

/** https://developer.android.com/privacy-and-security/keystore?hl=en */