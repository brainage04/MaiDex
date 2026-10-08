package io.github.brainage04.maidex.autologin

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptedBlob(val iv: ByteArray, val ciphertext: ByteArray)

interface CredentialCipher {
    fun encrypt(plaintext: ByteArray, associatedData: ByteArray): EncryptedBlob
    fun decrypt(blob: EncryptedBlob, associatedData: ByteArray): ByteArray
    fun deleteKey()
}

/**
 * AES-256-GCM with a non-exportable key held in the Android Keystore (hardware-backed where the
 * device supports it). The key never leaves the Keystore, so a copy of the app's data directory
 * is useless without the device. User authentication is deliberately not required because the
 * credentials are used for a background refresh when the app opens.
 */
class AndroidKeystoreCipher(private val alias: String = DEFAULT_ALIAS) : CredentialCipher {
    override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): EncryptedBlob {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key(create = true))
        cipher.updateAAD(associatedData)
        val ciphertext = cipher.doFinal(plaintext)
        return EncryptedBlob(cipher.iv, ciphertext)
    }

    override fun decrypt(blob: EncryptedBlob, associatedData: ByteArray): ByteArray {
        val key = key(create = false) ?: throw IllegalStateException("Credential key is missing")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, blob.iv))
        cipher.updateAAD(associatedData)
        return cipher.doFinal(blob.ciphertext)
    }

    override fun deleteKey() {
        runCatching { keyStore().deleteEntry(alias) }
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }

    private fun key(create: Boolean): SecretKey? {
        (keyStore().getKey(alias, null) as? SecretKey)?.let { return it }
        if (!create) return null
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val KEYSTORE = "AndroidKeyStore"
        const val DEFAULT_ALIAS = "maidex-dx-net-credentials"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
    }
}
