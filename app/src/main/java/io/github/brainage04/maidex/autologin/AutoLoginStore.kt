package io.github.brainage04.maidex.autologin

import android.content.Context
import android.content.SharedPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.Base64

/** SEGA ID credentials for maimai DX NET. [toString] never reveals the password. */
class DxNetCredentials(val username: String, val password: String) {
    init {
        require(username.isNotBlank()) { "SEGA ID is required" }
        require(password.isNotEmpty()) { "Password is required" }
    }

    override fun toString(): String = "DxNetCredentials(username=$username, password=██)"
}

/**
 * Persists the opt-in automatic-login setting and the user's DX NET credentials.
 *
 * Credentials are only ever written as AES-GCM ciphertext produced by [cipher] (an Android
 * Keystore key in production). The backing preferences file is excluded from cloud backup and
 * device transfer (see `res/xml/data_extraction_rules.xml` and `backup_rules.xml`), so a
 * restored device simply has no credentials rather than undecryptable ones.
 */
class AutoLoginStore(
    private val preferences: SharedPreferences,
    private val cipher: CredentialCipher,
) {
    val isEnabled: Boolean get() = preferences.getBoolean(KEY_ENABLED, false)

    val hasCredentials: Boolean
        get() = preferences.contains(KEY_CIPHERTEXT) && preferences.contains(KEY_IV)

    /** Username shown in settings. Stored encrypted with the password, never in plaintext. */
    fun savedUsername(): String? = loadCredentials()?.username

    /** Set when DX NET rejected the saved password; automatic sign-in stays paused until new credentials are saved. */
    val rejectionMessage: String? get() = preferences.getString(KEY_REJECTED, null)

    val lastAttemptAt: Long get() = preferences.getLong(KEY_LAST_ATTEMPT_AT, 0L)

    val lastResult: String? get() = preferences.getString(KEY_LAST_RESULT, null)

    fun setEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_ENABLED, enabled).commit()
    }

    /** Encrypts and stores [credentials], clearing any earlier rejection so automatic sign-in can resume. */
    fun saveCredentials(credentials: DxNetCredentials) {
        val encrypted = cipher.encrypt(encode(credentials), ASSOCIATED_DATA)
        val encoder = Base64.getEncoder()
        preferences.edit()
            .putString(KEY_IV, encoder.encodeToString(encrypted.iv))
            .putString(KEY_CIPHERTEXT, encoder.encodeToString(encrypted.ciphertext))
            .remove(KEY_REJECTED)
            .remove(KEY_LAST_ATTEMPT_AT)
            .commit()
    }

    /**
     * Returns the decrypted credentials, or null when none are stored. Undecryptable data (for
     * example after the Keystore key was invalidated) is wiped so the user is asked to re-enter it
     * rather than failing on every launch.
     */
    fun loadCredentials(): DxNetCredentials? {
        val iv = preferences.getString(KEY_IV, null) ?: return null
        val ciphertext = preferences.getString(KEY_CIPHERTEXT, null) ?: return null
        return try {
            val decoder = Base64.getDecoder()
            decode(
                cipher.decrypt(
                    EncryptedBlob(decoder.decode(iv), decoder.decode(ciphertext)),
                    ASSOCIATED_DATA,
                ),
            )
        } catch (_: Exception) {
            wipeCredentials()
            null
        }
    }

    fun markRejected(message: String) {
        preferences.edit().putString(KEY_REJECTED, message).commit()
    }

    fun recordAttempt(at: Long) {
        preferences.edit().putLong(KEY_LAST_ATTEMPT_AT, at).commit()
    }

    fun recordResult(message: String) {
        preferences.edit().putString(KEY_LAST_RESULT, message).commit()
    }

    /** Disables automatic sign-in and irreversibly removes the credentials and their Keystore key. */
    fun forget() {
        preferences.edit().clear().commit()
        cipher.deleteKey()
    }

    private fun wipeCredentials() {
        preferences.edit().remove(KEY_IV).remove(KEY_CIPHERTEXT).commit()
        cipher.deleteKey()
    }

    private fun encode(credentials: DxNetCredentials): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            output.writeByte(FORMAT_VERSION)
            output.writeUTF(credentials.username)
            output.writeUTF(credentials.password)
        }
        return bytes.toByteArray()
    }

    private fun decode(plaintext: ByteArray): DxNetCredentials =
        DataInputStream(ByteArrayInputStream(plaintext)).use { input ->
            check(input.readByte().toInt() == FORMAT_VERSION) { "Unknown credential format" }
            DxNetCredentials(username = input.readUTF(), password = input.readUTF())
        }

    companion object {
        const val PREFERENCES_NAME = "dx-net-auto-login"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_IV = "credentials_iv"
        private const val KEY_CIPHERTEXT = "credentials_ciphertext"
        private const val KEY_REJECTED = "rejected_message"
        private const val KEY_LAST_ATTEMPT_AT = "last_attempt_at"
        private const val KEY_LAST_RESULT = "last_result"
        private const val FORMAT_VERSION = 1
        private val ASSOCIATED_DATA = "maidex:dx-net-credentials:v1".toByteArray()

        fun create(context: Context): AutoLoginStore = AutoLoginStore(
            context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
            AndroidKeystoreCipher(),
        )
    }
}
