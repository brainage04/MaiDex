package io.github.brainage04.maidex.autologin

import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.data.ImportResult
import io.github.brainage04.maidex.data.PlayerProfile
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
    private val gateway = FakeGateway()
    private val reporter = RecordingReporter()
    private val saved = mutableListOf<ImportResult>()
    private var now = 10_000_000L
    private val runner = AutoUpdateRunner(
        store = store,
        gateway = gateway,
        saveImport = { saved += it },
        reporter = reporter,
        clock = { now },
        minIntervalMillis = 60_000L,
    )

    private fun optIn(password: String = "right") {
        store.saveCredentials(DxNetCredentials("player", password))
        store.setEnabled(true)
    }

    @Test
    fun disabledByDefaultDoesNothing() = runTest {
        store.saveCredentials(DxNetCredentials("player", "right"))

        assertEquals(AutoUpdateOutcome.Disabled, runner.run(AccountRegion.INTERNATIONAL))
        assertEquals(0, gateway.importCalls + gateway.signInCalls)
        assertTrue(reporter.events.isEmpty())
    }

    @Test
    fun enabledWithoutCredentialsDoesNothing() = runTest {
        store.setEnabled(true)

        assertEquals(AutoUpdateOutcome.NoCredentials, runner.run(AccountRegion.INTERNATIONAL))
        assertTrue(reporter.events.isEmpty())
    }

    @Test
    fun japanIsUnsupported() = runTest {
        optIn()

        assertEquals(AutoUpdateOutcome.UnsupportedRegion, runner.run(AccountRegion.JAPAN))
        assertEquals(0, gateway.importCalls)
    }

    @Test
    fun validSessionUpdatesWithoutSendingCredentials() = runTest {
        optIn()
        gateway.signedIn = true

        val outcome = runner.run(AccountRegion.INTERNATIONAL)

        assertEquals(AutoUpdateOutcome.Updated(scores = 2, recentDetails = 0, unmatched = 1, signedIn = false), outcome)
        assertEquals(0, gateway.signInCalls)
        assertEquals(1, saved.size)
        assertEquals(listOf("started", "progress:Importing…", "finished:Updated 2 scores and 0 recent plays"), reporter.events)
    }

    @Test
    fun expiredSessionSignsInOnceThenImports() = runTest {
        optIn()

        val outcome = runner.run(AccountRegion.INTERNATIONAL)

        assertTrue(outcome is AutoUpdateOutcome.Updated && outcome.signedIn)
        assertEquals(1, gateway.signInCalls)
        assertEquals(2, gateway.importCalls)
        assertEquals("player", gateway.lastCredentials?.username)
        assertEquals("started", reporter.events.first())
        assertTrue(reporter.events.last().startsWith("finished:Updated"))
    }

    @Test
    fun wrongPasswordPausesWithoutRetryingOrLockingOut() = runTest {
        optIn(password = "wrong")

        val first = runner.run(AccountRegion.INTERNATIONAL)
        assertTrue(first is AutoUpdateOutcome.Failed && first.reason == FailureReason.WRONG_CREDENTIALS)
        assertTrue(reporter.events.last().startsWith("finished:DX NET rejected"))

        now += 24 * 60 * 60 * 1000L // even a day later, with force
        val second = runner.run(AccountRegion.INTERNATIONAL, force = true)
        assertTrue(second is AutoUpdateOutcome.Paused)
        assertEquals(1, gateway.signInCalls)
        assertTrue(saved.isEmpty())

        // Saving corrected credentials resumes automatic sign-in.
        store.saveCredentials(DxNetCredentials("player", "right"))
        assertNull(store.rejectionMessage)
        assertTrue(runner.run(AccountRegion.INTERNATIONAL) is AutoUpdateOutcome.Updated)
        assertEquals(2, gateway.signInCalls)
    }

    @Test
    fun networkFailureIsReportedAndThrottledNotLooped() = runTest {
        optIn()
        gateway.importFailure = UnknownHostException("maimaidx-eng.com")

        val first = runner.run(AccountRegion.INTERNATIONAL)
        assertTrue(first is AutoUpdateOutcome.Failed && first.reason == FailureReason.NETWORK)
        assertTrue(reporter.events.last().startsWith("finished:Couldn't reach DX NET"))

        // Re-opening the app right away does not hammer DX NET.
        now += 30_000L
        assertEquals(AutoUpdateOutcome.Throttled, runner.run(AccountRegion.INTERNATIONAL))
        assertEquals(1, gateway.importCalls)

        // After the interval a fresh attempt is allowed, and credentials remain usable.
        now += 60_000L
        gateway.importFailure = null
        gateway.signedIn = true
        assertTrue(runner.run(AccountRegion.INTERNATIONAL) is AutoUpdateOutcome.Updated)
    }

    @Test
    fun sitePageChangeFailsWithoutPausingCredentials() = runTest {
        optIn()
        gateway.signInResult = SignInResult.PageChanged("The SEGA ID sign-in form was not found on DX NET")

        val outcome = runner.run(AccountRegion.INTERNATIONAL)

        assertTrue(outcome is AutoUpdateOutcome.Failed && outcome.reason == FailureReason.SITE_CHANGED)
        assertNull(store.rejectionMessage)
        assertEquals(1, gateway.importCalls)
    }

    @Test
    fun sessionRejectedAfterSignInDoesNotLoop() = runTest {
        optIn()
        gateway.acceptSession = false

        val outcome = runner.run(AccountRegion.INTERNATIONAL)

        assertTrue(outcome is AutoUpdateOutcome.Failed && outcome.reason == FailureReason.SITE_CHANGED)
        assertEquals(1, gateway.signInCalls)
        assertEquals(2, gateway.importCalls)
    }

    @Test
    fun gatewayRejectionIsReported() = runTest {
        optIn()
        gateway.signInResult = SignInResult.Rejected("Under maintenance.")

        val outcome = runner.run(AccountRegion.INTERNATIONAL)

        assertEquals(
            AutoUpdateOutcome.Failed(FailureReason.SITE_REJECTED, "DX NET refused sign-in: Under maintenance."),
            outcome,
        )
        assertNull(store.rejectionMessage)
    }

    @Test
    fun forcedRunBypassesThrottle() = runTest {
        optIn()
        gateway.signedIn = true
        runner.run(AccountRegion.INTERNATIONAL)
        now += 1_000L

        assertTrue(runner.run(AccountRegion.INTERNATIONAL, force = true) is AutoUpdateOutcome.Updated)
        assertEquals(2, gateway.importCalls)
    }

    @Test
    fun cancellationPropagatesWithoutFinishedReport() = runTest {
        optIn()
        gateway.importFailure = CancellationException("stopped")
        try {
            runner.run(AccountRegion.INTERNATIONAL)
            fail("Expected cancellation")
        } catch (_: CancellationException) {
        }
        assertEquals(listOf("started"), reporter.events)
    }

    private class FakeGateway : DxNetGateway {
        var signedIn = false
        var acceptSession = true
        var importFailure: Exception? = null
        var signInResult: SignInResult? = null
        var importCalls = 0
        var signInCalls = 0
        var lastCredentials: DxNetCredentials? = null

        override suspend fun importAll(region: AccountRegion, onProgress: (String) -> Unit): ImportResult {
            importCalls += 1
            importFailure?.let { throw it }
            if (!signedIn) throw AuthenticationRequiredException("DX NET login expired; sign in again")
            onProgress("Importing…")
            return ImportResult(
                profile = PlayerProfile(name = "PLAYER", officialRating = 15000, region = region),
                scores = List(2) { index ->
                    io.github.brainage04.maidex.data.UserScore(
                        chartKey = "chart-$index",
                        achievement = 100.0,
                        grade = io.github.brainage04.maidex.data.Grade.SSS,
                        comboMedal = io.github.brainage04.maidex.data.ComboMedal.NONE,
                        syncMedal = io.github.brainage04.maidex.data.SyncMedal.NONE,
                        dxScore = 0,
                        maxDxScore = 0,
                    )
                },
                playDetails = emptyList(),
                unmatchedCharts = 1,
            )
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
    }

    private class RecordingReporter : AutoUpdateReporter {
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
}
