package io.github.brainage04.maidex.autologin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class AutoLoginStoreTest {
    private val preferences = InMemoryPreferences()
    private val cipher = SoftwareAesCipher()
    private val store = AutoLoginStore(preferences, cipher)

    @Test
    fun automaticSignInIsDisabledByDefault() {
        assertFalse(store.isEnabled)
        assertFalse(store.hasCredentials)
        assertNull(store.loadCredentials())
    }

    @Test
    fun credentialsRoundTripWithoutPlaintextAtRest() {
        store.saveCredentials(DxNetCredentials("player.one@example.com", "s3cret-パスワード"))

        val loaded = store.loadCredentials()
        assertEquals("player.one@example.com", loaded?.username)
        assertEquals("s3cret-パスワード", loaded?.password)
        val stored = preferences.values.values.joinToString("\n") { it.toString() }
        assertFalse(stored.contains("s3cret"))
        assertFalse(stored.contains("player.one"))
        val decoded = preferences.values.values.filterIsInstance<String>()
            .mapNotNull { runCatching { String(Base64.getDecoder().decode(it)) }.getOrNull() }
        assertTrue(decoded.none { it.contains("s3cret") || it.contains("player.one") })
    }

    @Test
    fun toStringNeverRevealsPassword() {
        assertFalse(DxNetCredentials("user", "hunter2").toString().contains("hunter2"))
    }

    @Test
    fun forgetWipesEverythingAndDeletesKey() {
        store.saveCredentials(DxNetCredentials("user", "pw"))
        store.setEnabled(true)
        store.markRejected("bad password")

        store.forget()

        assertTrue(preferences.values.isEmpty())
        assertFalse(store.isEnabled)
        assertNull(store.loadCredentials())
        assertNull(store.rejectionMessage)
        assertEquals(1, cipher.deleteCount)
    }

    @Test
    fun tamperedCiphertextIsWipedInsteadOfFailingForever() {
        store.saveCredentials(DxNetCredentials("user", "pw"))
        val key = preferences.values.keys.first { it.contains("ciphertext") }
        val bytes = Base64.getDecoder().decode(preferences.values[key] as String)
        bytes[0] = (bytes[0].toInt() xor 1).toByte()
        preferences.values[key] = Base64.getEncoder().encodeToString(bytes)

        assertNull(store.loadCredentials())
        assertFalse(store.hasCredentials)
    }

    @Test
    fun lostKeystoreKeyIsHandled() {
        store.saveCredentials(DxNetCredentials("user", "pw"))
        cipher.deleteKey() // e.g. restored to a new device or key invalidated

        assertNull(store.loadCredentials())
        assertFalse(store.hasCredentials)
    }

    @Test
    fun savingNewCredentialsClearsRejectionAndThrottle() {
        store.saveCredentials(DxNetCredentials("user", "old"))
        store.recordAttempt(1_000L)
        store.markRejected("SEGA ID or password is incorrect.")

        store.saveCredentials(DxNetCredentials("user", "new"))

        assertNull(store.rejectionMessage)
        assertEquals(0L, store.lastAttemptAt)
        assertEquals("new", store.loadCredentials()?.password)
    }

    @Test
    fun disablingKeepsCredentialsUntilForgotten() {
        store.saveCredentials(DxNetCredentials("user", "pw"))
        store.setEnabled(true)
        store.setEnabled(false)

        assertFalse(store.isEnabled)
        assertEquals("user", store.savedUsername())
    }
}
