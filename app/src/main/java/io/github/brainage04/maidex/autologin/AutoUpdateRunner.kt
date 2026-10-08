package io.github.brainage04.maidex.autologin

import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.data.ImportResult
import io.github.brainage04.maidex.network.AuthenticationRequiredException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** The DX NET operations the automatic update needs; faked in tests. */
interface DxNetGateway {
    /** Imports everything with the current session; throws [AuthenticationRequiredException] when signed out. */
    suspend fun importAll(region: AccountRegion, onProgress: (String) -> Unit): ImportResult

    suspend fun signIn(region: AccountRegion, credentials: DxNetCredentials): SignInResult
}

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

    data class Updated(
        val scores: Int,
        val recentDetails: Int,
        val unmatched: Int,
        val signedIn: Boolean,
    ) : Finished {
        override val summary: String
            get() = "Updated $scores scores and $recentDetails recent plays"
    }

    data class Failed(val reason: FailureReason, val message: String) : Finished {
        override val summary: String get() = message
    }
}

enum class FailureReason { WRONG_CREDENTIALS, SITE_REJECTED, SITE_CHANGED, NETWORK, CREDENTIALS_UNREADABLE, OTHER }

/**
 * One automatic "sign in if needed, then import" pass.
 *
 * Safety properties: the existing session is tried first, at most one credential sign-in is made
 * per run, a wrong password pauses automatic sign-in until the user saves new credentials (so
 * SEGA never sees repeated bad logins), and attempts are throttled to one per [minIntervalMillis]
 * however often the app is opened. Nothing here retries.
 */
class AutoUpdateRunner(
    private val store: AutoLoginStore,
    private val gateway: DxNetGateway,
    private val saveImport: suspend (ImportResult) -> Unit,
    private val reporter: AutoUpdateReporter,
    private val clock: () -> Long = System::currentTimeMillis,
    private val minIntervalMillis: Long = DEFAULT_MIN_INTERVAL_MILLIS,
) {
    suspend fun run(region: AccountRegion, force: Boolean = false): AutoUpdateOutcome {
        if (!store.isEnabled) return AutoUpdateOutcome.Disabled
        if (!region.supportsSegaIdLogin) return AutoUpdateOutcome.UnsupportedRegion
        store.rejectionMessage?.let { return AutoUpdateOutcome.Paused(it) }
        if (!store.hasCredentials) return AutoUpdateOutcome.NoCredentials
        val now = clock()
        val sinceLast = now - store.lastAttemptAt
        if (!force && sinceLast in 0 until minIntervalMillis) return AutoUpdateOutcome.Throttled

        store.recordAttempt(now)
        reporter.started()
        val outcome = try {
            attempt(region)
        } catch (cancelled: CancellationException) {
            throw cancelled
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

    private suspend fun attempt(region: AccountRegion): AutoUpdateOutcome.Finished {
        val credentials = store.loadCredentials()
            ?: return AutoUpdateOutcome.Failed(
                FailureReason.CREDENTIALS_UNREADABLE,
                "Saved DX NET credentials could not be decrypted and were removed; enter them again",
            )
        var signedIn = false
        val result = try {
            gateway.importAll(region, reporter::progress)
        } catch (_: AuthenticationRequiredException) {
            reporter.progress("Signing in to DX NET…")
            when (val signIn = gateway.signIn(region, credentials)) {
                SignInResult.Success -> signedIn = true
                is SignInResult.InvalidCredentials -> {
                    val message = "DX NET rejected the saved SEGA ID or password (${signIn.message.trimEnd('.')}). " +
                        "Automatic sign-in is paused until you update your credentials."
                    store.markRejected(message)
                    return AutoUpdateOutcome.Failed(FailureReason.WRONG_CREDENTIALS, message)
                }
                is SignInResult.Rejected -> return AutoUpdateOutcome.Failed(
                    FailureReason.SITE_REJECTED,
                    "DX NET refused sign-in: ${signIn.message}",
                )
                is SignInResult.PageChanged -> return AutoUpdateOutcome.Failed(
                    FailureReason.SITE_CHANGED,
                    "${signIn.message}. Sign in manually; MaiDex may need an update.",
                )
            }
            try {
                gateway.importAll(region, reporter::progress)
            } catch (_: AuthenticationRequiredException) {
                return AutoUpdateOutcome.Failed(
                    FailureReason.SITE_CHANGED,
                    "Signed in, but DX NET did not accept the session. Sign in manually; MaiDex may need an update.",
                )
            }
        }
        saveImport(result)
        return AutoUpdateOutcome.Updated(
            scores = result.scores.size,
            recentDetails = result.playDetails.size,
            unmatched = result.unmatchedCharts,
            signedIn = signedIn,
        )
    }

    companion object {
        const val DEFAULT_MIN_INTERVAL_MILLIS = 15L * 60L * 1000L
    }
}
