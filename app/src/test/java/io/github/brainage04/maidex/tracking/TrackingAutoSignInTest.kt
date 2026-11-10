package io.github.brainage04.maidex.tracking

import io.github.brainage04.maidex.autologin.AutoLoginStore
import io.github.brainage04.maidex.autologin.AutoUpdateRunner
import io.github.brainage04.maidex.autologin.DxNetCredentials
import io.github.brainage04.maidex.autologin.FakeDxNet
import io.github.brainage04.maidex.autologin.InMemoryPreferences
import io.github.brainage04.maidex.autologin.RecordingReporter
import io.github.brainage04.maidex.autologin.SignInResult
import io.github.brainage04.maidex.autologin.SoftwareAesCipher
import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.network.DX_NET_MAINTENANCE_MESSAGE
import io.github.brainage04.maidex.network.DxNetMaintenanceException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

/** The hourly tracker re-signs in through the shared runner, with the same safeguards. */
class TrackingAutoSignInTest {
    private val store = AutoLoginStore(InMemoryPreferences(), SoftwareAesCipher())
    private val site = FakeDxNet()
    private val reporter = RecordingReporter()
    private val failures = mutableListOf<String>()
    private var now = 50_000_000L
    private val runner = AutoUpdateRunner(store, site, reporter, clock = { now }, minIntervalMillis = 15 * 60_000L)

    private fun optIn(password: String = "right") {
        store.saveCredentials(DxNetCredentials("player", password))
        store.setEnabled(true)
    }

    private suspend fun hourlySync(runAttemptCount: Int = 0) = runTrackingSync(
        region = AccountRegion.INTERNATIONAL,
        sync = site.importer,
        runner = runner,
        recordFailure = { failures += it },
        runAttemptCount = runAttemptCount,
    )

    @Test
    fun validSessionSyncsSilentlyWithoutSignIn() = runTest {
        optIn()
        site.signedIn = true

        assertEquals(TrackingSyncResult.DONE, hourlySync())
        assertEquals(0, site.signInCalls)
        assertTrue("no notification for an ordinary hourly sync", reporter.events.isEmpty())
        assertTrue(failures.isEmpty())
    }

    @Test
    fun expiredSessionWithoutOptInRecordsFailureAndDoesNotSignIn() = runTest {
        store.saveCredentials(DxNetCredentials("player", "right")) // saved but disabled

        assertEquals(TrackingSyncResult.DONE, hourlySync())
        assertEquals(0, site.signInCalls)
        assertEquals(listOf("DX NET login expired; sign in again"), failures)
        assertTrue(reporter.events.isEmpty())
    }

    @Test
    fun expiredSessionSignsInAgainWithStartAndEndNotifications() = runTest {
        optIn()

        assertEquals(TrackingSyncResult.DONE, hourlySync())

        assertEquals(1, site.signInCalls)
        assertEquals(2, site.refreshCalls) // silent attempt + one after sign-in
        assertEquals(1, site.saved)
        assertEquals("started", reporter.events.first())
        assertTrue(reporter.events.last().startsWith("finished:Updated"))
        assertTrue(failures.isEmpty())
    }

    @Test
    fun wrongPasswordPausesTrackerSignInToo() = runTest {
        optIn(password = "wrong")

        hourlySync()
        now += 2 * 60 * 60_000L // two hours of tracker runs later
        hourlySync()
        now += 60 * 60_000L
        hourlySync()

        assertEquals(1, site.signInCalls)
        assertTrue(store.rejectionMessage != null)
        assertTrue(failures.first().startsWith("DX NET rejected the saved SEGA ID or password"))
        assertEquals(1, reporter.events.count { it == "started" })
    }

    @Test
    fun trackerRespectsThrottleSharedWithAppOpen() = runTest {
        optIn()
        runner.run(AccountRegion.INTERNATIONAL, site.importer) // app open signs in
        site.signedIn = false // session lost again straight away
        val signInsAfterAppOpen = site.signInCalls

        now += 5 * 60_000L
        assertEquals(TrackingSyncResult.DONE, hourlySync())
        assertEquals(signInsAfterAppOpen, site.signInCalls)

        now += 15 * 60_000L
        hourlySync()
        assertEquals(signInsAfterAppOpen + 1, site.signInCalls)
    }

    @Test
    fun failedSignInIsNeverRetriedByWorkManager() = runTest {
        optIn()
        site.signInResult = SignInResult.PageChanged("The SEGA ID sign-in form was not found on DX NET")

        assertEquals(TrackingSyncResult.DONE, hourlySync(runAttemptCount = 0))
        assertEquals(1, site.signInCalls)
        assertTrue(failures.single().contains("sign-in form was not found"))
    }

    @Test
    fun networkFailureBeforeSignInKeepsExistingRetryPolicy() = runTest {
        optIn()
        site.failure = UnknownHostException("maimaidx-eng.com")

        assertEquals(TrackingSyncResult.RETRY, hourlySync(runAttemptCount = 0))
        assertEquals(TrackingSyncResult.DONE, hourlySync(runAttemptCount = 2))
        assertEquals(0, site.signInCalls)
    }

    @Test
    fun dxNetMaintenanceIsRecordedAsMaintenanceWithoutSignInOrRetry() = runTest {
        optIn()
        site.failure = DxNetMaintenanceException()

        assertEquals(TrackingSyncResult.DONE, hourlySync(runAttemptCount = 0))
        assertEquals(0, site.signInCalls)
        assertEquals(listOf(DX_NET_MAINTENANCE_MESSAGE), failures)
        assertTrue(reporter.events.isEmpty())
    }

    @Test
    fun gatewayMaintenanceDuringReSignInIsRecordedAsMaintenance() = runTest {
        optIn()
        site.signInFailure = DxNetMaintenanceException()

        assertEquals(TrackingSyncResult.DONE, hourlySync())
        assertEquals(1, site.signInCalls)
        assertEquals(listOf(DX_NET_MAINTENANCE_MESSAGE), failures)
        assertEquals("finished:$DX_NET_MAINTENANCE_MESSAGE", reporter.events.last())
        assertEquals(null, store.rejectionMessage)
    }
}
