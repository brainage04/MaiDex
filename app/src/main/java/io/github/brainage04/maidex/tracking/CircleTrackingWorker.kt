package io.github.brainage04.maidex.tracking

import android.content.Context
import android.webkit.CookieManager
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.brainage04.maidex.autologin.AutoLoginStore
import io.github.brainage04.maidex.autologin.AutoUpdateNotifier
import io.github.brainage04.maidex.autologin.AutoUpdateOutcome
import io.github.brainage04.maidex.autologin.AutoUpdateReporter
import io.github.brainage04.maidex.autologin.AutoUpdateRunner
import io.github.brainage04.maidex.autologin.DxNetRefresh
import io.github.brainage04.maidex.autologin.DxNetSignIn
import io.github.brainage04.maidex.autologin.RefreshResult
import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.data.UserDataRepository
import io.github.brainage04.maidex.network.AuthenticationRequiredException
import io.github.brainage04.maidex.network.MaimaiDxClient
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class CircleTrackingWorker(
    appContext: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(appContext, parameters) {
    override suspend fun doWork(): Result {
        val repository = UserDataRepository(applicationContext)
        val profile = withContext(Dispatchers.IO) { repository.loadProfile() }
            ?: return Result.success()
        repository.recordTrackingAttempt()
        val client = MaimaiDxClient(withContext(Dispatchers.Main) { CookieManager.getInstance() })
        val sync = DxNetRefresh { region, _ ->
            val snapshot = client.syncAccount(region)
            withContext(Dispatchers.IO) {
                repository.saveTrackingSnapshot(snapshot.profile, snapshot.circle)
                repository.recordTrackingSuccess()
            }
            RefreshResult("Signed in again and refreshed circle and play-count data")
        }
        val notifier = AutoUpdateNotifier(applicationContext)
        val runner = AutoUpdateRunner(
            store = AutoLoginStore.create(applicationContext),
            signIn = DxNetSignIn { region, credentials -> client.signInWithSegaId(region, credentials) },
            reporter = object : AutoUpdateReporter {
                override fun started() = notifier.showRunning("Your DX NET session expired; signing in again…")
                override fun progress(message: String) = notifier.showRunning(message)
                override fun finished(outcome: AutoUpdateOutcome.Finished) = notifier.showFinished(outcome)
            },
        )
        return try {
            when (
                runTrackingSync(
                    region = profile.region,
                    sync = sync,
                    runner = runner,
                    recordFailure = repository::recordTrackingFailure,
                    runAttemptCount = runAttemptCount,
                )
            ) {
                TrackingSyncResult.DONE -> Result.success()
                TrackingSyncResult.RETRY -> Result.retry()
            }
        } catch (cancelled: CancellationException) {
            notifier.dismiss()
            throw cancelled
        }
    }
}

internal enum class TrackingSyncResult { DONE, RETRY }

/**
 * One hourly sync. The existing session is used silently; only if it has expired does the
 * opt-in automatic sign-in step in, through [runner] so it obeys the same gates, wrong-password
 * pause, shared 15-minute throttle and start/end notifications as the app-open update. A pass
 * that attempted a sign-in is never retried by WorkManager.
 */
internal suspend fun runTrackingSync(
    region: AccountRegion,
    sync: DxNetRefresh,
    runner: AutoUpdateRunner,
    recordFailure: (String) -> Unit,
    runAttemptCount: Int,
): TrackingSyncResult = try {
    sync.refresh(region) {}
    TrackingSyncResult.DONE
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (expired: AuthenticationRequiredException) {
    when (val outcome = runner.recoverExpiredSession(region, sync)) {
        is AutoUpdateOutcome.Updated -> Unit
        is AutoUpdateOutcome.Failed -> recordFailure(outcome.message)
        is AutoUpdateOutcome.Skipped -> recordFailure(expired.message ?: "DX NET login expired; sign in again")
    }
    TrackingSyncResult.DONE
} catch (failure: Exception) {
    recordFailure(failure.message ?: "Unable to refresh DX NET tracking data")
    if (shouldRetryHourlySync(failure, runAttemptCount)) TrackingSyncResult.RETRY else TrackingSyncResult.DONE
}

object CircleTrackingScheduler {
    private const val WORK_NAME = "hourly-dx-net-tracking"
    private const val LEGACY_WORK_NAME = "daily-dx-net-tracking"

    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(LEGACY_WORK_NAME)
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<CircleTrackingWorker>(
                TRACKING_REPEAT_INTERVAL_HOURS,
                TimeUnit.HOURS,
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build(),
        )
    }

    fun cancel(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WORK_NAME)
        workManager.cancelUniqueWork(LEGACY_WORK_NAME)
    }
}

internal const val TRACKING_REPEAT_INTERVAL_HOURS = 1L

internal fun shouldRetryHourlySync(failure: Exception, runAttemptCount: Int): Boolean =
    failure !is AuthenticationRequiredException && runAttemptCount < 2
