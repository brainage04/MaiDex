package io.github.brainage04.maidex.autologin

import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.network.AuthenticationRequiredException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.net.UnknownHostException

class AutoUpdateRunnerTest {
    private val store = AutoLoginStore(InMemoryPreferences(), SoftwareAesCipher())
    private val site = FakeDxNet()
    private val reporter = RecordingReporter()
    private var now = 10_000_000L
    private val runner = AutoUpdateRunner(
        store = store,
        signIn = site,
        reporter = reporter,
        clock = { now },
        minIntervalMillis = 60_000L,
    )

    private fun optIn(password: String = "right") {
        store.saveCredentials(DxNetCredentials("player", password))
        store.setEnabled(true)
    }

    private suspend fun run(force: Boolean = false) = runner.run(AccountRegion.INTERNATIONAL, site.importer, force)

    @Test
    fun disabledByDefaultDoesNothing() = runTest {
        store.saveCredentials(DxNetCredentials("player", "right"))

        assertEquals(AutoUpdateOutcome.Disabled, run())
        assertEquals(0, site.refreshCalls + site.signInCalls)
        assertTrue(reporter.events.isEmpty())
    }

    @Test
    fun enabledWithoutCredentialsDoesNothing() = runTest {
        store.setEnabled(true)

        assertEquals(AutoUpdateOutcome.NoCredentials, run())
        assertTrue(reporter.events.isEmpty())
    }

    @Test
    fun japanIsUnsupported() = runTest {
        optIn()

        assertEquals(AutoUpdateOutcome.UnsupportedRegion, runner.run(AccountRegion.JAPAN, site.importer))
        assertEquals(0, site.refreshCalls)
    }

    @Test
    fun validSessionUpdatesWithoutSendingCredentials() = runTest {
        optIn()
        site.signedIn = true

        val outcome = run()

        assertEquals(AutoUpdateOutcome.Updated(FakeDxNet.IMPORTED, signedIn = false), outcome)
        assertEquals(0, site.signInCalls)
        assertEquals(1, site.saved)
        assertEquals(listOf("started", "progress:Importing…", "finished:Updated 2 scores"), reporter.events)
    }

    @Test
    fun expiredSessionSignsInOnceThenImports() = runTest {
        optIn()

        val outcome = run()

        assertTrue(outcome is AutoUpdateOutcome.Updated && outcome.signedIn)
        assertEquals(1, site.signInCalls)
        assertEquals(2, site.refreshCalls)
        assertEquals("player", site.lastCredentials?.username)
        assertEquals("started", reporter.events.first())
        assertTrue(reporter.events.last().startsWith("finished:Updated"))
    }

    @Test
    fun wrongPasswordPausesWithoutRetryingOrLockingOut() = runTest {
        optIn(password = "wrong")

        val first = run()
        assertTrue(first is AutoUpdateOutcome.Failed && first.reason == FailureReason.WRONG_CREDENTIALS)
        assertTrue(reporter.events.last().startsWith("finished:DX NET rejected"))

        now += 24 * 60 * 60 * 1000L // even a day later, with force
        assertTrue(run(force = true) is AutoUpdateOutcome.Paused)
        assertEquals(1, site.signInCalls)
        assertEquals(0, site.saved)

        // Saving corrected credentials resumes automatic sign-in.
        store.saveCredentials(DxNetCredentials("player", "right"))
        assertNull(store.rejectionMessage)
        assertTrue(run() is AutoUpdateOutcome.Updated)
        assertEquals(2, site.signInCalls)
    }

    @Test
    fun networkFailureIsReportedAndThrottledNotLooped() = runTest {
        optIn()
        site.failure = UnknownHostException("maimaidx-eng.com")

        val first = run()
        assertTrue(first is AutoUpdateOutcome.Failed && first.reason == FailureReason.NETWORK)
        assertTrue(reporter.events.last().startsWith("finished:Couldn't reach DX NET"))

        // Re-opening the app right away does not hammer DX NET.
        now += 30_000L
        assertEquals(AutoUpdateOutcome.Throttled, run())
        assertEquals(1, site.refreshCalls)

        // After the interval a fresh attempt is allowed, and credentials remain usable.
        now += 60_000L
        site.failure = null
        site.signedIn = true
        assertTrue(run() is AutoUpdateOutcome.Updated)
    }

    @Test
    fun sitePageChangeFailsWithoutPausingCredentials() = runTest {
        optIn()
        site.signInResult = SignInResult.PageChanged("The SEGA ID sign-in form was not found on DX NET")

        val outcome = run()

        assertTrue(outcome is AutoUpdateOutcome.Failed && outcome.reason == FailureReason.SITE_CHANGED)
        assertNull(store.rejectionMessage)
        assertEquals(1, site.refreshCalls)
    }

    @Test
    fun sessionRejectedAfterSignInDoesNotLoop() = runTest {
        optIn()
        site.acceptSession = false

        val outcome = run()

        assertTrue(outcome is AutoUpdateOutcome.Failed && outcome.reason == FailureReason.SITE_CHANGED)
        assertEquals(1, site.signInCalls)
        assertEquals(2, site.refreshCalls)
    }

    @Test
    fun gatewayRejectionIsReported() = runTest {
        optIn()
        site.signInResult = SignInResult.Rejected("Under maintenance.")

        assertEquals(
            AutoUpdateOutcome.Failed(FailureReason.SITE_REJECTED, "DX NET refused sign-in: Under maintenance."),
            run(),
        )
        assertNull(store.rejectionMessage)
    }

    @Test
    fun forcedRunBypassesThrottle() = runTest {
        optIn()
        site.signedIn = true
        run()
        now += 1_000L

        assertTrue(run(force = true) is AutoUpdateOutcome.Updated)
        assertEquals(2, site.refreshCalls)
    }

    @Test
    fun cancellationPropagatesWithoutFinishedReport() = runTest {
        optIn()
        site.failure = CancellationException("stopped")
        try {
            run()
            fail("Expected cancellation")
        } catch (_: CancellationException) {
        }
        assertEquals(listOf("started"), reporter.events)
    }

    @Test
    fun recoverExpiredSessionSignsInOnceAndRefreshesOnce() = runTest {
        optIn()

        val outcome = runner.recoverExpiredSession(AccountRegion.INTERNATIONAL, site.importer)

        assertTrue(outcome is AutoUpdateOutcome.Updated && outcome.signedIn)
        assertEquals(1, site.signInCalls)
        assertEquals(1, site.refreshCalls)
    }

    @Test
    fun recoverySharesThrottleWithAppOpenUpdate() = runTest {
        optIn()
        run() // app open signs in

        now += 1_000L
        assertEquals(
            AutoUpdateOutcome.Throttled,
            runner.recoverExpiredSession(AccountRegion.INTERNATIONAL, site.importer),
        )
        assertEquals(1, site.signInCalls)
    }
}

/** In-memory DX NET: session state, a SEGA ID sign-in, and an import that needs the session. */
class FakeDxNet : DxNetSignIn {
    var signedIn = false
    var acceptSession = true
    var failure: Exception? = null
    var signInResult: SignInResult? = null
    var refreshCalls = 0
    var signInCalls = 0
    var saved = 0
    var lastCredentials: DxNetCredentials? = null

    val importer = DxNetRefresh { _, onProgress ->
        refreshCalls += 1
        failure?.let { throw it }
        if (!signedIn) throw AuthenticationRequiredException("DX NET login expired; sign in again")
        onProgress("Importing…")
        saved += 1
        IMPORTED
    }

    override suspend fun signIn(region: AccountRegion, credentials: DxNetCredentials): SignInResult {
        signInCalls += 1
        lastCredentials = credentials
        signInResult?.let { return it }
        if (credentials.password != "right") {
            return SignInResult.InvalidCredentials("SEGA ID or password is incorrect.")
        }
        signedIn = acceptSession
        return SignInResult.Success
    }

    companion object {
        val IMPORTED = RefreshResult(summary = "Updated 2 scores", scores = 2, unmatched = 1)
    }
}

class RecordingReporter : AutoUpdateReporter {
    val events = mutableListOf<String>()
    override fun started() {
        events += "started"
    }

    override fun progress(message: String) {
        events += "progress:$message"
    }

    override fun finished(outcome: AutoUpdateOutcome.Finished) {
        events += "finished:${outcome.summary}"
    }
}
