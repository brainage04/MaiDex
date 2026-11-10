package io.github.brainage04.maidex.autologin

import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.network.AuthenticationRequiredException
import io.github.brainage04.maidex.network.DX_NET_MAINTENANCE_MESSAGE
import io.github.brainage04.maidex.network.DxNetMaintenanceException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Signs in to DX NET with a SEGA ID; faked in tests. */
fun interface DxNetSignIn {
    suspend fun signIn(region: AccountRegion, credentials: DxNetCredentials): SignInResult
}

/**
 * Fetches and saves DX NET data with the current session, throwing
 * [AuthenticationRequiredException] when signed out. The full import (app open) and the lighter
 * hourly tracking sync are both refreshes.
 */
fun interface DxNetRefresh {
    suspend fun refresh(region: AccountRegion, onProgress: (String) -> Unit): RefreshResult
}

data class RefreshResult(
    val summary: String,
    val scores: Int = 0,
    val recentDetails: Int = 0,
    val unmatched: Int = 0,
)

/** Receives the visible start/progress/end of an automatic update (notification + in-app status). */
interface AutoUpdateReporter {
    fun started()
    fun progress(message: String)
    fun finished(outcome: AutoUpdateOutcome.Finished)
}

sealed interface AutoUpdateOutcome {
    /** Nothing was attempted, so nothing is shown to the user. */
    sealed interface Skipped : AutoUpdateOutcome
    data object Disabled : Skipped
    data object NoCredentials : Skipped
    data object UnsupportedRegion : Skipped
    data object Throttled : Skipped
    data class Paused(val reason: String) : Skipped

    sealed interface Finished : AutoUpdateOutcome {
        val summary: String
    }

    data class Updated(val result: RefreshResult, val signedIn: Boolean) : Finished {
        override val summary: String get() = result.summary
    }

    data class Failed(val reason: FailureReason, val message: String) : Finished {
        override val summary: String get() = message
    }

    /**
     * DX NET or the sign-in gateway was in maintenance. Not a failed sign-in: nothing is paused,
     * and the next app open or hourly run tries again.
     */
    data object Maintenance : Finished {
        override val summary: String get() = DX_NET_MAINTENANCE_MESSAGE
    }
}

enum class FailureReason { WRONG_CREDENTIALS, SITE_REJECTED, SITE_CHANGED, NETWORK, CREDENTIALS_UNREADABLE, OTHER }

/**
 * Automatic "sign in if needed, then refresh" passes, shared by the app-open update and the
 * hourly tracker so both obey the same safeguards: nothing happens unless the user opted in and
 * saved credentials; at most one credential sign-in per pass; a wrong password pauses automatic
 * sign-in until the user saves new credentials (so SEGA never sees repeated bad logins); passes
 * are throttled to one per [minIntervalMillis] across both callers; nothing here retries.
 */
class AutoUpdateRunner(
    private val store: AutoLoginStore,
    private val signIn: DxNetSignIn,
    private val reporter: AutoUpdateReporter,
    private val clock: () -> Long = System::currentTimeMillis,
    private val minIntervalMillis: Long = DEFAULT_MIN_INTERVAL_MILLIS,
) {
    /** App open: refresh with the existing session, signing in once if it has expired. */
    suspend fun run(region: AccountRegion, refresh: DxNetRefresh, force: Boolean = false): AutoUpdateOutcome =
        guarded(region, force) {
            try {
                AutoUpdateOutcome.Updated(refresh.refresh(region, reporter::progress), signedIn = false)
            } catch (_: AuthenticationRequiredException) {
                signInThenRefresh(region, refresh)
            }
        }

    /**
     * Hourly tracker: the caller's silent refresh already found the session expired, so sign in
     * once and refresh once. Same gates and throttle as [run]; never forced.
     */
    suspend fun recoverExpiredSession(region: AccountRegion, refresh: DxNetRefresh): AutoUpdateOutcome =
        guarded(region, force = false) { signInThenRefresh(region, refresh) }

    private suspend fun guarded(
        region: AccountRegion,
        force: Boolean,
        attempt: suspend () -> AutoUpdateOutcome.Finished,
    ): AutoUpdateOutcome {
        // Check-and-record is atomic so the app-open update and the tracker can't both sign in.
        gate(region, force)?.let { return it }
        reporter.started()
        val outcome = try {
            attempt()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: DxNetMaintenanceException) {
            AutoUpdateOutcome.Maintenance
        } catch (failure: IOException) {
            AutoUpdateOutcome.Failed(
                FailureReason.NETWORK,
                "Couldn't reach DX NET${failure.message?.let { ": $it" }.orEmpty()}",
            )
        } catch (failure: Exception) {
            AutoUpdateOutcome.Failed(FailureReason.OTHER, failure.message ?: "DX NET update failed")
        }
        store.recordResult(outcome.summary)
        reporter.finished(outcome)
        return outcome
    }

    private suspend fun gate(region: AccountRegion, force: Boolean): AutoUpdateOutcome.Skipped? =
        attemptLock.withLock {
            if (!store.isEnabled) return@withLock AutoUpdateOutcome.Disabled
            if (!region.supportsSegaIdLogin) return@withLock AutoUpdateOutcome.UnsupportedRegion
            store.rejectionMessage?.let { return@withLock AutoUpdateOutcome.Paused(it) }
            if (!store.hasCredentials) return@withLock AutoUpdateOutcome.NoCredentials
            val now = clock()
            if (!force && now - store.lastAttemptAt in 0 until minIntervalMillis) {
                return@withLock AutoUpdateOutcome.Throttled
            }
            store.recordAttempt(now)
            null
        }

    private suspend fun signInThenRefresh(region: AccountRegion, refresh: DxNetRefresh): AutoUpdateOutcome.Finished {
        val credentials = store.loadCredentials()
            ?: return AutoUpdateOutcome.Failed(
                FailureReason.CREDENTIALS_UNREADABLE,
                "Saved DX NET credentials could not be decrypted and were removed; enter them again",
            )
        reporter.progress("Signing in to DX NET…")
        when (val result = signIn.signIn(region, credentials)) {
            SignInResult.Success -> Unit
            is SignInResult.InvalidCredentials -> {
                val message = "DX NET rejected the saved SEGA ID or password (${result.message.trimEnd('.')}). " +
                    "Automatic sign-in is paused until you update your credentials."
                store.markRejected(message)
                return AutoUpdateOutcome.Failed(FailureReason.WRONG_CREDENTIALS, message)
            }
            is SignInResult.Rejected -> return AutoUpdateOutcome.Failed(
                FailureReason.SITE_REJECTED,
                "DX NET refused sign-in: ${result.message}",
            )
            is SignInResult.PageChanged -> return AutoUpdateOutcome.Failed(
                FailureReason.SITE_CHANGED,
                "${result.message}. Sign in manually; MaiDex may need an update.",
            )
        }
        return try {
            AutoUpdateOutcome.Updated(refresh.refresh(region, reporter::progress), signedIn = true)
        } catch (_: AuthenticationRequiredException) {
            AutoUpdateOutcome.Failed(
                FailureReason.SITE_CHANGED,
                "Signed in, but DX NET did not accept the session. Sign in manually; MaiDex may need an update.",
            )
        }
    }

    companion object {
        const val DEFAULT_MIN_INTERVAL_MILLIS = 15L * 60L * 1000L
        private val attemptLock = Mutex()
    }
}
