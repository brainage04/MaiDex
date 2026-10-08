package io.github.brainage04.maidex.autologin

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Exercises the real Android Keystore cipher and the on-disk preferences file. */
@RunWith(AndroidJUnit4::class)
class KeystoreCredentialStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val preferencesName = "dx-net-auto-login-test"
    private val alias = "maidex-dx-net-credentials-test"
    private lateinit var store: AutoLoginStore

    @Before
    fun setUp() {
        store = AutoLoginStore(
            context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE),
            AndroidKeystoreCipher(alias),
        )
        store.forget()
    }

    @After
    fun tearDown() {
        store.forget()
        context.deleteSharedPreferences(preferencesName)
    }

    @Test
    fun roundTripsThroughKeystoreWithoutPlaintextOnDisk() {
        store.saveCredentials(DxNetCredentials("keystore-user", "keystore-password-123"))

        assertEquals("keystore-password-123", store.loadCredentials()?.password)
        val file = File(context.applicationInfo.dataDir, "shared_prefs/$preferencesName.xml")
        val onDisk = file.readText()
        assertFalse(onDisk.contains("keystore-password-123"))
        assertFalse(onDisk.contains("keystore-user"))
    }

    @Test
    fun forgetDeletesKeyAndCredentials() {
        store.saveCredentials(DxNetCredentials("user", "pw"))
        store.forget()

        assertNull(store.loadCredentials())
        assertFalse(store.isEnabled)
    }

    @Test
    fun ciphertextFromDeletedKeyIsDiscarded() {
        store.saveCredentials(DxNetCredentials("user", "pw"))
        AndroidKeystoreCipher(alias).deleteKey()

        assertNull(store.loadCredentials())
        assertFalse(store.hasCredentials)
    }
}
